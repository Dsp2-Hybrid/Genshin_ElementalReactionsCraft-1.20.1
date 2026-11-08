#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;
uniform vec2 OutSize;
uniform float Time;
uniform int Mode;
uniform float GlowStrength;
uniform float DistortStrength;
uniform float DistortFreq;

in vec2 texCoord;
out vec4 fragColor;

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
float cov(vec2 uv) { return clamp(texture(TrailSampler, uv).a, 0.0, 1.0); }

void main() {
    vec2 px = 1.0 / OutSize;
    vec2 P = texCoord * OutSize;
    vec2 asp = vec2(OutSize.x / OutSize.y, 1.0);
    float t = Time;

    float m = cov(texCoord);
    for (int i = 0; i < 12; i++) {
        float a = float(i) * 0.5235988;
        float r = mod(float(i), 2.0) < 0.5 ? 8.0 : 16.0;
        m += cov(texCoord + vec2(cos(a), sin(a)) * r * px);
    }
    m = smoothstep(0.03, 0.5, m / 13.0 * 1.8);

    vec3 scene;
    if (Mode != 0 && m > 0.001) {
        vec2 g = vec2(cov(texCoord + vec2(10.0, 0.0) * px) - cov(texCoord - vec2(10.0, 0.0) * px),
                      cov(texCoord + vec2(0.0, 10.0) * px) - cov(texCoord - vec2(0.0, 10.0) * px));
        vec2 hp = texCoord * asp * 6.0 * DistortFreq + vec2(0.0, -t * 0.8);
        float n1 = fbm(hp + vec2(0.0, t * 0.4));
        float n2 = fbm(hp * 1.6 + vec2(11.3 - t * 0.5, 4.7));
        vec2 N = length(g) > 0.02 ? normalize(g) : normalize(vec2(n1, n2) - 0.5 + 1e-4);
        vec2 T = vec2(-N.y, N.x);

        vec2 off = (vec2(n1, n2) - 0.5) * 12.0;
        off.x += sin(P.y * 0.04 + t * 7.0 + n1 * 6.0) * 2.5;

        if      (Mode == 1)  off += T * sin(dot(P, T) * 0.05 * DistortFreq - t * 10.0) * 14.0;
        else if (Mode == 2)  off += N * (1.0 - m) * 40.0 + T * sin(t * 5.0 + dot(P, N) * 0.05) * 4.0;
        else if (Mode == 3)  off += T * sin(m * 10.0 * DistortFreq - t * 8.0) * 18.0;
        else if (Mode == 4)  off += T * sin(dot(P, T) * 0.06 * DistortFreq + t * 3.0) * 12.0 + N * cos(dot(P, N) * 0.06 - t * 4.0) * 12.0;
        else if (Mode == 5)  off += N * sin(m * 14.0 * DistortFreq - t * 16.0) * 14.0;
        else if (Mode == 6)  off += N * m * 26.0;
        else if (Mode == 7)  off += N * sin(t * 6.0) * 22.0;
        else if (Mode == 9) {
            float band = floor(P.y / 12.0), k = floor(t * 18.0 * DistortFreq);
            off.x += step(0.55, hash(vec2(band, k))) * (hash(vec2(band, k + 0.3)) - 0.5) * 90.0;
        }
        else if (Mode == 12) off *= 1.6;

        off *= DistortStrength * m;
        vec2 uv = clamp(texCoord + off * px, px, 1.0 - px);

        if (Mode == 8) {
            vec3 acc = vec3(0.0);
            for (int i = 0; i < 7; i++)
                acc += texture(DiffuseSampler, clamp(uv + T * (float(i) - 3.0) * 5.0 * m * DistortStrength * px, px, 1.0 - px)).rgb;
            scene = acc / 7.0;
        } else if (Mode == 11) {
            vec2 s = N * (6.0 + 4.0 * sin(t * 4.0)) * m * DistortStrength * px;
            scene = vec3(texture(DiffuseSampler, uv + s).r, texture(DiffuseSampler, uv).g, texture(DiffuseSampler, uv - s).b);
        } else {
            if (Mode == 10 && m > 0.3) uv = (floor(P / 10.0) + 0.5) * 10.0 * px;
            scene = texture(DiffuseSampler, uv).rgb;
        }
        scene += scene * max(n1 - 0.5, 0.0) * 0.25 * m;
    } else {
        scene = texture(DiffuseSampler, texCoord).rgb;
    }

    vec4 trail = texture(TrailSampler, texCoord);
    vec3 halo = vec3(0.0);
    for (int i = 0; i < 8; i++) {
        float a = float(i) * 0.7853982 + 0.3927;
        vec2 d = vec2(cos(a), sin(a)) * px;
        halo += texture(TrailSampler, texCoord + d * 5.0).rgb + 0.6 * texture(TrailSampler, texCoord + d * 12.0).rgb;
    }
    halo /= 12.8;

    vec3 col = scene + trail.rgb + halo * GlowStrength;
    fragColor = vec4(min(col, vec3(1.0)), 1.0);
}