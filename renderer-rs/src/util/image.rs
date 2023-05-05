use image::{DynamicImage, GenericImageView, ImageBuffer, Rgba};

pub fn get_with_uv(image: DynamicImage, from_x: i32, from_y: i32, to_x: i32, to_y: i32) -> DynamicImage {
    let width = (to_x - from_x).abs() as u32;
    let height = (to_y - from_y).abs() as u32;
    let mut new_image = ImageBuffer::new(width.max(1), height.max(1));

    let step_x = if from_x < to_x { 1 } else { -1 };
    let step_y = if from_y < to_y { 1 } else { -1 };
    let mut cur_x = from_x.min(to_x.max(from_x) - 1);
    let mut cur_y = from_y.min(to_y.max(from_y) - 1);

    for x in 0..width {
        for y in 0..height {
            let pixel = image.get_pixel(cur_x as u32, cur_y as u32);
            new_image.put_pixel(x, y, pixel);
            cur_y += step_y;
        }
        cur_x += step_x;
        cur_y = from_y.min(to_y.max(from_y) - 1);
    }

    DynamicImage::ImageRgba8(new_image)
}