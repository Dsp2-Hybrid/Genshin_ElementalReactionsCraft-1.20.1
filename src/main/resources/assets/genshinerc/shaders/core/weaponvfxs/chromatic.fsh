#version 150
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 dir = texCoord - 0.5; float off = length(dir) * 0.05;
    float a = texture(Sampler0, texCoord).a;
    if (a < 0.05) discard;
    fragColor = vec4(texture(Sampler0, texCoord - dir * off * 0.3).r, texture(Sampler0, texCoord).g, texture(Sampler0, texCoord + dir * off * 0.3).b, a) * vertexColor;
}
