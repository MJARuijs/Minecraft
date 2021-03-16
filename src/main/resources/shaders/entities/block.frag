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

in vec4 worldPosition;
in vec2 passTextureCoord;
in vec3 passNormal;
in vec3 passInstancePosition;

uniform AmbientLight ambient;
uniform DirectionalLight directional;
uniform PointLight pointlights[2];
uniform vec3 cameraPosition;
uniform sampler2D textureMap;
uniform vec3 selectedBlockPosition;
uniform bool selected;

out vec4 outColor;

vec4 computeAmbientColor() {
    return texture(textureMap, passTextureCoord);
}

vec4 computeDirectionalColor() {
    vec4 textureColor = texture(textureMap, passTextureCoord);

    // Diffuse
    vec3 lightDirection = normalize(directional.direction);
    vec3 normal = normalize(passNormal);

    float brightness = clamp(dot(lightDirection, normal), 0.0, 1.0);

    vec4 diffuseColor = brightness * textureColor * directional.color;

    // Specular
    vec3 position = worldPosition.xyz;
    vec3 reflectionVector = 2 * (dot(lightDirection, normal)) * normal - lightDirection;
    vec3 toCameraVector = normalize(cameraPosition - position);

    vec4 specularColor = textureColor * directional.color * clamp(pow(dot(reflectionVector, toCameraVector), 0.0), 0.0, 1.0);

    return diffuseColor;
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
    if (selected && equals(passInstancePosition, selectedBlockPosition)) {
        outColor = vec4(passNormal, 1.0);
    } else {
        vec4 ambientColor = computeAmbientColor();
        vec4 directionalColor = computeDirectionalColor();
        outColor = ambientColor + directionalColor;
        outColor = clamp(outColor, 0.0, 1.0);

    }
}
