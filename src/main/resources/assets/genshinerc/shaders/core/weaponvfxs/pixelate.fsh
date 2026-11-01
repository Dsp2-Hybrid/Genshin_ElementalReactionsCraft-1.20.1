#version 150
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec4 tex = texture(Sampler0, floor(texCoord * 30.0) / 30.0);
    if (tex.a < 0.05) discard; fragColor = tex * vertexColor;
}
