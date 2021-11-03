#version 460

layout(location = 0) in vec2 inPosition;

out vec2 textureCoordinates;

void main() {
    textureCoordinates = (inPosition + 1.0) / 2.0;

    gl_Position = vec4(inPosition, 0, 1);
}
