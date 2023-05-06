extern crate nalgebra as na;

use jni::JNIEnv;
use jni::objects::{JObject, JObjectArray, JValue, JValueGen};

use crate::model::resource::resource_manager::{ResourceManager, ResourcePack};
use crate::util::jni::{object_array_to_vec, string_array_to_vec};

pub mod model;
pub mod util;

struct MinecraftModelRenderer<'a> {
    render_width: i32,
    render_height: i32,
    export_width: i32,
    export_height: i32,
    resource_loader: ResourceManager,
    camera_distance: f64,
    fov: f64,
    crop_vertical: f64,
    crop_horizontal: f64,
    pipeline: JObject<'a>,
}

impl MinecraftModelRenderer<'_> {
    pub unsafe fn new<'a>(env: &mut JNIEnv, renderer_object: JObject, pipeline: JObject<'a>) -> MinecraftModelRenderer<'a> {
        let render_width = env.get_field(&renderer_object, "renderWidth", "I").unwrap().i().unwrap();
        let render_height = env.get_field(&renderer_object, "renderHeight", "I").unwrap().i().unwrap();
        let export_width = env.get_field(&renderer_object, "exportWidth", "I").unwrap().i().unwrap();
        let export_height = env.get_field(&renderer_object, "exportHeight", "I").unwrap().i().unwrap();
        let camera_distance = env.get_field(&renderer_object, "cameraDistance", "D").unwrap().d().unwrap();
        let fov = env.get_field(&renderer_object, "fov", "D").unwrap().d().unwrap();
        let crop_vertical = env.get_field(&renderer_object, "cropVertical", "D").unwrap().d().unwrap();
        let crop_horizontal = env.get_field(&renderer_object, "cropHorizontal", "D").unwrap().d().unwrap();

        let packs = JObjectArray::from(env.get_field(
            &renderer_object,
            "resourcePacks",
            "[Lxyz/xenondevs/renderer/resource/ResourcePack;"
        ).unwrap().l().unwrap());
        let packs_vec = object_array_to_vec(env, &packs).unwrap()
            .iter()
            .map(|obj| ResourcePack::new(*obj))
            .collect();
        let resource_loader = ResourceManager::new(packs_vec);

        MinecraftModelRenderer {
            render_width,
            render_height,
            export_width,
            export_height,
            resource_loader,
            camera_distance,
            fov,
            crop_vertical,
            crop_horizontal,
            pipeline
        }
    }

    pub fn notify_pipeline(&self, env: &mut JNIEnv, model: &str, image: Vec<u8>) {
        let bytes = env.byte_array_from_slice(image.as_slice()).unwrap();
        let model = env.new_string(model).unwrap();
        let args: &[JValue] = &[JValueGen::Object(&model), JValueGen::Object(&bytes)];
        env.call_method(&self.pipeline, "finish", "(Ljava/lang/String;[B)V", args)
            .expect("Failed to call finish method");
    }

    pub fn render(&self, env: &mut JNIEnv, model_path: &str) {
        let a: Vec<u8> = vec![1, 2, 3];
        self.notify_pipeline(env, model_path, a);
    }
}

#[no_mangle]
pub extern "system" fn Java_xyz_xenondevs_renderer_MinecraftModelRenderer_render(
    mut env: JNIEnv<'static>,
    object: JObject,
    models: JObjectArray,
    pipeline: JObject,
) {
    unsafe {
        let renderer = MinecraftModelRenderer::new(&mut env, object, pipeline);
        let vec = string_array_to_vec(&mut env, &models).unwrap();
        for x in vec {
            println!("Rendering model: {}", x);
            renderer.render(&mut env, x.as_str());
        }
    }
}