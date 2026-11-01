#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;
uniform float Time;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec2 dir = normalize(texCoord - 0.5);
    float dist = length(texCoord - 0.5);
    vec2 uv = texCoord - dir * sin(dist * 40.0 - Time * 15.0) * 0.08;
    vec3 scene = texture(DiffuseSampler, uv).rgb;
    vec3 trail = texture(TrailSampler, uv).rgb;
    vec3 blended = 1.0 - (1.0 - scene) * (1.0 - trail);
    fragColor = vec4(blended, 1.0);
}
