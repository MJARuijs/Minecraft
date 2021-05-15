#version 450

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inTextureCoord;
layout(location = 2) in vec3 inNormal;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

out vec2 passTextureCoords;
out vec4 worldPosition;
out vec3 passNormal;

void main() {

    passTextureCoords = inTextureCoord;
    worldPosition = model * vec4(inPosition, 1.0);
    passNormal = mat3(model) * inNormal;

    gl_Position = projection * view * worldPosition;
}
