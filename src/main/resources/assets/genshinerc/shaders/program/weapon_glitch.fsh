#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;
uniform float Time;

in vec2 texCoord;
out vec4 fragColor;

float rand(vec2 co) { return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453); }

void main() {
    vec4 trail = texture(TrailSampler, texCoord);
    float mask = clamp(trail.a * 2.0, 0.0, 1.0);

    float block = floor(texCoord.y * 50.0);
    float tick = floor(Time * 20.0);
    vec2 uv = texCoord;
    if (rand(vec2(block, tick)) > 0.5)
        uv.x += (rand(vec2(block, Time)) - 0.5) * 0.28 * mask;

    float r = texture(DiffuseSampler, uv + vec2(0.004 * mask, 0.0)).r;
    float g = texture(DiffuseSampler, uv).g;
    float b = texture(DiffuseSampler, uv - vec2(0.004 * mask, 0.0)).b;

    vec3 scene = vec3(r, g, b);
    vec3 combined = 1.0 - (1.0 - scene) * (1.0 - trail.rgb);
    fragColor = vec4(combined, 1.0);
}