#version 450

in vec3 passPosition;

uniform samplerCube cubeMap;

out vec4 outColor;

void main() {
    outColor = texture(cubeMap, passPosition);
}
