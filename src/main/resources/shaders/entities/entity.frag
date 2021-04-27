#version 450


struct AmbientLight {
    vec4 color;
};

struct DirectionalLight {
    vec4 color;
    vec3 direction;
};

struct Material {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float shininess;
};

in vec4 worldPosition;
in vec3 passNormal;

//uniform AmbientLight ambient;
//uniform DirectionalLight sun;
//uniform Material material;
//
//uniform vec3 cameraPosition;
//
out vec4 outColor;
//
//vec4 computeAmbientColor() {
//    return ambient.color * material.ambient;
//}
//
//vec4 computeDirectionalColor(vec3 lightDirection) {
//
//    // Diffuse
//    vec3 normalDirection = normalize(passNormal);
//    lightDirection = normalize(lightDirection);
//
//    float brightness = clamp(dot(lightDirection, normalDirection), 0.0, 1.0);
//
//    vec4 diffuse = material.diffuse * sun.color * brightness;
//
//    // Specular
//    vec3 position = worldPosition.xyz;
//    vec3 reflectionVector = 2 * (dot(lightDirection, normalDirection)) * normalDirection - lightDirection;
//    vec3 toCameraVector = normalize(cameraPosition - position);
//
//    vec4 specular = material.specular * sun.color * clamp(pow(dot(reflectionVector, toCameraVector), material.shininess), 0.0, 1.0);
//
//    return diffuse + specular;
//}

void main() {
//    vec4 ambientColor = computeAmbientColor();
//    vec4 sunColor = computeDirectionalColor(sun.direction) + computeDirectionalColor(vec3(-1.0));

    outColor = vec4(0.5, 0.5, 0.5, 1.0);
}