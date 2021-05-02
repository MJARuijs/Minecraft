#version 460

layout (location = 0) in vec3 inPosition;
layout (location = 1) in float textureIndex;

uniform mat4 projection;
uniform mat4 view;

out float passTextureIndex;

void main() {
    passTextureIndex = textureIndex;
    gl_Position = projection * view * vec4(inPosition, 1.0);
}
