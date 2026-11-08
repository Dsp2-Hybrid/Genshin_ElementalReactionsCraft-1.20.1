#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;
uniform float Time;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 trail = texture(TrailSampler, texCoord);
    float mask = clamp(trail.a * 1.5, 0.0, 1.0);

    vec2 p = texCoord - 0.5;
    float r = length(p);
    float a = atan(p.y, p.x) + Time * 1.5;
    const float segments = 8.0;
    a = mod(a, 6.283185 / segments);
    if (a > 3.14159 / segments) a = (3.14159 / segments) - a;
    vec2 kUV = vec2(cos(a), sin(a)) * r + 0.5;

    vec2 uv = mix(texCoord, kUV, mask);
    vec3 scene = texture(DiffuseSampler, uv).rgb;
    vec3 combined = 1.0 - (1.0 - scene) * (1.0 - trail.rgb);
    fragColor = vec4(combined, 1.0);
}