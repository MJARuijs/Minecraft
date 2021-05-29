#version 460

layout (location = 0) in vec3 inPosition;
//layout (location = 1) in vec3 inNormal;
//layout (location = 2) in vec2 inTextureCoordinates;

const float transitionDistance = 0.0;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

uniform mat4 shadowMatrix;
uniform vec3 cameraPosition;
uniform float shadowDistance;

out vec4 worldPosition;
out vec3 passNormal;
out vec4 shadowCoords;

void main() {
    worldPosition = model * vec4(inPosition, 1.0);
    passNormal = mat3(model) * vec3(0,0,0);
    shadowCoords = shadowMatrix * worldPosition;

    vec3 toCameraVector = cameraPosition - worldPosition.xyz;
    float toCameraDistance = length(toCameraVector);
    float distance = toCameraDistance - (shadowDistance - transitionDistance);
    distance /= transitionDistance;
    shadowCoords.w = clamp(1.0 - distance, 0.0, 1.0);

    gl_Position = projection * view * worldPosition;
}