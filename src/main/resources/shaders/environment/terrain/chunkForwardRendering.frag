#version 460

struct AmbientLight {
    vec4 color;
};

struct Sun {
    vec4 color;
    vec3 direction;
};

const int samples = 3;
const float samplesPerPixel = (samples * 2.0 + 1.0) * (samples * 2.0 + 1.0);

in flat int passOverlayColor;
in flat int textureIndex;
in flat int normalIndex;
in flat int specularIndex;
in vec4 worldPosition;
in vec4 passShadowCoords;
in vec2 passTextureCoordinates;
in vec3 normal;
in vec3 lightDirection;
in vec3 tangentToCameraVector;
in mat3 toTangentSpace;

uniform AmbientLight ambient;
uniform Sun sun;

uniform vec3 cameraPosition;
uniform vec2 shadowMapSize;
uniform sampler2DArray textureMap;
uniform sampler2DArray normalMap;
uniform sampler2DArray specularMap;
uniform sampler2D shadowMap;

uniform vec3 selectedBlockPosition;
uniform vec3 breakingPosition;

uniform bool breaking;
uniform bool selected;
uniform vec4 overlayColor;

out vec4 outColor;

vec4 computeAmbientColor(vec4 color) {
    return color * ambient.color;
}

vec4 computeSunColor(vec4 color, vec3 normal, float specularStrength) {

    // Diffuse
    float brightness = clamp(dot(lightDirection, normal), 0.0, 1.0);

    if (brightness <= 0) {
        return vec4(0, 0, 0, 1);
    }

    vec4 diffuseColor = color * sun.color;
    diffuseColor.rgb *= brightness;

    // Specular
    vec3 reflectionVector = 2 * (dot(lightDirection, normal)) * normal - lightDirection;

    float specularFactor = dot(reflectionVector, tangentToCameraVector);
    specularFactor = max(specularFactor, 0.0);

    float dampedFactor = pow(specularFactor, 20.0) * 16;

    vec4 specularColor = color * sun.color;
    specularColor.rgb *= dampedFactor * specularStrength;

    return diffuseColor + specularColor;
}

void main() {
    vec4 color = texture(textureMap, vec3(passTextureCoordinates, textureIndex));
    color.rgb *= 0.85;

    if (passOverlayColor == 1) {
        float strength = color.r;
        color.rgb = overlayColor.rgb * strength;
    }

    vec3 normal = normalize(normal);
    if (normalIndex != -1) {
        normal = normalize(2.0 * texture(normalMap, vec3(passTextureCoordinates, normalIndex)).xyz - 1.0);
    }

    float specularStrength = 0.0;
    if (specularIndex != -1) {
        specularStrength = texture(specularMap, vec3(passTextureCoordinates, specularIndex)).r;
    }

    vec4 ambientColor = computeAmbientColor(color);
    vec4 sunColor = computeSunColor(color, normal, specularStrength);

    float horizontalPixelSize = 1.0 / shadowMapSize.x;
    float verticalPixelSize = 1.0 / shadowMapSize.y;
    float shadowValue = 0.0;

    for (int x = -samples; x < samples; x++) {
        for (int y = -samples; y < samples; y++) {
            float distanceFromLight = texture(shadowMap, passShadowCoords.xy + vec2(x * horizontalPixelSize, y * verticalPixelSize)).r;
            float actualDistance = passShadowCoords.z;
            if (actualDistance - 0.0002 > distanceFromLight) {
                shadowValue += 1.5;
            }
        }
    }

    shadowValue /= samplesPerPixel;
    float lightFactor = 1.0 - (shadowValue * passShadowCoords.w);

    outColor = ambientColor;
    outColor.rgb += sunColor.rgb * lightFactor;
    outColor = clamp(outColor, 0.0, 1.0);
}
