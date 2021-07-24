#version 450

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec3 inNormal;
layout(location = 2) in vec2 inTextureCoordinates;
layout(location = 3) in vec4 inBoneWeights;
layout(location = 4) in ivec4 inBoneIds;

const int MAX_BONES = 100;
const int MAX_BONES_PER_VERTEX = 4;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;
uniform mat4 boneMatrices[MAX_BONES];

void main() {
    vec4 position = vec4(0.0);

    for (int i = 0; i < MAX_BONES_PER_VERTEX; i++) {
        vec4 unscaledPosition = boneMatrices[int(inBoneIds[0])] * vec4(inPosition, 1.0);
        position += unscaledPosition * inBoneWeights[0];
    }

    gl_Position = projection * view * model * position;
}
