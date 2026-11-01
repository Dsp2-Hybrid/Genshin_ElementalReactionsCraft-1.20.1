#version 150
uniform sampler2D DiffuseSampler;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;

void main() {
    fragColor = vec4(1.0, 0.0, 0.0, vertexColor.a);
}