#version 150
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 p = texCoord - 0.5;
    float r2 = dot(p, p);
    float factor = 1.0 + 3.0 * r2;
    vec2 uv = p * factor + 0.5;
    if(uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) discard;
    vec4 col = texture(Sampler0, uv);
    if (col.a < 0.05) discard;
    fragColor = col * vertexColor;
}
