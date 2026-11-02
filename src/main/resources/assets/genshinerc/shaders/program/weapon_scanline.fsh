#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;
uniform float Time;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 trail = texture(TrailSampler, texCoord);
    float mask = clamp(trail.a * 1.5, 0.0, 1.0);

    vec2 uv = texCoord;
    uv.x += sin(uv.y * 200.0 + Time * 20.0) * 0.015 * mask;

    vec3 scene = texture(DiffuseSampler, uv).rgb;
    float scan = mix(1.0, sin(uv.y * 800.0) * 0.5 + 0.5, mask * 0.6);
    vec3 combined = 1.0 - (1.0 - scene * scan) * (1.0 - trail.rgb);
    fragColor = vec4(combined, 1.0);
}