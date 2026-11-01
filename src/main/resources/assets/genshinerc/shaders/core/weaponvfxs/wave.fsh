#version 150
uniform sampler2D Sampler0;
uniform float Time;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 uv = texCoord;
    uv.x += sin(uv.y * 25.0 + Time * 15.0) * 0.3;
    uv.y += cos(uv.x * 25.0 + Time * 15.0) * 0.3;
    vec4 col = texture(Sampler0, uv);
    if (col.a < 0.05) discard;
    fragColor = col * vertexColor;
}
