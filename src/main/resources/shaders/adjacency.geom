#version 460

layout (triangles_adjacency) in;
layout (triangle_strip, max_vertices = 18) out;

const float epsilon = 0.001;

struct DirectionalLight {
    vec3 direction;
};

in vec3 worldPosition[];

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

void emitLine(int startIndex, int endIndex) {
    gl_Position = gl_in[startIndex].gl_Position;
    EmitVertex();

    gl_Position = gl_in[endIndex].gl_Position;
    EmitVertex();

    EndPrimitive();
}

void main() {
    mat4 mvpMatrix = projection * view * model;

    vec3 baseEdge1 = (worldPosition[2] - worldPosition[0]).xyz;
    vec3 baseEdge2 = (worldPosition[4] - worldPosition[0]).xyz;
    vec3 baseEdge3 = (worldPosition[4] - worldPosition[2]).xyz;

    vec3 adjacentEdge1 = (worldPosition[0] - worldPosition[1]).xyz;
    vec3 adjacentEdge2 = (worldPosition[3] - worldPosition[4]).xyz;
    vec3 adjacentEdge3 = (worldPosition[4] - worldPosition[5]).xyz;

    vec3 normal = cross(baseEdge1, baseEdge2);
//    vec3 lightDirection = normalize(pointLight.position - worldPosition[0].xyz);
    vec3 lightDirection = normalize(sun.direction);

    if (dot(normal, lightDirection) > 0.00001) {

        normal = cross(baseEdge1, adjacentEdge1);
        if (dot(normal, lightDirection) <= 0) {
//            emitLine(0, 2);
            vec3 startVertex = worldPosition[0].xyz;
            vec3 endVertex = worldPosition[2].xyz;
            emitQuad(startVertex, endVertex, mvpMatrix);
        }

//        lightDirection = normalize(pointLight.position - worldPosition[2]);
        normal = cross(baseEdge3, adjacentEdge3);
        if (dot(normal, lightDirection) <= 0) {
//            emitLine(2, 4);
            vec3 startVertex = worldPosition[2].xyz;
            vec3 endVertex = worldPosition[4].xyz;
            emitQuad(startVertex, endVertex, mvpMatrix);
        }

//        lightDirection = normalize(pointLight.position - worldPosition[4]);
        normal = cross(baseEdge2, adjacentEdge2);
        if (dot(normal, lightDirection) <= 0) {
//            emitLine(0, 4);
            vec3 startVertex = worldPosition[0].xyz;
            vec3 endVertex = worldPosition[4].xyz;
            emitQuad(startVertex, endVertex, mvpMatrix);
        }

//        lightDirection = normalize(worldPosition[0] - pointLight.position);
        gl_Position = mvpMatrix * vec4(worldPosition[0].xyz - lightDirection * epsilon, 1.0);
        EmitVertex();

//        lightDirection = normalize(worldPosition[2] - pointLight.position);
        gl_Position = mvpMatrix * vec4(worldPosition[2].xyz - lightDirection * epsilon, 1.0);
        EmitVertex();

//        lightDirection = normalize(worldPosition[4] - pointLight.position);
        gl_Position = mvpMatrix * vec4(worldPosition[4].xyz - lightDirection * epsilon, 1.0);
        EmitVertex();
        EndPrimitive();

//        lightDirection = worldPosition[0] - pointLight.position;
        gl_Position = mvpMatrix * vec4(lightDirection, 0.0);
        EmitVertex();

//        lightDirection = worldPosition[4] - pointLight.position;
        gl_Position = mvpMatrix * vec4(lightDirection, 0.0);
        EmitVertex();

//        lightDirection = worldPosition[2] - pointLight.position;
        gl_Position = mvpMatrix * vec4(lightDirection, 0.0);
        EmitVertex();
        EndPrimitive();
    }

}
