#version 460

layout (location = 0) in vec3 inPosition;
layout (location = 1) in int textureIndex;

uniform mat4 projection;
uniform mat4 view;

void main() {
    gl_Position = projection * view * vec4(inPosition, 1.0);
}
