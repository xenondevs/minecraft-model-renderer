use std::hash::{Hash, Hasher};

use lazy_static::lazy_static;
use regex::Regex;

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ResourceId {
    namespace: String,
    path: String,
    id: String,
}

lazy_static! {
    static ref NAMESPACED_ENTRY: Regex = Regex::new(r#"^(\w*):([\w/-]*)$"#).unwrap();
    static ref NON_NAMESPACED_ENTRY: Regex = Regex::new(r#"^([\w/-]*)$"#).unwrap();
}

impl ResourceId {
    pub fn new(namespace: &str, path: &str) -> ResourceId {
        ResourceId::new_string(namespace.to_string(), path.to_string())
    }

    pub fn new_string(namespace: String, path: String) -> ResourceId {
        let id = format!("{}:{}", namespace, path);
        Self {
            namespace,
            path,
            id,
        }
    }

    pub fn of(id: &str) -> Result<ResourceId, String> {
        if let Some(captures) = NAMESPACED_ENTRY.captures(id) {
            let namespace = captures.get(1).unwrap().as_str();
            let path = captures.get(2).unwrap().as_str();
            Ok(ResourceId::new(namespace, path))
        } else if let Some(captures) = NON_NAMESPACED_ENTRY.captures(id) {
            let path = captures.get(1).unwrap().as_str();
            Ok(ResourceId::new("minecraft", path))
        } else {
            Err(format!("Invalid resource id: {}", id))
        }
    }
}

impl Hash for ResourceId {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.id.hash(state);
    }
}

impl ToString for ResourceId {
    fn to_string(&self) -> String {
        self.id.clone()
    }
}