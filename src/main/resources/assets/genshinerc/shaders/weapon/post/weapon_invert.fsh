#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 trail = texture(TrailSampler, texCoord);
    float mask = clamp(trail.a * 1.5, 0.0, 1.0);

    vec3 raw = texture(DiffuseSampler, texCoord).rgb;
    vec3 scene = mix(raw, 1.0 - raw, mask);
    vec3 combined = 1.0 - (1.0 - scene) * (1.0 - trail.rgb);
    fragColor = vec4(combined, 1.0);
}