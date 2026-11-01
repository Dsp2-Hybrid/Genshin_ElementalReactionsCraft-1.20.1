#version 150
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 dir = texCoord - 0.5; vec4 color = vec4(0.0);
    for (int i = 0; i < 12; i++) color += texture(Sampler0, dir * (1.0 - float(i) * 0.03) + 0.5);
    if (color.a < 0.05) discard; fragColor = (color / 12.0) * vertexColor;
}
