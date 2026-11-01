#version 150
uniform sampler2D Sampler0;
uniform float Time;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 uv = texCoord; uv.y = fract((uv.y - 0.5) * (1.0 + sin(Time * 5.0) * 0.5) + 0.5);
    vec4 tex = texture(Sampler0, uv);
    if (tex.a < 0.05) discard; fragColor = tex * vertexColor;
}
