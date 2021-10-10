#version 460

layout(location = 0) in vec3 inPosition;
//layout(location = 1) in vec3 inNormal;
//layout(location = 2) in vec2 inTextureCoord;

//uniform mat4 projection;
//uniform mat4 view;
//uniform mat4 model;

out vec3 worldPosition;
//out vec3 passNormal;

void main() {

    worldPosition = inPosition;
//    passNormal = mat3(model) * inNormal;

//    gl_Position = projection * view * worldPosition;
}