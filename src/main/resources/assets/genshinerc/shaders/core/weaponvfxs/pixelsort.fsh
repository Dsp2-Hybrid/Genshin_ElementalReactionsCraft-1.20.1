#version 150
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec4 base = texture(Sampler0, texCoord);
    if (base.a < 0.05) discard;
    if (dot(base.rgb, vec3(0.299, 0.587, 0.114)) > 0.3) {
        vec4 smear = vec4(0.0);
        for (float i = 0.0; i < 5.0; i += 1.0) smear += texture(Sampler0, texCoord + vec2(i * 0.02, 0.0));
        base.rgb = mix(base.rgb, (smear / 5.0).rgb, 0.5);
    }
    fragColor = base * vertexColor;
}
