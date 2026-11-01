#version 150
uniform sampler2D DiffuseSampler;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec4 tex = texture(DiffuseSampler, texCoord);
    if (tex.a < 0.05) discard;
    fragColor = vec4(1.0, 0.0, 0.0, tex.a * vertexColor.a); 
}
