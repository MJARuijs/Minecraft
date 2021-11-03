#version 450

layout(location = 0) in vec2 position;

uniform sampler2D sampler;
uniform float strength;

out vec2 texCoords[11];

void main() {

    vec2 texCoord = (position + 1.0) / 2.0;
    float height = textureSize(sampler, 0).y;

    for (int y = 0; y < 11; y++) {
        texCoords[y] = texCoord + vec2(0.0, (y - 5.0) / height * strength);
    }

    gl_Position = vec4(position, 0, 1);
}
