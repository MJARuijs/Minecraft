#version 450

layout (location = 0) in vec2 inPosition;
layout (location = 1) in vec2 inTexCoords;

uniform vec2 translation;
uniform float aspectRatio;
uniform float scale;

out vec2 passTexCoords;

void main() {

    gl_Position = vec4(inPosition + vec2(1, -1) + translation * vec2(1.0, -1.0), 0.0, 1.0);
    passTexCoords = inTexCoords;
}
