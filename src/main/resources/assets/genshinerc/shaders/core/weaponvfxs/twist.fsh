#version 150
uniform sampler2D Sampler0;
uniform float Time;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 p = texCoord - 0.5; float r = length(p); float a = atan(p.y, p.x) + (r * 20.0) - Time * 4.0;
    vec4 tex = texture(Sampler0, vec2(cos(a), sin(a)) * r + 0.5);
    if (tex.a < 0.05) discard; fragColor = tex * vertexColor;
}
