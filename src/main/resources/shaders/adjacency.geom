#version 460

layout (triangles_adjacency) in;
layout (triangle_strip, max_vertices = 18) out;

const float epsilon = 0.001;

struct DirectionalLight {
    vec3 direction;
};

in vec3 vertexPosition[];

uniform DirectionalLight sun;
uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

void emitQuad(vec3 startVertex, vec3 endVertex, mat4 mvpMatrix) {
    vec3 lightDirection = -normalize(sun.direction);
    gl_Position = mvpMatrix * vec4((startVertex + lightDirection * epsilon), 1.0);
    EmitVertex();

    gl_Position = mvpMatrix * vec4(lightDirection, 0.0);
    EmitVertex();

    gl_Position = mvpMatrix * vec4((endVertex + lightDirection * epsilon), 1.0);
    EmitVertex();

    gl_Position = mvpMatrix * vec4(lightDirection, 0.0);
    EmitVertex();

    EndPrimitive();
}

void main() {
    mat4 mvpMatrix = projection * view * model;

    vec3 baseEdge1 = vertexPosition[2] - vertexPosition[0];
    vec3 baseEdge2 = vertexPosition[4] - vertexPosition[0];
    vec3 baseEdge3 = vertexPosition[4] - vertexPosition[2];

    vec3 adjacentEdge1 = vertexPosition[0] - vertexPosition[1];
    vec3 adjacentEdge2 = vertexPosition[3] - vertexPosition[4];
    vec3 adjacentEdge3 = vertexPosition[4] - vertexPosition[5];

    vec3 normal = cross(baseEdge1, baseEdge2);
    vec3 lightDirection = normalize(sun.direction);

    if (dot(normal, lightDirection) > 0) {

        normal = cross(baseEdge1, adjacentEdge1);
        if (dot(normal, lightDirection) <= 0) {
            vec3 startVertex = vertexPosition[0].xyz;
            vec3 endVertex = vertexPosition[2].xyz;
            emitQuad(startVertex, endVertex, mvpMatrix);
        }

        normal = cross(baseEdge3, adjacentEdge3);
        if (dot(normal, lightDirection) <= 0) {
            vec3 startVertex = vertexPosition[2].xyz;
            vec3 endVertex = vertexPosition[4].xyz;
            emitQuad(startVertex, endVertex, mvpMatrix);
        }

        normal = cross(baseEdge2, adjacentEdge2);
        if (dot(normal, lightDirection) <= 0) {
            vec3 startVertex = vertexPosition[0].xyz;
            vec3 endVertex = vertexPosition[4].xyz;
            emitQuad(startVertex, endVertex, mvpMatrix);
        }

        gl_Position = mvpMatrix * vec4(vertexPosition[0].xyz - lightDirection * epsilon, 1.0);
        EmitVertex();

        gl_Position = mvpMatrix * vec4(vertexPosition[2].xyz - lightDirection * epsilon, 1.0);
        EmitVertex();

        gl_Position = mvpMatrix * vec4(vertexPosition[4].xyz - lightDirection * epsilon, 1.0);
        EmitVertex();
        EndPrimitive();

        gl_Position = mvpMatrix * vec4(lightDirection, 0.0);
        EmitVertex();

        gl_Position = mvpMatrix * vec4(lightDirection, 0.0);
        EmitVertex();

        gl_Position = mvpMatrix * vec4(lightDirection, 0.0);
        EmitVertex();
        EndPrimitive();
    }

}
