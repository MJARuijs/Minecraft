#version 450

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inTextureCoord;
layout(location = 2) in vec3 inNormal;
layout(location = 3) in vec3 instancePosition;
layout(location = 4) in vec2 frontTextureOffset;
layout(location = 5) in vec2 backTextureOffset;
layout(location = 6) in vec2 rightTextureOffset;
layout(location = 7) in vec2 leftTextureOffset;
layout(location = 8) in vec2 topTextureOffset;
layout(location = 9) in vec2 bottomTextureOffset;

uniform mat4 projection;
uniform mat4 view;
//uniform int chunkHeight;
//uniform int chunkSize;
//uniform vec3 place;

out vec4 worldPosition;
out vec4 shadowCoords;
out vec2 passTextureCoord;
out vec3 passNormal;

//out vec2 textureOffset;

void main() {
    mat4 model;
    model[0][0] = 1;
    model[1][1] = 1;
    model[2][2] = 1;
    model[3][3] = 1;

    model[3][0] = instancePosition.x;
    model[3][1] = instancePosition.y;
    model[3][2] = instancePosition.z;

    passNormal = normalize(mat3(model) * inNormal);
    passTextureCoord = (inTextureCoord / 16.0);

    if (passNormal.x == 1.0) {
        passTextureCoord += rightTextureOffset;
    } else if (passNormal.x == -1.0) {
        passTextureCoord += leftTextureOffset;
    } else if (passNormal.y == 1.0) {
        passTextureCoord += topTextureOffset;
    } else if (passNormal.y == -1.0) {
        passTextureCoord += bottomTextureOffset;
    } else if (passNormal.z == 1.0) {
        passTextureCoord += frontTextureOffset;
    } else if (passNormal.z == -1.0) {
        passTextureCoord += backTextureOffset;
    } else {
        passTextureCoord = vec2(0.0, 0.0);
    }

    worldPosition = model * vec4(inPosition, 1.0);
    gl_Position = projection * view * worldPosition;
}
