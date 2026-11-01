#version 150
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    float a = texture(Sampler0, texCoord).a;
    if (a < 0.05) discard;
    fragColor = vec4(texture(Sampler0, texCoord + vec2(0.02, 0)).r, texture(Sampler0, texCoord).g, texture(Sampler0, texCoord - vec2(0.02, 0)).b, a) * vertexColor;
}
