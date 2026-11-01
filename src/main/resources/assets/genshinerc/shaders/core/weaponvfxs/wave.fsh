#version 150
uniform sampler2D Sampler0;
uniform float Time;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 uv = texCoord;
    uv.x += sin(uv.y * 10.0 + Time * 5.0) * 0.05;
    uv.y += cos(uv.x * 10.0 + Time * 5.0) * 0.05;
    vec4 col = texture(Sampler0, uv);
    if (col.a < 0.05) discard;
    fragColor = vec4(col.rgb * vertexColor.rgb * 8.0, col.a * vertexColor.a);
}


