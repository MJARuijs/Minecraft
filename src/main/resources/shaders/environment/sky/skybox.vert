#version 450

layout(location = 0) in vec3 inPosition;

uniform mat4 projection;
uniform mat4 view;

uniform float distance;

out vec3 passPosition;

void main() {
    passPosition = inPosition;

    vec4 worldPosition = vec4(inPosition * distance, 1.0);

    gl_Position = projection * vec4(mat3(view) * inPosition * distance, 1.0);
}
