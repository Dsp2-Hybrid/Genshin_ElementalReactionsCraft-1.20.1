#version 150
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec4 tex = texture(Sampler0, texCoord);
    if (tex.a < 0.05) discard;
    fragColor = vec4(floor(tex.rgb * 5.0) / 5.0 * vertexColor.rgb, tex.a * vertexColor.a);
}
