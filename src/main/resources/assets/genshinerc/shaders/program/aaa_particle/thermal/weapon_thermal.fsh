#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D BeforeSampler;
uniform float Time;
in vec2 texCoord;
out vec4 fragColor;

vec3 contribution(vec2 uv) {
    return max(texture(DiffuseSampler, uv).rgb - texture(BeforeSampler, uv).rgb, vec3(0.0));
}

void main() {
    float s = 0.015 + 0.01 * sin(Time * 3.5) + 0.006 * sin(Time * 7.3 + texCoord.x * 12.0);
    vec3 cR = contribution(texCoord + vec2(0.0, s));
    vec3 cG = contribution(texCoord);
    vec3 cB = contribution(texCoord - vec2(0.0, s));

    float m = max(max(max(cR.r, cR.g), cR.b), max(max(cG.r, cG.g), max(cG.b, max(cB.r, max(cB.g, cB.b)))));
    if (m < 0.01) discard;

    vec3 split = vec3(cR.r, cG.g, cB.b)
               + vec3(1.0, 0.15, 0.1) * max(cR.r - cG.g, 0.0)
               + vec3(0.1, 0.5, 1.0) * max(cB.b - cG.g, 0.0);

    fragColor = vec4(texture(BeforeSampler, texCoord).rgb + split, 1.0);
}