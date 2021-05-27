#version 460

struct Sun {
    vec4 color;
    vec3 direction;
};

const int samples = 3;
const float samplesPerPixel = (samples * 2.0 + 1.0) * (samples * 2.0 + 1.0);

in vec2 passTextureCoordinates;

uniform sampler2DMS positionMap;
uniform sampler2DMS surfaceNormalMap;
uniform sampler2DMS colorMap;
uniform sampler2DMS normalMap;
uniform sampler2DMS specularMap;
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
    vec3 position = vec3(0);
    vec3 surfaceNormal = vec3(0);
    vec4 color = vec4(0);
    vec3 normal = vec3(0);
    float specularStrength = 0.0;

//    position = texture(positionMap, passTextureCoordinates).xyz;
//    surfaceNormal = texture(surfaceNormalMap, passTextureCoordinates).xyz;
//    color = texture(colorMap, passTextureCoordinates);
//    normal = normalize(2.0 * texture(normalMap, passTextureCoordinates).xyz - 1.0);
//    specularStrength = texture(specularMap, passTextureCoordinates).r;

    float msaaSamples = 1.0;

    for (int i = 0; i < int(msaaSamples); i++) {
        position += texelFetch(positionMap, ivec2(gl_FragCoord.xy), i).xyz;
        surfaceNormal += texelFetch(surfaceNormalMap, ivec2(gl_FragCoord.xy), i).xyz;
        color += texelFetch(colorMap, ivec2(gl_FragCoord.xy), i);
        normal += normalize(2.0 * texelFetch(normalMap, ivec2(gl_FragCoord.xy), i).xyz - 1.0);
        specularStrength += texelFetch(specularMap, ivec2(gl_FragCoord.xy), i).r;
    }

    position /= msaaSamples;
    surfaceNormal /= msaaSamples;
    color /= msaaSamples;
    normal /= msaaSamples;
    specularStrength /= msaaSamples;

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
    outColor = clamp(outColor, 0.0, 1.0);
}