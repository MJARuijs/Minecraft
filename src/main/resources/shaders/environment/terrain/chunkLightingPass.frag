#version 460

struct Sun {
    vec4 color;
    vec3 direction;
};

const int samples = 3;
const float samplesPerPixel = (samples * 2.0 + 1.0) * (samples * 2.0 + 1.0);

in vec2 passTextureCoordinates;

uniform sampler2D positionMap;
uniform sampler2D surfaceNormal;
uniform sampler2D colorMap;
uniform sampler2D normalMap;
uniform sampler2D specularMap;
uniform sampler2D shadowCoordinatesMap;
uniform sampler2D shadowMap;

uniform vec3 cameraPosition;
uniform vec2 shadowMapSize;

uniform Sun sun;

out vec4 outColor;

vec4 computeSunColor(vec4 color, vec3 toCameraVector, vec3 normal, vec3 lightDirection, float specularStrength) {

    // Diffuse
    float brightness = clamp(dot(lightDirection, normal), 0.0, 1.0);

    if (brightness <= 0) {
        return vec4(0,0,0,1);
    }

    vec4 diffuseColor = color * sun.color;
    diffuseColor.rgb *= brightness;

    // Specular
    vec3 reflectionVector = 2 * (dot(lightDirection, normal)) * normal - lightDirection;

    float specularFactor = dot(reflectionVector, toCameraVector);
    specularFactor = max(specularFactor, 0.0);

    float dampedFactor = pow(specularFactor, 20.0) * 16;

    vec4 specularColor = color * sun.color;
    specularColor.rgb *= dampedFactor * specularStrength;

    return diffuseColor + specularColor;
}

vec3 computeTangent(vec3 normal) {
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
    return tangent;
}

void main() {
    vec3 position = texture(positionMap, passTextureCoordinates).xyz;
    vec3 surfaceNormal = texture(surfaceNormal, passTextureCoordinates).xyz;
    vec4 color = texture(colorMap, passTextureCoordinates);
    vec3 normal = normalize(2.0 * texture(normalMap, passTextureCoordinates).xyz - 1.0);

    float specularStrength = texture(specularMap, passTextureCoordinates).r;

    vec3 tangent = computeTangent(surfaceNormal);
    vec3 biTangent = normalize(cross(surfaceNormal, tangent));

    mat3 toTangentSpace = mat3(
        tangent.x, biTangent.x, surfaceNormal.x,
        tangent.y, biTangent.y, surfaceNormal.y,
        tangent.z, biTangent.z, surfaceNormal.z
    );

    vec3 lightDirection = normalize(toTangentSpace * normalize(sun.direction));
    vec3 toCameraVector = normalize(toTangentSpace * normalize(cameraPosition - position));

    vec4 sunColor = computeSunColor(color, toCameraVector, normal, lightDirection, specularStrength);

    float horizontalPixelSize = 1.0 / shadowMapSize.x;
    float verticalPixelSize = 1.0 / shadowMapSize.y;
    float shadowValue = 0.0;

    vec4 shadowCoordinates = texture(shadowCoordinatesMap, passTextureCoordinates);

    for (int x = -samples; x < samples; x++) {
        for (int y = -samples; y < samples; y++) {
            float distanceFromLight = texture(shadowMap, shadowCoordinates.xy + vec2(x * horizontalPixelSize, y * verticalPixelSize)).r;
            float actualDistance = shadowCoordinates.z;
            if (actualDistance - 0.0005 > distanceFromLight) {
                shadowValue += 1.5;
            }
        }
    }

    shadowValue /= samplesPerPixel;
    float lightFactor = 1.0 - (shadowValue * shadowCoordinates.w);

    outColor = color;
    outColor.rgb += sunColor.rgb * lightFactor;
//    outColor = shadowCoordinates;
    outColor = clamp(outColor, 0.0, 1.0);
}