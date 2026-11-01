#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec3 scene = 1.0 - texture(DiffuseSampler, texCoord).rgb;
    vec3 trail = texture(TrailSampler, texCoord).rgb;
    vec3 blended = 1.0 - (1.0 - scene) * (1.0 - trail);
    fragColor = vec4(blended, 1.0);
}
