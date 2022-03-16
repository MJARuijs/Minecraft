#version 450

layout(location = 0) in vec2 inPosition;

uniform vec2 translation;
uniform vec2 scale;
uniform float aspectRatio;

out vec2 passTextureCoordinates;

void main() {
    passTextureCoordinates = (inPosition + 1.0) / 2.0;
    //    passTextureCoordinates.y *= -1.0f;

    vec2 position = translation + scale * inPosition;
    position.x /= aspectRatio;

    gl_Position = vec4(position, 0, 1);
}