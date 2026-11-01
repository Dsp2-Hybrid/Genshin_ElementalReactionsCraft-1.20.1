#version 150
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 px = 1.0 / 32.0; vec4 base = texture(Sampler0, texCoord);
    if (base.a < 0.05) discard;
    vec3 col = vec3(0.0); float total = 0.0;
    for (float x = -2.0; x <= 2.0; x += 1.0) for (float y = -2.0; y <= 2.0; y += 1.0) {
        vec4 s = texture(Sampler0, texCoord + vec2(x, y) * px);
        float w = 1.0 / (1.0 + length(s.rgb - base.rgb) * 3.0);
        col += s.rgb * w; total += w;
    }
    fragColor = vec4(col / total * vertexColor.rgb, base.a * vertexColor.a);
}
