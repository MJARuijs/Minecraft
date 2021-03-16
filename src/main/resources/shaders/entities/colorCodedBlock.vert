#version 450

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec3 instancePosition;

uniform mat4 projection;
uniform mat4 view;
uniform float idOffset;
uniform float stepSize;

out vec3 color;

vec3 encodeId(int id) {

    float value = float(id) * stepSize * 4;

    if (value < 1.0) { // red
        return vec3(value, 0.0, 0.0);
    } else if (value < 2.0) { // green
        return vec3(1.0, value - 1.0, 0.0);
    } else if (value < 3.0) { // blue
        return vec3(0.0, 1.0, value - 2.0);
    } else if (value < 4.0) {
        return vec3(value - 3.0, 0.0, 1.0);
    }
//    else if (value < 4.0) { // yellow
//        return vec3(value - 3.0, 1.0, 0.0);
//    } else if (value < 5.0) {
//        return vec3(1.0, value - 4.0, 0.0);
//    } else if (value < 6.0) {
//        return vec3(0.0, value - 5.0, 1.0);
//    } else if (value < 7.0) {
//        return vec3(value - 6.0, value - 6.0, value - 6.0);
//    }

    return vec3(0.0, 0.0, 0.0);
}

void main() {
    mat4 model;
    model[0][0] = 1;
    model[1][1] = 1;
    model[2][2] = 1;
    model[3][3] = 1;

    model[3][0] = instancePosition.x;
    model[3][1] = instancePosition.y;
    model[3][2] = instancePosition.z;

    int id = gl_InstanceID;

    vec4 worldPosition = model * vec4(inPosition, 1.0);

    color = encodeId(id + int(idOffset));
    gl_Position = projection * view * worldPosition;
}
