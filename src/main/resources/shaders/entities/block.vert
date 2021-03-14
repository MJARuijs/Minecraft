#version 450

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inTextureCoord;
layout(location = 2) in vec3 inNormal;
layout(location = 3) in float instanceTextureId;
layout(location = 4) in vec3 instancePosition;

uniform mat4 projection;
uniform mat4 view;
uniform int chunkHeight;
uniform int chunkSize;
uniform vec3 place;

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

    model[3][0] = place.x + instancePosition.x; // X
    model[3][1] = place.y + instancePosition.y; // Y
    model[3][2] = place.z + instancePosition.z; // Z

    passNormal = mat3(model) * inNormal;
    worldPosition = model * vec4(inPosition, 1.0);
    gl_Position = projection * view * worldPosition;
}
