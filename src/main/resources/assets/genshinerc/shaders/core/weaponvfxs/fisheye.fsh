#version 150
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 p = texCoord - 0.5; vec2 uv = p * (1.0 + 2.0 * dot(p, p)) + 0.5;
    if(uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) discard;
    vec4 tex = texture(Sampler0, uv);
    if (tex.a < 0.05) discard; fragColor = tex * vertexColor;
}
