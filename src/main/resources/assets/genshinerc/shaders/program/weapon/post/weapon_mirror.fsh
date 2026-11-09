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
    if (uv.y > 0.5) {
        vec2 mirror = vec2(uv.x, 1.0 - uv.y + sin(uv.x * 40.0 + Time * 10.0) * 0.04);
        uv = mix(uv, mirror, mask);
    }

    vec3 scene = texture(DiffuseSampler, uv).rgb;
    vec3 combined = 1.0 - (1.0 - scene) * (1.0 - trail.rgb);
    fragColor = vec4(combined, 1.0);
}