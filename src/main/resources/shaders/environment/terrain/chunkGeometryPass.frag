#version 450

struct AmbientLight {
    vec4 color;
};

in vec4 worldPosition;
in vec3 normal;
in vec2 passTextureCoordinates;
in flat int passOverlayColor;

uniform sampler2D textureMap;
uniform vec4 overlayColor;
uniform AmbientLight ambient;

layout (location = 0) out vec4 outPosition;
layout (location = 1) out vec4 outNormal;
layout (location = 2) out vec4 outColor;

void main() {
    outPosition = worldPosition;
    outNormal = vec4(normalize(normal), 1.0);
    vec4 color = texture(textureMap, passTextureCoordinates);
    outColor = color * ambient.color;

    if (passOverlayColor == 1) {
        float strength = outColor.r;
        outColor.rgb = overlayColor.rgb * strength;
    }
}
