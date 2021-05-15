#version 450 core

uniform sampler2D sampler;

in vec2 passTextureCoordinates;

out vec4 outColor;

float linearizeDepth(float depth) {
    float zNear = 0.01;
    float zFar = 65;
    return 2.0 * zNear * zFar / (zFar + zNear - (2.0 * depth - 1.0) * (zFar - zNear));
//    return depth;
}

void main() {
//    float depth = linearizeDepth(texture(sampler, passTexCoords).x);
//    outColor = vec4(depth, depth, depth, 1.0);
    outColor = texture(sampler, passTextureCoordinates);
//        outColor = vec4(1.0, 0.0, 0.0, 1.0);

}
