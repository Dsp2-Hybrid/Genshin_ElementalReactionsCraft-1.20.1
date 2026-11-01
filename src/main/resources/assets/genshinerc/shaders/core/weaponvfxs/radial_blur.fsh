#version 150
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec4 base = texture(Sampler0, texCoord);
    if (base.a < 0.05) discard;
    vec2 dir = texCoord - 0.5;
    vec4 color = vec4(0.0);
    float total = 0.0;
    for(int i = 0; i < 20; i++) {
        float scale = 1.0 - float(i) * 0.08;
        color += texture(Sampler0, dir * scale + 0.5);
        total += 1.0;
    }
    color /= total;
    fragColor = vec4(color.rgb * vertexColor.rgb, color.a * vertexColor.a);
}
