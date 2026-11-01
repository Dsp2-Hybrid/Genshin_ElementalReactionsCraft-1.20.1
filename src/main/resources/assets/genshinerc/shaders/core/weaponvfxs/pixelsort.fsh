#version 150
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec4 base = texture(Sampler0, texCoord);
    if (base.a < 0.05) discard;
    float lum = dot(base.rgb, vec3(0.299, 0.587, 0.114));
    vec4 res = base;
    if (lum > 0.15) {
        for(float i = 0.0; i < 0.8; i += 0.02) {
            vec2 uv = texCoord + vec2(i, 0.0);
            res = mix(res, texture(Sampler0, uv), 1.0 - (i/0.8));
        }
    }
    fragColor = res * vertexColor;
}
