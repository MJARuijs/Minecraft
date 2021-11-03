#version 460

struct AmbientLight {
    vec4 color;
};

struct DirectionalLight {
    vec4 color;
    vec3 direction;
};

struct Material {
    vec4 diffuse;
    vec4 specular;
    float shininess;
};

in vec3 passNormal;

uniform Material material;
uniform AmbientLight ambient;
uniform DirectionalLight sun;

out vec4 outColor;

vec4 computeAmbientColor() {
    return material.diffuse * ambient.color;
}

vec4 computeSunColor() {

    // Diffuse
    vec3 normalDirection = normalize(passNormal);
    vec3 lightDirection = normalize(sun.direction);

    float brightness = clamp(dot(lightDirection, normalDirection), 0.0, 1.0);

    vec4 diffuse = material.diffuse * sun.color * brightness;

    return diffuse;
}

void main() {
    vec4 ambientColor = computeAmbientColor();
    vec4 sunColor = computeSunColor();

    outColor = ambientColor + sunColor;
    outColor = clamp(outColor, 0.0, 1.0);
}
