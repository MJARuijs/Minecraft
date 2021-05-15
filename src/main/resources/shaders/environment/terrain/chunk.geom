#version 460

layout (triangles) in;
layout (triangle_strip, max_vertices = 3) out;

const float transitionDistance = 10.0f;

in flat int useOverlayColor[3];
in vec3 position[3];
in vec2 textureCoordinates[3];

uniform mat4 projection;
uniform mat4 view;
//uniform mat4 shadowMatrix;
//uniform vec3 cameraPosition;
//uniform float shadowDistance;

out flat int passOverlayColor;
out vec4 passShadowCoords;
out vec4 worldPosition;
out vec2 passTextureCoordinates;
out vec3 normal;

void main() {

    normal = normalize(cross(position[1] - position[0], position[2] - position[0]));
    for (int i = 0; i < 3; i++) {
        passOverlayColor = useOverlayColor[i];
        passTextureCoordinates = textureCoordinates[i];
        worldPosition = vec4(position[i], 1.0);

//        passShadowCoords = shadowMatrix * worldPosition;
//        vec3 toCameraVector = cameraPosition - worldPosition.xyz;
//        float toCameraDistance = length(toCameraVector);
//        float distance = toCameraDistance - (shadowDistance - transitionDistance);
//        distance /= transitionDistance;
//        passShadowCoords.w = clamp(1.0 - distance, 0.0, 1.0);

        gl_Position = projection * view * worldPosition;

        EmitVertex();
    }
    EndPrimitive();

}