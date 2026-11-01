#version 150
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 px = 1.0 / 32.0; vec4 base = texture(Sampler0, texCoord);
    if (base.a < 0.05) discard;
    vec3 b = vec3(0.0);
    for (float x = -2.0; x <= 2.0; x += 1.0) for (float y = -2.0; y <= 2.0; y += 1.0) {
        vec4 s = texture(Sampler0, texCoord + vec2(x, y) * px);
        if (dot(s.rgb, vec3(0.299, 0.587, 0.114)) > 0.5) b += s.rgb * (dot(s.rgb, vec3(0.299, 0.587, 0.114)) - 0.5);
    }
    fragColor = vec4((base.rgb + b * 0.15) * vertexColor.rgb, base.a * vertexColor.a);
}
