#version 450

layout(location = 0) in vec2 inTexCoords;

//uniform vec2 scale;
//uniform vec2 translation;
//uniform float aspectRatio;

out vec2 passTexCoords;

void main() {
    passTexCoords = (inTexCoords + 1.0) / 2.0;
//vec2 position = passTexCoords;
//    vec2 position = translation + scale * inTexCoords.xy;
//    vec2 position = passTexCoords;
//    position.x /= aspectRatio;

    gl_Position = vec4(passTexCoords, 0, 1);
}