use jni::JNIEnv;
use jni::objects::{JObjectArray, JString};
use jni::sys::jobject;

pub fn string_array_to_vec<'a>(
    env: &mut JNIEnv,
    array: &JObjectArray<'a>,
) -> Result<Vec<String>, jni::errors::Error> {
    let length = env.get_array_length(array)?;
    let vec = (0..length)
        .map(|i| {
            let object = env.get_object_array_element(array, i)?;
            let j_string = JString::from(object);
            let string = env.get_string(&j_string)?;
            Ok(String::from(string.to_str().unwrap()))
        }).collect::<Result<Vec<String>, jni::errors::Error>>()?;

    Ok(vec)
}

pub fn object_array_to_vec<'a>(
    env: &mut JNIEnv,
    array: &JObjectArray<'a>,
) -> Result<Vec<jobject>, jni::errors::Error> {
    let length = env.get_array_length(array)?;
    let vec = (0..length)
        .map(|i| {
            let object = env.get_object_array_element(array, i)?;
            Ok(object.into_raw())
        }).collect::<Result<Vec<jobject>, jni::errors::Error>>()?;
    Ok(vec)
}

pub fn copy_jni_env<'a>(env: &'a JNIEnv) -> JNIEnv<'a> {
    unsafe { env.unsafe_clone() }
}