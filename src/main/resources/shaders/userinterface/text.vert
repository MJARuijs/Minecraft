#version 450

layout (location = 0) in vec2 inPosition;
layout (location = 1) in vec2 inTexCoords;

uniform vec2 translation;
uniform float aspectRatio;
uniform float scale;

out vec2 passTexCoords;

void main() {
//    passTexCoords = inTexCoords;
//
//    vec2 position = inPosition* scale;
//    vec2 scaledTranslation = translation;
//    scaledTranslation.x /= aspectRatio;
//    position += scaledTranslation;
//
//    gl_Position = vec4(position, 0.0, 1.0);
    gl_Position = vec4(inPosition + translation * vec2(2.0, -2.0), 0.0, 1.0);
    passTexCoords = inTexCoords;
}
