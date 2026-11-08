#version 150

uniform sampler2D BeforeSampler;
uniform sampler2D IsolatedSampler;
uniform float Alpha;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 before = texture(BeforeSampler, texCoord);
    vec4 isolated = texture(IsolatedSampler, texCoord);
    fragColor = before + (isolated * Alpha);
}