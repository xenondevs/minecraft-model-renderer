use jni::sys::jobject;

pub struct ResourceLoader {}

impl ResourceLoader {
    pub fn new(packs: &Vec<jobject>) -> ResourceLoader {
        ResourceLoader {}
    }
}
