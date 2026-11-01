#version 150
uniform sampler2D DiffuseSampler;
uniform float Time;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec4 base = texture(DiffuseSampler, texCoord);
    float lum = dot(base.rgb, vec3(0.299, 0.587, 0.114));
    vec4 res = base;
    if (lum > 0.3) {
        for(float i = 0.0; i < 0.15; i += 0.01) {
            vec2 uv = texCoord + vec2(i, 0.0);
            res = mix(res, texture(DiffuseSampler, uv), 1.0 - (i/0.15));
        }
    }
    fragColor = res * vertexColor;
}
