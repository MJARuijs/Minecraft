#version 450

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inTextureCoord;
layout(location = 2) in vec3 inNormal;
layout(location = 3) in vec3 instancePosition;
layout(location = 4) in float instanceTextureId;

uniform mat4 projection;
uniform mat4 view;

out vec4 worldPosition;
out vec4 shadowCoords;
out vec2 passTextureCoord;
out vec3 passNormal;
out int passTextureId;

void main() {
    passTextureCoord = inTextureCoord;
    passTextureId = int(instanceTextureId);
    mat4 model;
    model[0][0] = 1;
    model[1][1] = 1;
    model[2][2] = 1;
    model[3][3] = 1;
    model[3][0] = instancePosition[0];
    model[3][1] = instancePosition[1];
    model[3][2] = instancePosition[2];
    passNormal = mat3(model) * inNormal;

    worldPosition = model * vec4(inPosition, 1.0);

    gl_Position = projection * view * worldPosition;
}
