#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;
uniform float Time;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 trail = texture(TrailSampler, texCoord);
    float mask = clamp(trail.a * 1.5, 0.0, 1.0);

    float split = (0.04 + sin(Time * 4.0) * 0.012) * mask;
    float r = texture(DiffuseSampler, texCoord + vec2(split, 0.0)).r + trail.r;
    float g = texture(DiffuseSampler, texCoord).g + trail.g;
    float b = texture(DiffuseSampler, texCoord - vec2(split, 0.0)).b + trail.b;
    fragColor = vec4(clamp(vec3(r, g, b), 0.0, 1.0), 1.0);
}