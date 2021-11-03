#version 450

layout(location = 0) in vec2 position;

uniform sampler2D sampler;
uniform float strength;

out vec2 texCoords[11];

void main() {

    vec2 texCoord = (position + 1.0) / 2.0;
    float width = textureSize(sampler, 0).x;

    for (int x = 0; x < 11; x++) {
        texCoords[x] = texCoord + vec2((x - 5.0) / width * strength, 0.0);
    }

    gl_Position = vec4(position, 0, 1);
}
