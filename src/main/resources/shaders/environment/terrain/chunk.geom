#version 460

struct Sun {
    vec4 color;
    vec3 direction;
};

layout (triangles) in;
layout (triangle_strip, max_vertices = 3) out;

const float transitionDistance = 10.0f;

in flat int useOverlayColor[3];
in flat int passTextureIndex[3];
in flat int passNormalIndex[3];
in flat int passSpecularIndex[3];
in vec3 position[3];
in vec2 textureCoordinates[3];

uniform Sun sun;

uniform mat4 projection;
uniform mat4 view;

uniform mat4 shadowMatrix;
uniform vec3 cameraPosition;
uniform float shadowDistance;

out flat int passOverlayColor;
out flat int textureIndex;
out flat int normalIndex;
out flat int specularIndex;
out vec4 passShadowCoords;
out vec4 worldPosition;
out vec2 passTextureCoordinates;
out vec3 normal;
out vec3 lightDirection;
out vec3 tangentToCameraVector;

out mat3 toTangentSpace;

void main() {

    normal = normalize(cross(position[1] - position[0], position[2] - position[0]));

    vec3 tangent;
    if (normal.x > 0.99) {
        tangent = vec3(0, 0, 1);
    }
    if (normal.x < -0.99) {
        tangent = vec3(0, 0, -1);
    }
    if (normal.y > 0.99) {
        tangent = vec3(1, 0, 0);
    }
    if (normal.y < -0.99) {
        tangent = vec3(-1, 0, 0);
    }
    if (normal.z > 0.99) {
        tangent = vec3(1, 0, 0);
    }
    if (normal.z < -0.99) {
        tangent = vec3(-1, 0, 0);
    }

    tangent = normalize((vec4(tangent, 0.0)).xyz);
    vec3 biTangent = normalize(cross(normal, tangent));

    toTangentSpace = mat3(
        tangent.x, biTangent.x, normal.x,
        tangent.y, biTangent.y, normal.y,
        tangent.z, biTangent.z, normal.z
    );

    lightDirection = normalize(toTangentSpace * normalize(sun.direction));

    for (int i = 0; i < 3; i++) {
        passOverlayColor = useOverlayColor[i];
        textureIndex = passTextureIndex[i];
        normalIndex = passNormalIndex[i];
        specularIndex = passSpecularIndex[i];
        passTextureCoordinates = textureCoordinates[i];
        worldPosition = vec4(position[i], 1.0);
        tangentToCameraVector = normalize(toTangentSpace * normalize(cameraPosition - worldPosition.xyz));

        passShadowCoords = shadowMatrix * worldPosition;
        vec3 toCameraVector = cameraPosition - worldPosition.xyz;
        float toCameraDistance = length(toCameraVector);
        float distance = toCameraDistance - (shadowDistance - transitionDistance);
        distance /= transitionDistance;
        passShadowCoords.w = clamp(1.0 - distance, 0.0, 1.0);
        passShadowCoords.w = 1.0;

        gl_Position = projection * view * worldPosition;

        EmitVertex();
    }

    EndPrimitive();

}