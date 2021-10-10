#version 460

layout (location = 0) in vec3 inPosition;

out vec3 vertexPosition;

void main() {
    vertexPosition = inPosition;
}
