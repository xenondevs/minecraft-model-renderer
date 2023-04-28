extern crate nalgebra as na;

use jni::JNIEnv;
use jni::objects::JClass;

use crate::model::resource::resource_id::ResourceId;

mod model;

static mut JNI_ENV: Option<*mut JNIEnv> = None;

#[allow(non_snake_case)]
pub extern "system" fn Xyz_xenondevs_renderer_Renderer_start(env: JNIEnv, _: JClass) {
    unsafe {
        JNI_ENV = Some(Box::into_raw(Box::new(env)));
    }
}