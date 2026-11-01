#version 150
uniform sampler2D Sampler0;
uniform float Time;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 p = texCoord - 0.5; float r = length(p);
    vec4 tex = texture(Sampler0, texCoord + normalize(p + vec2(0.001)) * sin(r * 40.0 - Time * 15.0) * 0.03);
    if (tex.a < 0.05) discard; fragColor = tex * vertexColor;
}
