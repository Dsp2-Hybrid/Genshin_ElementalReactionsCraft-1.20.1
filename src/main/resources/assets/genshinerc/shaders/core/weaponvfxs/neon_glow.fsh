#version 150
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 px = 1.0 / 64.0; vec4 c = texture(Sampler0, texCoord);
    if (c.a < 0.05) discard;
    float edge = abs(texture(Sampler0, texCoord - vec2(px, 0)).a - c.a) + abs(texture(Sampler0, texCoord + vec2(px, 0)).a - c.a) + abs(texture(Sampler0, texCoord - vec2(0, px)).a - c.a) + abs(texture(Sampler0, texCoord + vec2(0, px)).a - c.a);
    fragColor = vec4(c.rgb * vertexColor.rgb + vec3(0.1, 0.9, 1.0) * edge * 2.0, c.a * vertexColor.a);
}
