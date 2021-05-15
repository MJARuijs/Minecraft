#version 450

struct Sun {
    vec4 color;
    vec3 direction;
};

in vec2 passTextureCoordinates;

uniform sampler2D positionMap;
uniform sampler2D normalMap;
uniform sampler2D colorMap;
uniform vec3 cameraPosition;
uniform Sun sun;

out vec4 outColor;

vec4 computeSunColor(vec4 color, vec3 position, vec3 normal) {

    // Diffuse
    vec3 lightDirection = normalize(sun.direction);

    float brightness = clamp(dot(lightDirection, normal), 0.0, 1.0);

    vec4 diffuseColor = color * sun.color * brightness;
    diffuseColor.rgb *= brightness;

    // Specular
    vec3 reflectionVector = 2 * (dot(lightDirection, normal)) * normal - lightDirection;
    vec3 toCameraVector = normalize(cameraPosition - position);

    vec4 specularColor = color * sun.color * clamp(pow(dot(reflectionVector, toCameraVector), 0.0), 0.0, 1.0);
    specularColor.rgb *= brightness;

    return diffuseColor + specularColor;
}

void main() {
    vec3 position = texture(positionMap, passTextureCoordinates).xyz;
    vec3 normal = texture(normalMap, passTextureCoordinates).xyz;
    vec4 ambientColor = texture(colorMap, passTextureCoordinates);

    vec4 sunColor = computeSunColor(ambientColor, position, normal);

    outColor = ambientColor + sunColor;
    outColor = clamp(outColor, 0.0, 1.0);
}