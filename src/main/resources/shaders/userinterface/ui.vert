#version 450 core

layout(location = 0) in vec2 inPosition;

uniform vec2 translation;
uniform vec2 scale;
uniform float aspectRatio;

out vec2 position;
out vec2 passTexCoords;

void main() {
    passTexCoords = (inPosition + 1.0) / 2.0;
    passTexCoords.y *= -1.0f;

    position = translation + scale * inPosition.xy;
    position.x /= aspectRatio;

    gl_Position = vec4(position, 0, 1);
}
