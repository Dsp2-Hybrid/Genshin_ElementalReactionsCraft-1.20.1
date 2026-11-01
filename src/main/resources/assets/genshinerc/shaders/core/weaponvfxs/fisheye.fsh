#version 150
uniform sampler2D DiffuseSampler;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 p = texCoord - 0.5;
    float r2 = dot(p, p);
    float factor = 1.0 + 0.5 * r2;
    vec2 uv = p * factor + 0.5;
    if(uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) discard;
    vec4 col = texture(DiffuseSampler, uv);
    vec3 finalColor = col.rgb * vertexColor.rgb;
    if (col.a < 0.05 || dot(finalColor, vec3(0.299, 0.587, 0.114)) < 0.08) discard;
    fragColor = vec4(finalColor, col.a * vertexColor.a);
}
