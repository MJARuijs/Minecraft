#version 450

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec3 instancePosition;

uniform mat4 projection;
uniform mat4 view;

void main() {
    mat4 model;
    model[0][0] = 1;
    model[1][1] = 1;
    model[2][2] = 1;
    model[3][3] = 1;

    model[3][0] = instancePosition.x;
    model[3][1] = instancePosition.y;
    model[3][2] = instancePosition.z;

    vec4 worldPosition = model * vec4(inPosition, 1.0);

    gl_Position = projection * view * worldPosition;
}
