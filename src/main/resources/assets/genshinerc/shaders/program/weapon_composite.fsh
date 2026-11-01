#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec4 scene = texture(DiffuseSampler, texCoord);
    vec4 trail = texture(TrailSampler, texCoord);
    fragColor = vec4(scene.rgb + trail.rgb, 1.0);
}
