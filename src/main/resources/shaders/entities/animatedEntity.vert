#version 460

layout (location = 0) in vec3 inPosition;
layout (location = 1) in vec3 inNormal;
//layout (location = 2) in vec2 inTextureCoordinates;
layout(location = 2) in vec4 inBoneIds;
layout(location = 3) in vec4 inBoneWeights;

const float transitionDistance = 0.0;
const int MAX_BONES = 100;
const int MAX_BONES_PER_VERTEX = 4;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;
uniform mat4 boneMatrices[MAX_BONES];

uniform mat4 shadowMatrix;
uniform vec3 cameraPosition;
uniform float shadowDistance;

out vec4 worldPosition;
out vec3 passNormal;
out vec4 shadowCoords;

void main() {

    vec4 position = vec4(0.0);
    vec3 normal = vec3(0.0);

//    for (int i = 0; i < MAX_BONES_PER_VERTEX; i++) {
        vec4 unscaledPosition = boneMatrices[int(inBoneIds[0])] * vec4(inPosition, 1.0);
        position += unscaledPosition * inBoneWeights[0];

        vec3 unscaledNormal = mat3(boneMatrices[int(inBoneIds[0])]) * inNormal;
        normal += unscaledNormal * inBoneWeights[0];
//    }

    worldPosition = model * position;
    passNormal = mat3(model) * normal;

    shadowCoords = shadowMatrix * worldPosition;

    vec3 toCameraVector = cameraPosition - worldPosition.xyz;
    float toCameraDistance = length(toCameraVector);
    float distance = toCameraDistance - (shadowDistance - transitionDistance);
    distance /= transitionDistance;
    shadowCoords.w = clamp(1.0 - distance, 0.0, 1.0);

    gl_Position = projection * view * worldPosition;
}
