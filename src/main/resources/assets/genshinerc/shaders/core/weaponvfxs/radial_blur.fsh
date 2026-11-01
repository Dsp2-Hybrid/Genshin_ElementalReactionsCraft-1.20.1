#version 150
uniform sampler2D DiffuseSampler;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 dir = texCoord - 0.5;
    vec4 color = vec4(0.0);
    float total = 0.0;
    for(int i = 0; i < 8; i++) {
        float scale = 1.0 - float(i) * 0.05;
        color += texture(DiffuseSampler, dir * scale + 0.5);
        total += 1.0;
    }
    fragColor = (color / total) * vertexColor;
}
