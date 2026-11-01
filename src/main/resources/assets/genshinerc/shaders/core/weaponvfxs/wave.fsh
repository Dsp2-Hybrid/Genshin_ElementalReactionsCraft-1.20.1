#version 150
uniform sampler2D DiffuseSampler;
uniform float Time;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 uv = texCoord;
    uv.x += sin(uv.y * 10.0 + Time * 5.0) * 0.05;
    uv.y += cos(uv.x * 10.0 + Time * 5.0) * 0.05;
    vec4 col = texture(DiffuseSampler, uv);
    vec3 finalColor = col.rgb * vertexColor.rgb;
    if (col.a < 0.05 || dot(finalColor, vec3(0.299, 0.587, 0.114)) < 0.08) discard;
    fragColor = vec4(finalColor, col.a * vertexColor.a);
}
