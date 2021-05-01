#version 450

struct AmbientLight {
    vec4 color;
};

struct Sun {
    vec4 color;
    vec3 direction;
};

const int samples = 3;
const float samplesPerPixel = (samples * 2.0 + 1.0) * (samples * 2.0 + 1.0);

in vec4 worldPosition;
in vec3 passTextureCoord;
in vec3 passNormal;
in vec3 passInstancePosition;
in vec2 passBreakTextureCoord;
in vec4 shadowCoords;

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
    vec3 normal = normalize(passNormal);

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

vec4 computePointsColor() {

    //    for (int i = 0; i < 2; i++) {
    //
    //    }

    return vec4(0);
}

bool equals(vec3 one, vec3 two) {
    if (abs(one.x - two.x) > 0.0005) {
        return false;
    }

    if (abs(one.y - two.y) > 0.0005) {
        return false;
    }

    if (abs(one.z - two.z) > 0.0005) {
        return false;
    }

    return true;
}

void main() {
    vec4 color = texture(textureMap, passTextureCoord.xy);
    color.rgb *= 0.85;

    if (passTextureCoord.z > 0.5) {
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
            float distanceFromLight = texture(shadowMap, shadowCoords.xy + vec2(x * horizontalPixelSize, y * verticalPixelSize)).r;
            float actualDistance = shadowCoords.z;
            if (actualDistance - 0.0005 > distanceFromLight) {
                shadowValue += 1.5;
            }
        }
    }

    shadowValue /= samplesPerPixel;
    float lightFactor = 1.0 - (shadowValue * shadowCoords.w);

    outColor = ambientColor;
    outColor.rgb += directionalColor.rgb * lightFactor;

    if (breaking && equals(passInstancePosition, breakingPosition)) {
        vec4 breakColor = texture(textureMap, passBreakTextureCoord);
        if (breakColor.a > 0.5) {
            outColor.rgb -= breakColor.rgb * 0.5;
        }
    }

    outColor = clamp(outColor, 0.0, 1.0);
}
