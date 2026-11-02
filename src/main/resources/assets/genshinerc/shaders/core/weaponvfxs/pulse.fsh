#version 150

uniform sampler2D Sampler0;
uniform float Time;
uniform vec2 OutSize;
uniform float Trail;

in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;

const bool SWAP_UV = false;
const float ASPECT = 6.0;
const float GAIN = 1.35;

#define TEXEL (1.0 / vec2(textureSize(Sampler0, 0)))

vec2 tuv() { return SWAP_UV ? texCoord.yx : texCoord; }
vec2 st(vec2 t) { return t * vec2(ASPECT, 1.0); }
vec4 tex(vec2 q) { return texture(Sampler0, SWAP_UV ? q.yx : q) * vertexColor; }
vec4 base() { return texture(Sampler0, texCoord) * vertexColor; }

float lum(vec3 c) { return dot(c, vec3(0.299, 0.587, 0.114)); }
float core(vec2 t) { return pow(clamp(1.0 - abs(t.y - 0.5) * 2.0, 0.0, 1.0), 3.0); }
vec3 palette(float x, vec3 a, vec3 b) { return mix(a, b, 0.5 + 0.5 * sin(x)); }

float hash(vec2 p) {
    p = fract(p * vec2(5.3983, 5.4427));
    p += dot(p, p + vec2(21.5351, 14.3137));
    return fract(p.x * p.y * 95.4337);
}
float noise(vec2 p) {
    vec2 i = floor(p), f = fract(p);
    vec2 u = f * f * f * (f * (f * 6.0 - 15.0) + 10.0);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), u.x),
               mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x), u.y);
}
float fbm(vec2 p) {
    float v = 0.0, a = 0.5;
    for (int i = 0; i < 4; i++) { v += a * noise(p); p = p * 2.03 + 17.1; a *= 0.5; }
    return v / 0.9375;
}

void emit(vec3 col, float cov) {
    cov = clamp(cov, 0.0, 1.0);
    if (cov < 0.004) discard;
    vec3 c = max(col, vec3(0.0)) * Trail;
    fragColor = vec4(c * cov * GAIN, cov);
}
void emitSplit(vec4 r, vec4 g, vec4 b) {
    float c = max(g.a, max(r.a, b.a));
    vec3 col = vec3(r.r * r.a, g.g * g.a, b.b * b.a)
             + vec3(1.0, 0.15, 0.1) * max(r.a - g.a, 0.0)
             + vec3(0.1, 0.5, 1.0) * max(b.a - g.a, 0.0);
    emit(col / max(c, 1e-3), c);
}
uniform float PulseSpeed;
uniform float PulseSharp;
uniform float CoreGain;
void main() {
    vec4 b = base(); vec2 t = tuv();
    float p = pow(fract(t.x * 2.5 - Time * PulseSpeed), PulseSharp);
    emit(b.rgb * (1.0 + p * 2.0) + vec3(p * CoreGain * core(t)), b.a);
}