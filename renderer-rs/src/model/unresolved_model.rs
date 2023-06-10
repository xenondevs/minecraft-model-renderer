use std::cell::RefCell;
use std::collections::HashMap;
use std::ops::Div;

use jni::JNIEnv;
use serde_json::{to_vec, Value};

use crate::model::direction::{Axis, Direction, ElementRotation};
use crate::model::resource::resource_id::ResourceId;
use crate::model::resource::resource_manager::ResourceManager;
use crate::util::math::Vector3d;

fn as_vector(json: &Value) -> Vector3d {
    as_vector_safe(json).unwrap_or_else(|| panic!("Invalid vector: {}", json))
}

fn as_vector_safe(json: &Value) -> Option<Vector3d> {
    let array = json.as_array()?;
    if array.len() == 3 {
        let x = array[0].as_f64()?;
        let y = array[1].as_f64()?;
        let z = array[2].as_f64()?;
        return Some(Vector3d::new(x, y, z));
    }
    None
}

#[derive(Clone)]
pub struct UnresolvedModel {
    layered: bool,
    parent: Option<Box<UnresolvedModel>>,
    texture_names: HashMap<String, String>,
    elements: Vec<UnresolvedElement>,
    ambient_occlusion: bool,
    rotation: Option<Vector3d>,
    translation: Option<Vector3d>,
    scale: Option<Vector3d>,
}

impl UnresolvedModel {
    pub fn new(env: &mut JNIEnv, manager: &mut ResourceManager, id: ResourceId, json: &Value) -> UnresolvedModel {
        let (parent, layered) = Self::find_parent(env, manager, &id, json);
        let textures = Self::parse_textures(json);
        let elements: Vec<UnresolvedElement> = Vec::new();

        let mut rotation = None;
        let mut translation = None;
        let mut scale = None;
        let mut ambient_occlusion = true;

        if let Some(gui_json) = json["display"]["gui"].as_object() {
            if let Some(rotation_json) = gui_json.get("rotation") {
                rotation = as_vector_safe(rotation_json)
            }
            if let Some(translation_json) = gui_json.get("translation") {
                translation = as_vector_safe(translation_json)
            }
            if let Some(scale_json) = gui_json.get("scale") {
                scale = as_vector_safe(scale_json)
            }
            if let Some(ambient_occlusion_json) = gui_json.get("ambientocclusion") {
                ambient_occlusion = ambient_occlusion_json.as_bool().unwrap_or(true);
            }
        }

        let mut model = UnresolvedModel {
            layered,
            parent: parent.map(|model| Box::new(model)),
            texture_names: textures,
            elements,
            ambient_occlusion,
            rotation,
            translation,
            scale,
        };

        Self::parse_elements(&mut model, json);
        model
    }

    fn find_parent<'a>(env: &mut JNIEnv, manager: &'a mut ResourceManager, id: &ResourceId, json: &Value) -> (Option<UnresolvedModel>, bool) {
        return if let Some(parent_name) = json["parent"].as_str() {
            let parent_id = ResourceId::of(parent_name)
                .unwrap_or_else(|_| panic!("Invalid parent id: {}", parent_name));

            if parent_id.to_string() != "minecraft:builtin/generated" {
                let model = manager.get_model(env, &parent_id);
                (Some(model), false)
            } else { (None, true) }
        } else {
            if id.path.starts_with("block/") && id.path != "block/block" {
                let model = manager.get_model(env, &ResourceId::of("minecraft:block/block").unwrap());
                (Some(model), false)
            } else { (None, false) }
        };
    }

    fn parse_textures(json: &Value) -> HashMap<String, String> {
        let mut textures = HashMap::new();
        if let Some(obj) = json["textures"].as_object() {
            for (key, value) in obj {
                if let Some(value) = value.as_str() {
                    textures.insert(key.to_string(), value.to_string());
                }
            }
        }
        textures.remove("particle");
        textures
    }

    fn parse_elements(model: &mut UnresolvedModel, json: &Value) {
        if let Some(array) = json["elements"].as_array() {
            for element_json in array {
                model.elements.push(UnresolvedElement::new(element_json));
            }
        }
    }
}

#[derive(Clone)]
pub struct UnresolvedElement {
    from: Vector3d,
    to: Vector3d,
    rotation: Option<ElementRotation>,
    faces: HashMap<Direction, UnresolvedTexture>,
}

impl UnresolvedElement {
    pub fn new(json: &Value) -> UnresolvedElement {
        let from = as_vector(&json["from"]) / 16.0;
        let to = as_vector(&json["to"]) / 16.0;
        let rotation =
            if let Some(rotation_json) = json["rotation"].as_object() {
                Some(ElementRotation {
                    origin: as_vector(&rotation_json["origin"]) / 16.0,
                    axis: rotation_json["axis"].to_string().to_uppercase().parse::<Axis>()
                        .unwrap_or_else(|_| panic!("Invalid axis: {}", rotation_json["axis"])),
                    angle: rotation_json["angle"].as_f64()
                        .unwrap_or_else(|| panic!("Invalid angle: {}", rotation_json["angle"])),
                    rescale: rotation_json["rescale"].as_bool().unwrap_or(false),
                })
            } else { None };
        let faces = HashMap::new();

        let mut element = UnresolvedElement { from, to, rotation, faces };
        if let Some(faces_json) = json["faces"].as_object() {
            for (key, value) in faces_json {
                let direction = key.to_string().to_uppercase().parse::<Direction>()
                    .unwrap_or_else(|_| panic!("Invalid direction: {}", key));
                let texture = UnresolvedTexture::new(&element, direction.clone(), value);
                element.faces.insert(direction, texture);
            }
        }

        element
    }
}

#[derive(Clone)]
pub struct UnresolvedTexture {
    pub texture_name: String,
    pub rotation: i32,
    pub from_x: f64,
    pub from_y: f64,
    pub to_x: f64,
    pub to_y: f64,
}

impl UnresolvedTexture {
    pub fn new(element: &UnresolvedElement, direction: Direction, json: &Value) -> UnresolvedTexture {
        let texture_name = json["texture"].as_str()
            .unwrap_or_else(|| panic!("Invalid texture: {}", json["texture"]))
            .trim_start_matches('#')
            .to_string();

        let rotation = json["rotation"].as_i64().unwrap_or(0) as i32;

        let (from_x, from_y, to_x, to_y) =
            if let Some(uvs_json) = json["uv"].as_array() {
                let uvs = uvs_json.iter().map(|v| v.as_f64().unwrap() / 16.0).collect::<Vec<f64>>();
                (uvs[0], uvs[1], uvs[2], uvs[3])
            } else {
                Self::get_dynamic_uv(element, direction.clone())
            };

        UnresolvedTexture { texture_name, rotation, from_x, from_y, to_x, to_y }
    }

    fn get_dynamic_uv(parent: &UnresolvedElement, direction: Direction) -> (f64, f64, f64, f64) {
        let from_pos = Vector3d::new(parent.from.x - 0.5, parent.from.y - 0.5, parent.from.z - 0.5);
        let to_pos = Vector3d::new(parent.to.x - 0.5, parent.to.y - 0.5, parent.to.z - 0.5);

        let (axis_hor, axis_vert) = match direction.axis() {
            Axis::X => (2, 1), // west or east -> z, y
            Axis::Y => (0, 2), // up or down -> x, z
            Axis::Z => (0, 1) // north or south -> x, y
        };

        let (nx, ny) = if direction == Direction::WEST || direction == Direction::SOUTH {
            (1.0, -1.0)
        } else if direction == Direction::UP {
            (1.0, 1.0)
        } else {
            (-1.0, 1.0)
        };

        let mut x = [from_pos[axis_hor] * nx, to_pos[axis_hor] * nx];
        x.sort_by(|a, b| a.partial_cmp(b).unwrap());
        let mut y = [from_pos[axis_vert] * ny, to_pos[axis_vert] * ny];
        y.sort_by(|a, b| a.partial_cmp(b).unwrap());

        (x[0] + 0.5, y[0] + 0.5, x[1] + 0.5, y[1] + 0.5)
    }
}