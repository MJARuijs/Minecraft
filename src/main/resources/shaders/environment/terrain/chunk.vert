#version 460

const vec2 TEXTURE_COORDINATES[6] = {
    vec2(0.0, 0.0),
    vec2(0.0, 1.0),
    vec2(1.0, 1.0),
    vec2(0.0, 0.0),
    vec2(1.0, 1.0),
    vec2(1.0, 0.0)
};

const int OVERLAYED_TEXTURE_INDICES[] = {
    0, 38, 39, 40, 52, 53, 56, 76
};

layout (location = 0) in vec3 inPosition;
layout (location = 1) in int textureIndex;

out flat int useOverlayColor;
out vec2 textureCoordinates;

out vec3 position;

void main() {
//    float u = int(textureIndex) % 16;
//    float v = floor(textureIndex / 16);
    textureCoordinates = TEXTURE_COORDINATES[gl_VertexID % 6];

    useOverlayColor = 0;
    for (int i = 0; i < OVERLAYED_TEXTURE_INDICES.length(); i++) {
        if (OVERLAYED_TEXTURE_INDICES[i] == textureIndex) {
            useOverlayColor = 1;
            break;
        }
    }

    position = inPosition;
}
