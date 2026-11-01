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
    fragColor = texture(DiffuseSampler, uv) * vertexColor;
}
