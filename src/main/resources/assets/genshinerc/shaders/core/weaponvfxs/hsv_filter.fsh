#version 150

uniform sampler2D Sampler0;
uniform float Time;
uniform vec2 OutSize;

in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;

#define TEXEL (1.0 / vec2(textureSize(Sampler0, 0)))

float lum(vec3 c) { return dot(c, vec3(0.299, 0.587, 0.114)); }

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

vec4 warp(vec2 uv) {
    vec4 o = texture(Sampler0, texCoord);
    if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) return o;
    vec4 w = texture(Sampler0, uv);
    return mix(o, w, smoothstep(0.0, 0.2, w.a));
}

void emitGlow(vec3 rgb, vec3 glow, float a) {
    vec3 c = rgb * vertexColor.rgb + glow;
    a = clamp(a, 0.0, 1.0) * vertexColor.a;
    float k = clamp(max(c.r, max(c.g, c.b)) * 1.6, 0.0, 1.0);
    if (a * k < 0.004) discard;
    fragColor = vec4(c * 1.35, a * k);
}

void emit(vec3 rgb, float a) { emitGlow(rgb, vec3(0.0), a); }

vec3 rgb2hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
    float d = q.x - min(q.w, q.y);
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + 1e-10)), d / (q.x + 1e-10), q.x);
}
vec3 hsv2rgb(vec3 c) {
    vec3 p = abs(fract(c.xxx + vec3(1.0, 2.0 / 3.0, 1.0 / 3.0)) * 6.0 - 3.0);
    return c.z * mix(vec3(1.0), clamp(p - 1.0, 0.0, 1.0), c.y);
}
void main() {
    vec4 t = texture(Sampler0, texCoord);
    vec3 hsv = rgb2hsv(t.rgb * vertexColor.rgb);
    hsv.x = fract(hsv.x + Time * 0.18 + texCoord.x * 0.5);
    hsv.y = max(hsv.y * 1.3, 0.55);
    emitGlow(vec3(0.0), hsv2rgb(hsv), t.a);
}