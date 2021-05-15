#version 450

in vec2 passTextureCoords;

uniform sampler2DArray sampler;
//uniform sampler2D sampler;

out vec4 outColor;

void main() {
    outColor = texture(sampler, vec3(passTextureCoords, 0));
//    outColor = texture(sampler, passTextureCoords);
}
