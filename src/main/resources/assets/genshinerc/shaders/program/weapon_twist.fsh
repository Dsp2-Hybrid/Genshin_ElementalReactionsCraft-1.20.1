#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;
uniform float Time;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec2 p = texCoord - 0.5;
    float r = length(p);
    float a = atan(p.y, p.x) + (r * 20.0) - Time * 4.0;
    vec2 uv = vec2(cos(a), sin(a)) * r + 0.5;
    vec3 scene = texture(DiffuseSampler, uv).rgb;
    vec3 trail = texture(TrailSampler, uv).rgb;
    vec3 blended = 1.0 - (1.0 - scene) * (1.0 - trail);
    fragColor = vec4(blended, 1.0);
}
