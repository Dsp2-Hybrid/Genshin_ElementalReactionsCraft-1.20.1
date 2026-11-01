#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec4 scene = texture(DiffuseSampler, texCoord);
    vec4 trail = texture(TrailSampler, texCoord);
    vec3 blended = 1.0 - (1.0 - scene.rgb) * (1.0 - trail.rgb * trail.a);
    fragColor = vec4(blended, 1.0);
}
