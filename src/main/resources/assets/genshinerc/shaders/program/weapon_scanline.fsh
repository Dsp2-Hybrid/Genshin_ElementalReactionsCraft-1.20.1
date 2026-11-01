#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;
uniform float Time;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec2 uv = texCoord;
    uv.x += sin(uv.y * 200.0 + Time * 20.0) * 0.03;
    vec3 scene = texture(DiffuseSampler, uv).rgb;
    vec3 trail = texture(TrailSampler, uv).rgb;
    vec3 blended = 1.0 - (1.0 - scene) * (1.0 - trail);
    float scan = sin(uv.y * 1000.0) * 0.5 + 0.5;
    fragColor = vec4(blended * scan, 1.0);
}
