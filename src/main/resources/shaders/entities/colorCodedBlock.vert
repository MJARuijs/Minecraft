#version 450

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inTextureCoord;
layout(location = 2) in vec3 inNormal;
layout(location = 3) in float instanceTextureId;
//layout(location = 4) in vec4 positionChanges;

uniform mat4 projection;
uniform mat4 view;
uniform int chunkHeight;
uniform int chunkSize;
uniform vec3 place;

uniform float stepSize;

out vec3 color;

vec3 encodeId(int id) {

//    float value = float(id) * stepSize * 7.0;
//
//    if (value < 1.0) { // red
//        return vec3(value, 0.0, 0.0);
//    } else if (value < 2.0) { // green
//        return vec3(0.0, value, 0.0);
//    } else if (value < 3.0) { // blue
//        return vec3(0.0, 0.0, value - 2.0);
//    } else if (value < 4.0) { // yellow
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

    model[3][0] = place.x; // X
    model[3][1] = place.y; // Y
    model[3][2] = place.z; // Z

    int id = gl_InstanceID;

    float newX = floor(id / (chunkSize * chunkHeight));
    float newY = mod(floor(id / chunkSize), chunkHeight);
    float newZ = mod(id, chunkSize);

    model[3][0] += newX; // X
    model[3][1] += newY; // Y
    model[3][2] += newZ; // Z

    //    if (id == int(positionChanges[3])) {
    //        model[3][0] += positionChanges.x; // X
    //        model[3][1] += positionChanges.y; // Y
    //        model[3][2] += positionChanges.z; // Z
    //        model[3][2] += positionChanges[0] - positionChanges[1] - positionChanges[2] - positionChanges[3];
    //    }

    //    model[3][0] = instancePosition[0];
    //    model[3][1] = instancePosition[1];
    //    model[3][2] = instancePosition[2];

    vec4 worldPosition = model * vec4(inPosition, 1.0);

    color = encodeId(id);
    gl_Position = projection * view * worldPosition;
}
