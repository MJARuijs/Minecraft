#version 450

struct AmbientLight {
    vec4 color;
};

struct Material {
    vec4 diffuse;
    vec4 specular;
    float shininess;
};

in vec4 worldPosition;
in vec3 passNormal;
uniform AmbientLight ambient;
uniform Material material;

layout (location = 0) out vec4 outPosition;
layout (location = 1) out vec4 outNormal;
layout (location = 2) out vec4 outColor;

void main() {
    outPosition = worldPosition;
    outNormal = vec4(normalize(passNormal), 1.0);
    outColor = ambient.color * material.diffuse;
}
