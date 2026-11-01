#version 150
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 uv = texCoord * 16.0; vec2 r = vec2(1.0, 1.732); vec2 h = r * 0.5;
    vec2 a = mod(uv, r) - h; vec2 b = mod(uv - h, r) - h;
    vec4 tex = texture(Sampler0, texCoord);
    if (tex.a < 0.05) discard;
    fragColor = vec4(tex.rgb * (1.0 - smoothstep(0.4, 0.5, length(dot(a, a) < dot(b, b) ? a : b)) * 0.5) * vertexColor.rgb, tex.a * vertexColor.a);
}
