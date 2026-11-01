#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;
uniform float Time;
in vec2 texCoord;
out vec4 fragColor;
float rand(vec2 co){ return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453); }
void main() {
    float block = floor(texCoord.y * 50.0);
    vec2 uv = texCoord;
    if (rand(vec2(block, floor(Time * 20.0))) > 0.5) {
        uv.x += (rand(vec2(block, Time)) - 0.5) * 0.3;
    }
    vec3 scene = texture(DiffuseSampler, uv).rgb;
    vec3 trail = texture(TrailSampler, uv).rgb;
    vec3 blended = 1.0 - (1.0 - scene) * (1.0 - trail);
    fragColor = vec4(blended, 1.0);
}
