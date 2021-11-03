#version 450

float weights[11] = {
    0.0093,
    0.028002,
    0.065984,
    0.121703,
    0.175713,
    0.198596,
    0.175713,
    0.121703,
    0.065984,
    0.028002,
    0.0093
};

in vec2 texCoords[11];

uniform sampler2D sampler;

out vec4 color;

void main() {

    vec4 sum = vec4(0.0);

    for (int i = 0; i < 11; i++) {
        sum += weights[i] * texture(sampler, texCoords[i]);
    }

    color = clamp(max(sum, texture(sampler, texCoords[6])), 0.0, 1.0);
//    color = vec4(1,0,0,1);
//    color = texture(sampler, texCoords[6]);
}