#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec2 uv = floor(texCoord * 80.0) / 80.0;
    vec3 scene = texture(DiffuseSampler, uv).rgb;
    vec3 trail = texture(TrailSampler, uv).rgb;
    vec3 blended = 1.0 - (1.0 - scene) * (1.0 - trail);
    fragColor = vec4(blended, 1.0);
}
