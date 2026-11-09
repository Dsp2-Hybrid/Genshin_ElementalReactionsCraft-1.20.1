#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 trail = texture(TrailSampler, texCoord);
    float mask = clamp(trail.a * 1.5, 0.0, 1.0);

    vec2 p = texCoord - 0.5;
    float r2 = dot(p, p);
    vec2 fishUV = clamp(0.5 + p * (1.0 + r2 * 2.5), 0.0, 1.0);
    vec2 uv = mix(texCoord, fishUV, mask);

    vec3 scene = texture(DiffuseSampler, uv).rgb;
    vec3 combined = 1.0 - (1.0 - scene) * (1.0 - trail.rgb);
    fragColor = vec4(combined, 1.0);
}