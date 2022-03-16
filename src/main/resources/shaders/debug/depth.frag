#version 450 core

in vec2 passTextureCoordinates;

uniform sampler2D sampler;
uniform bool linearize;

out vec4 outColor;

float linearizeDepth(float depth) {
    float zNear = 0.01;
    float zFar = 65;
    return 2.0 * zNear * zFar / (zFar + zNear - (2.0 * depth - 1.0) * (zFar - zNear));
}

void main() {
        float depth = texture(sampler, passTextureCoordinates).r;

        if (linearize) {
            float linearDepth = linearizeDepth(depth);
            outColor = vec4(linearDepth, linearDepth, linearDepth, 1.0f);
        } else {
            outColor = vec4(depth, depth, depth, 1.0f);
        }

//    outColor = texture(sampler, passTextureCoordinates);
//    outColor.a = 1.0f;

}
