use std::arch::x86_64::_mm_move_epi64;
use std::collections::HashMap;
use std::hash::{Hash, Hasher};

use image::DynamicImage;
use imageproc::geometric_transformations::{Interpolation, rotate_about_center};
use jni::JNIEnv;
use jni::objects::{JByteArray, JObject, JValue};
use jni::sys::jobject;

use crate::model::resource::resource_id::ResourceId;
use crate::model::unresolved_model::UnresolvedModel;
use crate::util::image::get_with_uv;

pub struct ResourcePack {
    pack: jobject,
}

impl ResourcePack {
    pub fn new(pack: jobject) -> ResourcePack {
        Self { pack }
    }

    unsafe fn get_resource_bytes(&self, env: &mut JNIEnv, path: String) -> Result<Vec<u8>, jni::errors::Error> {
        let jni_path = env.new_string(path)?;
        let args = &[JValue::Object(&jni_path)];
        let bytes = env.call_method(JObject::from_raw(self.pack), "getResourceBytes", "(Ljava/lang/String;)[B", args)?;
        env.convert_byte_array(JByteArray::from(bytes.l()?))
    }
}

pub struct ResourceManager {
    packs: Vec<ResourcePack>,
    model_cache: ModelCache,
    texture_cache: TextureCache,
}

impl ResourceManager {
    pub fn new(packs: Vec<ResourcePack>) -> ResourceManager {
        Self {
            packs,
            model_cache: ModelCache::new(),
            texture_cache: TextureCache::new(),
        }
    }

    pub fn get_model_bytes(&self, env: &mut JNIEnv, id: &ResourceId) -> Vec<u8> {
        self.get_bytes(env, format!("assets/{}/models/{}.json", id.namespace, id.path))
            .unwrap_or_else(|| panic!("Model {} not found", id))
    }

    pub fn get_texture_bytes(&self, env: &mut JNIEnv, id: &ResourceId) -> Vec<u8> {
        self.get_bytes(env, format!("assets/{}/textures/{}.png", id.namespace, id.path))
            .unwrap_or_else(|| panic!("Texture {} not found", id))
    }

    pub fn get_texture(&mut self, env: &mut JNIEnv, id: &ResourceId) -> DynamicImage {
        if let Some(image) = self.texture_cache.image_cache.get(&id) {
            return image.clone();
        }
        let bytes = self.get_texture_bytes(env, &id);
        let image = image::load_from_memory(&bytes).unwrap();
        self.texture_cache.image_cache.insert(id.clone(), image.clone());
        image
    }

    pub fn get_texture_with_options(
        &mut self,
        env: &mut JNIEnv,
        id: ResourceId,
        rotation: i32,
        from_x: f64,
        from_y: f64,
        to_x: f64,
        to_y: f64,
    ) -> DynamicImage {
        let options = TextureOptions { id: id.clone(), rotation, from_x, from_y, to_x, to_y };
        if let Some(image) = self.texture_cache.texture_cache.get(&options) {
            return image.clone();
        }
        let mut image = self.get_texture(env, &id);

        // take first frame of animated texture
        if image.height() > image.width() {
            image = image.crop(0, 0, image.width(), image.width());
        }

        // apply uv
        image = get_with_uv(
            &image,
            (from_x * image.width() as f64).ceil() as i32,
            (from_y * image.height() as f64).ceil() as i32,
            (to_x * image.width() as f64).ceil() as i32,
            (to_y * image.height() as f64).ceil() as i32,
        );

        // apply rotation via imageproc
        if rotation != 0 {
            image = DynamicImage::ImageRgba8(
                rotate_about_center(
                    &image.to_rgba8(),
                    rotation as f32,
                    Interpolation::Nearest,
                    image::Rgba([0, 0, 0, 0]),
                )
            );
        }

        self.texture_cache.texture_cache.insert(options, image.clone());
        image
    }

    pub fn get_bytes(&self, env: &mut JNIEnv, path: String) -> Option<Vec<u8>> {
        for pack in &self.packs {
            if let Ok(bytes) = unsafe { pack.get_resource_bytes(env, path.clone()) } {
                if !bytes.is_empty() {
                    return Some(bytes);
                }
            }
        }
        None
    }
}

struct ModelCache {
    cache: HashMap<ResourceId, UnresolvedModel>,
}

impl ModelCache {
    fn new() -> ModelCache {
        Self { cache: HashMap::new() }
    }
}

struct TextureOptions {
    id: ResourceId,
    rotation: i32,
    from_x: f64,
    from_y: f64,
    to_x: f64,
    to_y: f64,
}

impl Hash for TextureOptions {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.id.hash(state);
        self.rotation.hash(state);
        self.from_x.to_bits().hash(state);
        self.from_y.to_bits().hash(state);
        self.to_x.to_bits().hash(state);
        self.to_y.to_bits().hash(state);
    }
}

impl PartialEq<Self> for TextureOptions {
    fn eq(&self, other: &Self) -> bool {
        self.id == other.id
            && self.rotation == other.rotation
            && self.from_x == other.from_x
            && self.from_y == other.from_y
            && self.to_x == other.to_x
            && self.to_y == other.to_y
    }
}

impl Eq for TextureOptions {}

struct TextureCache {
    image_cache: HashMap<ResourceId, DynamicImage>,
    texture_cache: HashMap<TextureOptions, DynamicImage>,
}

impl TextureCache {
    fn new() -> TextureCache {
        Self {
            image_cache: HashMap::new(),
            texture_cache: HashMap::new(),
        }
    }
}