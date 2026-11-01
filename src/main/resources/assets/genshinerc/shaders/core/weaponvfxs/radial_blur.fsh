#version 150
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec4 base = texture(Sampler0, texCoord);
    if (base.a < 0.05) discard;
    vec2 dir = texCoord - 0.5; vec4 color = vec4(0.0);
    for(int i = 0; i < 12; i++) color += texture(Sampler0, dir * (1.0 - float(i) * 0.05) + 0.5);
    fragColor = (color / 12.0) * vertexColor;
}
