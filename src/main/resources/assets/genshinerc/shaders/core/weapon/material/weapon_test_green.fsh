#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D BeforeSampler;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    fragColor = vec4(0.0, 1.0, 0.0, 1.0);
}