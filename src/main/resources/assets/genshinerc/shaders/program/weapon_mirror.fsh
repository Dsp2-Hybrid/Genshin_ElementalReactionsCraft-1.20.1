#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;
uniform float Time;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec2 uv = texCoord;
    if (uv.y > 0.5) {
        uv.y = 1.0 - uv.y + sin(uv.x * 40.0 + Time * 10.0) * 0.05;
    }
    vec3 scene = texture(DiffuseSampler, uv).rgb;
    vec3 trail = texture(TrailSampler, uv).rgb;
    vec3 blended = 1.0 - (1.0 - scene) * (1.0 - trail);
    fragColor = vec4(blended, 1.0);
}
