#version 460

struct AmbientLight {
    vec4 color;
};

in vec4 worldPosition;
in vec3 normal;
in vec2 passTextureCoordinates;
in vec4 passShadowCoords;

in flat int textureIndex;
in flat int normalIndex;
in flat int specularIndex;
in flat int passOverlayColor;

uniform sampler2DArray textureMap;
uniform sampler2DArray normalMap;
uniform sampler2DArray specularMap;

uniform vec4 overlayColor;
uniform AmbientLight ambient;

layout (location = 0) out vec4 outPosition;
layout (location = 1) out vec4 outSurfaceNormal;
layout (location = 2) out vec4 outColor;
layout (location = 3) out vec4 outNormal;
layout (location = 4) out vec4 outSpecular;
layout (location = 5) out vec4 outShadowCoordinates;

void main() {
    outPosition = worldPosition;

    vec4 color = texture(textureMap, vec3(passTextureCoordinates, textureIndex));
    outColor = color * ambient.color;

    outSurfaceNormal = vec4(normal, 1.0);
    if (normalIndex == -1) {
        outNormal = vec4(0, 0, 0, 1);
    } else {
        outNormal.rgb = texture(normalMap, vec3(passTextureCoordinates, normalIndex)).rgb;
        outNormal.a = 1.0;
    }

    vec4 specularStrength = vec4(0, 0, 0, 1);
    if (specularIndex != -1) {
        specularStrength.r = texture(specularMap, vec3(passTextureCoordinates, specularIndex)).r;
    }

    outSpecular = specularStrength;

    if (passOverlayColor == 1) {
        float strength = outColor.r;
        outColor.rgb = overlayColor.rgb * strength;
    }

    outShadowCoordinates = vec4(0,0,0,1);
    outShadowCoordinates.rgb = passShadowCoords.rgb;
    outShadowCoordinates.a = 1.0;
}
