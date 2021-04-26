#version 450 core

layout(location = 0) in vec2 inPosition;

uniform vec2 translation;
uniform vec2 scale;

out vec2 passTexCoords;

void main() {
    passTexCoords = (inPosition + 1.0) / 2.0;
//    passTexCoords.y = 1.0 - passTexCoords.y;

    vec2 position = (translation + scale * inPosition.xy);

    gl_Position = vec4(position, 0, 1);
}
