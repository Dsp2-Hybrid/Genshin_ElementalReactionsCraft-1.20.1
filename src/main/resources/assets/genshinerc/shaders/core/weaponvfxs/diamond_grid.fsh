#version 150
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 g = texCoord * 20.0; float d = abs(fract(g.x + g.y) - 0.5) + abs(fract(g.x - g.y) - 0.5);
    vec4 tex = texture(Sampler0, texCoord);
    if (tex.a < 0.05) discard;
    fragColor = vec4(tex.rgb * mix(0.6, 1.0, step(0.15, d)) * vertexColor.rgb, tex.a * vertexColor.a);
}
