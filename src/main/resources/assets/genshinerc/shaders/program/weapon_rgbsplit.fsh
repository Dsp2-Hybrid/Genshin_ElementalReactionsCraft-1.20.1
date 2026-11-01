#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec2 offset = vec2(0.05, 0.0);
    float r = texture(DiffuseSampler, texCoord + offset).r + texture(TrailSampler, texCoord + offset).r;
    float g = texture(DiffuseSampler, texCoord).g + texture(TrailSampler, texCoord).g;
    float b = texture(DiffuseSampler, texCoord - offset).b + texture(TrailSampler, texCoord - offset).b;
    fragColor = vec4(r, g, b, 1.0);
}
