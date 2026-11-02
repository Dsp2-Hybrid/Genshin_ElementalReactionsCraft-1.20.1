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

void main() {
    float band = floor(texCoord.y * 32.0), tick = floor(Time * 16.0);
    vec2 uv = texCoord;
    if (hash(vec2(band, tick)) > 0.65) uv.x += (hash(vec2(band, tick + 0.1)) - 0.5) * 0.25;
    if (hash(vec2(floor(texCoord.y * 6.0), floor(Time * 8.0))) > 0.8) uv.x += (hash(vec2(band, tick + 0.7)) - 0.5) * 0.15;
    vec4 t = warp(uv);
    float s = (hash(vec2(band, tick + 0.2)) - 0.5) * 0.05;
    float r = warp(uv + vec2(s, 0.0)).r, b = warp(uv - vec2(s, 0.0)).b;
    emitGlow(vec3(r, t.g, b), vec3(0.0, 0.6, 1.0) * step(0.93, hash(vec2(band, tick + 0.4))) * t.a, t.a);
}