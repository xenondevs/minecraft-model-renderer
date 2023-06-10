use std::str::FromStr;

use na::{OVector, U3, Unit, Vector3};

use crate::util::math::Vector3d;

#[derive(Clone, Eq, PartialEq, Hash)]
pub enum Direction {
    NORTH,
    EAST,
    SOUTH,
    WEST,
    UP,
    DOWN,
}

impl Direction {
    pub fn axis(&self) -> Axis {
        match self {
            Direction::NORTH => Axis::Z,
            Direction::EAST => Axis::X,
            Direction::SOUTH => Axis::Z,
            Direction::WEST => Axis::X,
            Direction::UP => Axis::Y,
            Direction::DOWN => Axis::Y,
        }
    }

    pub fn normal(&self) -> Vector3<f64> {
        match self {
            Direction::NORTH => Vector3::new(0.0, 0.0, -1.0),
            Direction::EAST => Vector3::new(1.0, 0.0, 0.0),
            Direction::SOUTH => Vector3::new(0.0, 0.0, 1.0),
            Direction::WEST => Vector3::new(-1.0, 0.0, 0.0),
            Direction::UP => Vector3::new(0.0, 1.0, 0.0),
            Direction::DOWN => Vector3::new(0.0, -1.0, 0.0),
        }
    }
}

impl FromStr for Direction {
    type Err = String;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s {
            "NORTH" => Ok(Direction::NORTH),
            "EAST" => Ok(Direction::EAST),
            "SOUTH" => Ok(Direction::SOUTH),
            "WEST" => Ok(Direction::WEST),
            "UP" => Ok(Direction::UP),
            "DOWN" => Ok(Direction::DOWN),
            _ => Err(format!("Invalid direction: {}", s))
        }
    }
}

#[derive(Clone)]
pub enum Axis {
    X,
    Y,
    Z,
}

impl Axis {
    pub fn normalized(&self) -> Unit<OVector<f64, U3>> {
        match self {
            Axis::X => Vector3d::x_axis(),
            Axis::Y => Vector3d::y_axis(),
            Axis::Z => Vector3d::z_axis(),
        }
    }
}

impl FromStr for Axis {
    type Err = String;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s {
            "X" => Ok(Axis::X),
            "Y" => Ok(Axis::Y),
            "Z" => Ok(Axis::Z),
            _ => Err(format!("Invalid axis: {}", s))
        }
    }
}

#[derive(Clone)]
pub struct ElementRotation {
    pub origin: Vector3<f64>,
    pub axis: Axis,
    pub angle: f64,
    pub rescale: bool,
}

impl ElementRotation {

    fn offset(self, scale: Vector3d, translation: Vector3d) -> Self {
        Self {
            origin: (self.origin.component_mul(&scale)) + translation,
            ..self
        }
    }

}