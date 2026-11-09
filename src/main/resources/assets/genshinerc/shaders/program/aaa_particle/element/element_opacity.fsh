#version 150
uniform sampler2D BeforeSampler;
uniform sampler2D AfterSampler;
uniform float Alpha;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec4 b = texture(BeforeSampler, texCoord);
    vec4 a = texture(AfterSampler, texCoord);
    fragColor = mix(b, a, clamp(Alpha, 0.0, 1.0));
}