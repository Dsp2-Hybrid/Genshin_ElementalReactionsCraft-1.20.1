#version 150
uniform sampler2D Sampler0;
uniform float Time;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 uv = texCoord;
    uv.x += sin(uv.y * 25.0 + Time * 10.0) * 0.15;
    uv.y += cos(uv.x * 25.0 + Time * 10.0) * 0.15;
    vec4 tex = texture(Sampler0, uv);
    if (tex.a < 0.05) discard; fragColor = tex * vertexColor;
}
