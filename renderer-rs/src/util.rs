use jni::objects::{JObject, JObjectArray, JString};
use jni::sys::jobject;


pub fn string_array_to_vec<'a>(env: &'a mut jni::JNIEnv, array: &JObjectArray<'a>) -> Vec<String> {
    let length = env.get_array_length(array).unwrap();
    let mut vec = Vec::with_capacity(length as usize);
    for i in 0..length {
        let object = env.get_object_array_element(array, i).unwrap();
        let j_string = JString::from(object);
        let string = env.get_string(&j_string).unwrap();
        vec.push(String::from(string.to_str().unwrap()));
    }
    vec
}

pub fn object_array_to_vec<'a>(env: &'a mut jni::JNIEnv, array: &JObjectArray<'a>) -> Vec<jobject> {
    let length = env.get_array_length(array).unwrap();
    let mut vec = Vec::with_capacity(length as usize);
    for i in 0..length {
        let object = env.get_object_array_element(array, i).unwrap();
        vec.push(object.into_raw());
    }
    vec
}