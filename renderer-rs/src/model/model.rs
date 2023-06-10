use std::collections::HashMap;

use image::DynamicImage;

use crate::model::direction::{Direction, ElementRotation};
use crate::util::math::{Matrix4d, Vector3d};

pub struct Element {
    pub from: Vector3d,
    pub to: Vector3d,
    pub rotation: Option<ElementRotation>,
    pub faces: HashMap<Direction, DynamicImage>,
}

impl Element {

    pub fn to_model_matrix(&self) -> Matrix4d {
        let center = (self.from + self.to) * 0.5;
        let size = self.to - self.from;

        let scale = Matrix4d::new_nonuniform_scaling(&Vector3d::new(size.x, size.y, size.z));
        let translation = Matrix4d::new_translation(&Vector3d::new(center.x, center.y, center.z));
        let rotate = match &self.rotation {
            Some(rotation) => {
                Matrix4d::from_axis_angle(&rotation.axis.normalized(), rotation.angle)
            }
            None => Matrix4d::identity()
        };

        translation * rotate * scale
    }

}

pub enum Model {
    Geometric(GeometricModel),
    Layered(LayeredModel),
}

pub struct GeometricModel {
    pub elements: Vec<Element>,
    pub ambient_occlusion: bool,
    pub rotation: Vector3d,
    pub translation: Vector3d,
    pub scale: Vector3d,
}

pub struct LayeredModel {
    pub layers: Vec<GeometricModel>,
}