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
in vec4 worldPosition;
in vec4 passShadowCoords;
in vec2 passTextureCoordinates;
in vec3 normal;

uniform AmbientLight ambient;
uniform Sun sun;

uniform vec3 cameraPosition;
uniform vec2 shadowMapSize;
uniform sampler2D textureMap;
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

vec4 computeDirectionalColor(vec4 color) {

    // Diffuse
    vec3 lightDirection = normalize(sun.direction);

    float brightness = clamp(dot(lightDirection, normal), 0.0, 1.0);

    vec4 diffuseColor = color * sun.color * brightness;
    diffuseColor.rgb *= brightness;

    // Specular
    vec3 position = worldPosition.xyz;
    vec3 reflectionVector = 2 * (dot(lightDirection, normal)) * normal - lightDirection;
    vec3 toCameraVector = normalize(cameraPosition - position);

    vec4 specularColor = color * sun.color * clamp(pow(dot(reflectionVector, toCameraVector), 0.0), 0.0, 1.0);
    specularColor.rgb *= brightness;

    return diffuseColor + specularColor;
}

void main() {
    vec4 color = texture(textureMap, passTextureCoordinates);
    color.rgb *= 0.85;

    if (passOverlayColor == 1) {
        float strength = color.r;
        color.rgb = overlayColor.rgb * strength;
    }

    vec4 ambientColor = computeAmbientColor(color);
    vec4 directionalColor = computeDirectionalColor(color);

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
    outColor.rgb += directionalColor.rgb * lightFactor;
    outColor = clamp(outColor, 0.0, 1.0);
}
