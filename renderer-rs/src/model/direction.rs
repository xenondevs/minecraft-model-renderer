use na::Vector3;

enum Direction {
    NORTH,
    EAST,
    SOUTH,
    WEST,
    UP,
    DOWN
}

impl Direction {

    fn axis(&self) -> Axis {
        match self {
            Direction::NORTH => Axis::Z,
            Direction::EAST => Axis::X,
            Direction::SOUTH => Axis::Z,
            Direction::WEST => Axis::X,
            Direction::UP => Axis::Y,
            Direction::DOWN => Axis::Y,
        }
    }

    fn normal(&self) -> Vector3<f64> {
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

enum Axis {
    X,
    Y,
    Z
}

struct ElementRotation {
    origin: Vector3<f64>,
    axis: Axis,
    angle: f64,
    rescale: bool
}