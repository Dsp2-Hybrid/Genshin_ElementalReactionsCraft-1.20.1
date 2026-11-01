#version 150
uniform sampler2D Sampler0;
uniform float Time;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 uv = texCoord; if (uv.x > 0.5) uv.x = 1.0 - uv.x + sin(uv.y * 20.0 + Time * 5.0) * 0.03;
    vec4 tex = texture(Sampler0, uv);
    if (tex.a < 0.05) discard; fragColor = tex * vertexColor;
}
