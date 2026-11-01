#version 150
uniform sampler2D Sampler0;
uniform float Time;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 p = texCoord - 0.5; float radius = length(p); float angle = atan(p.y, p.x) + Time * 2.0;
    float segments = 4.0; angle = mod(angle, 6.283185 / segments);
    if (angle > 3.14159 / segments) angle = (3.14159 / segments) - angle;
    vec4 tex = texture(Sampler0, vec2(cos(angle), sin(angle)) * radius + 0.5);
    if (tex.a < 0.05) discard; fragColor = tex * vertexColor;
}
