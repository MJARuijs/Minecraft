#version 460

layout (triangles_adjacency) in;
layout (line_strip, max_vertices = 6) out;

struct DirectionalLight {
    vec3 direction;
};

in vec4 worldPosition[];

uniform DirectionalLight sun;

void emitLine(int startIndex, int endIndex) {
    gl_Position = gl_in[startIndex].gl_Position;
    EmitVertex();

    gl_Position = gl_in[endIndex].gl_Position;
    EmitVertex();

    EndPrimitive();
}

void main() {

    vec3 baseEdge1 = (worldPosition[2] - worldPosition[0]).xyz;
    vec3 baseEdge2 = (worldPosition[4] - worldPosition[0]).xyz;
    vec3 baseEdge3 = (worldPosition[4] - worldPosition[2]).xyz;

    vec3 adjacentEdge1 = (worldPosition[0] - worldPosition[1]).xyz;
    vec3 adjacentEdge2 = (worldPosition[3] - worldPosition[4]).xyz;
    vec3 adjacentEdge3 = (worldPosition[4] - worldPosition[5]).xyz;

    vec3 normal = cross(baseEdge1, baseEdge2);

    if (dot(normal, sun.direction) > 0.00001) {
        normal = cross(baseEdge1, adjacentEdge1);
        if (dot(normal, sun.direction) <= 0) {
            emitLine(0, 2);
        }

        normal = cross(baseEdge2, adjacentEdge2);
        if (dot(normal, sun.direction) <= 0) {
            emitLine(0, 4);
        }

        normal = cross(baseEdge3, adjacentEdge3);
        if (dot(normal, sun.direction) <= 0) {
            emitLine(2, 4);
        }
    }

}
