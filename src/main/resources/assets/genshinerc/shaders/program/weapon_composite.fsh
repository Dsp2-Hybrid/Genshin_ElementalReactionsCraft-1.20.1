#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;
uniform vec2 OutSize;
uniform float Time;
uniform int Mode;
uniform float GlowStrength;
uniform float DistortStrength;

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
float m(vec2 uv) { return clamp(texture(TrailSampler, uv).a * 2.0, 0.0, 1.0); }

void main() {
    vec2 px = 1.0 / OutSize;
    vec4 trail = texture(TrailSampler, texCoord);

    float c = m(texCoord);
    float wide = c;
    for (int i = 0; i < 8; i++) {
        float ang = float(i) * 0.7853982;
        wide = max(wide, m(texCoord + vec2(cos(ang), sin(ang)) * px * 8.0) * 0.8);
    }
    vec2 grad = vec2(m(texCoord + vec2(px.x * 3.0, 0.0)) - m(texCoord - vec2(px.x * 3.0, 0.0)),
                     m(texCoord + vec2(0.0, px.y * 3.0)) - m(texCoord - vec2(0.0, px.y * 3.0)));
    vec2 aspect = vec2(OutSize.x / OutSize.y, 1.0);
    vec2 n = vec2(noise(texCoord * aspect * 9.0 + Time * 2.0),
                  noise(texCoord * aspect * 9.0 - Time * 1.7 + 31.7)) - 0.5;

    vec2 off = vec2(0.0);
    if (Mode == 1)      off = vec2(sin(texCoord.y * 90.0 + Time * 12.0), cos(texCoord.x * 70.0 + Time * 10.0)) * 0.006 + n * 0.006;
    else if (Mode == 2) off = vec2(-grad.y, grad.x) * 0.05 + n * 0.008;
    else if (Mode == 3) off = grad * 0.04 * sin(Time * 14.0 + c * 6.0) + n * 0.004;
    else if (Mode == 4) off = -grad * 0.05 + n * 0.003;
    else if (Mode == 5) {
        float band = floor(texCoord.y * 60.0), t = floor(Time * 20.0);
        off = vec2(step(0.6, hash(vec2(band, t))) * (hash(vec2(band, t + 0.3)) - 0.5) * 0.06, 0.0);
    }
    else if (Mode >= 6) off = n * 0.004;
    off *= DistortStrength * wide;

    vec2 uv = clamp(texCoord + off, px, 1.0 - px);
    vec3 scene;
    if (Mode == 6) {
        vec2 sp = (normalize(grad + 1e-4) * 0.006 + vec2(0.003, 0.0)) * DistortStrength * wide;
        scene = vec3(texture(DiffuseSampler, uv + sp).r, texture(DiffuseSampler, uv).g, texture(DiffuseSampler, uv - sp).b);
    } else {
        scene = texture(DiffuseSampler, uv).rgb;
    }

    vec3 halo = vec3(0.0);
    for (int i = 0; i < 8; i++) {
        float ang = float(i) * 0.7853982 + 0.3927;
        vec2 d = vec2(cos(ang), sin(ang)) * px;
        halo += texture(TrailSampler, texCoord + d * 5.0).rgb
              + 0.6 * texture(TrailSampler, texCoord + d * 12.0).rgb;
    }
    halo /= 12.8;

    vec3 col = scene + trail.rgb + halo * GlowStrength * (0.9 + 0.1 * sin(Time * 6.0));
    fragColor = vec4(min(col, vec3(1.0)), 1.0);
}