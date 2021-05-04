#version 460

in vec2 textureCoordinates;
in flat int useOverlayColor;

uniform sampler2D textureMap;
uniform vec4 overlayColor;

out vec4 outColor;

void main() {
    vec4 color = texture(textureMap, textureCoordinates);
    if (useOverlayColor == 1) {
        float strength = color.r;
        color.rgb = overlayColor.rgb * strength;
    }

    outColor = color;
}
