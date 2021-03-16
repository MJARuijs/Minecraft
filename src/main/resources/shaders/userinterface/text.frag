#version 450

in vec2 passTexCoords;

uniform sampler2D textureAtlas;
uniform vec4 color;

out vec4 outColor;
const float characterWidth = 0.55f;
const float edgeWidth = 0.1f;
void main() {
    float distance = 1.0f - texture(textureAtlas, passTexCoords).a;
    float alpha = 1.0f - smoothstep(characterWidth, characterWidth + edgeWidth, distance);
    outColor = vec4(color.rgb, alpha);
//    outColor = vec4(color.rgb, texture(textureAtlas, passTexCoords).a);
}
