#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;
uniform float Time;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec4 scene = texture(DiffuseSampler, texCoord);
    vec4 trail = texture(TrailSampler, texCoord);
    vec3 additive = scene.rgb + trail.rgb * trail.a;
    float trailMask = clamp(trail.a * 3.0, 0.0, 1.0);
    vec3 tonemapped = additive / (1.0 + additive * 0.6);
    vec3 result = mix(scene.rgb, tonemapped, trailMask);
    fragColor = vec4(result, 1.0);
}
