#version 450

struct AmbientLight {
    vec4 color;
};

struct DirectionalLight {
    vec4 color;
    vec3 direction;
};

struct PointLight {
    vec4 color;
    vec3 position;
};

struct Material {
    vec4 diffuse;
    vec4 specular;
    float shininess;
};

in vec4 worldPosition;
in vec2 passTextureCoord;
in vec3 passNormal;
//in flat int passTextureId;

uniform AmbientLight ambient;
uniform DirectionalLight sun;
uniform PointLight pointlights[2];
uniform Material material;
uniform vec3 cameraPosition;
uniform sampler2D blockTextures[2];

out vec4 outColor;

vec4 computeAmbientColor() {

    vec4 textureColor = texture(blockTextures[1], passTextureCoord);
    return textureColor;
}

vec4 computeDirectionalColor() {

    // Diffuse
    vec3 lightDirection = normalize(sun.direction);
    vec3 normal = normalize(passNormal);

    float brightness = clamp(dot(lightDirection, normal), 0.0, 1.0);

    vec4 diffuseColor = brightness * material.diffuse * sun.color;

    // Specular
    vec3 position = worldPosition.xyz;
    vec3 reflectionVector = 2 * (dot(lightDirection, normal)) * normal - lightDirection;
    vec3 toCameraVector = normalize(cameraPosition - position);

    vec4 specularColor = material.specular * sun.color * clamp(pow(dot(reflectionVector, toCameraVector), material.shininess), 0.0, 1.0);

    return diffuseColor;
}

vec4 computePointsColor() {

    //    for (int i = 0; i < 2; i++) {
    //
    //    }

    return vec4(0);
}

void main() {
    vec4 ambientColor = computeAmbientColor();
//    vec4 directionalColor = computeDirectionalColor();
//    vec4 pointColor = computePointsColor();

    outColor = ambientColor;
    outColor = clamp(outColor, 0.0, 1.0);}
