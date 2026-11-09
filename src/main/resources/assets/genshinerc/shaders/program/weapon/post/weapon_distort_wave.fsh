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
    uv.x += sin(uv.y * 22.0 + Time * 10.0) * 0.045 * mask;
    uv.y += cos(uv.x * 18.0 + Time * 8.0) * 0.025 * mask;

    vec3 scene = texture(DiffuseSampler, uv).rgb;
    vec3 combined = 1.0 - (1.0 - scene) * (1.0 - trail.rgb);
    fragColor = vec4(combined, 1.0);
}