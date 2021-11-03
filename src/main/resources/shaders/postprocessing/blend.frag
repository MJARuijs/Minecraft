#version 460

in vec2 textureCoordinates;

uniform sampler2D texture1;
uniform sampler2D texture2;

out vec4 outColor;

void main() {
    vec4 color1 = texture(texture1, textureCoordinates);
    vec4 color2 = texture(texture2, textureCoordinates);

//    if (color1.a < 0.5) {
//        outColor = color2;
//    } else {
//        outColor = color1;
//    }/
    outColor = color1 + color2;
//    outColor = vec4(1,0,0,1);
//    outColor = mix(color1, color2, 0.5);
//    outColor = color1;
}
