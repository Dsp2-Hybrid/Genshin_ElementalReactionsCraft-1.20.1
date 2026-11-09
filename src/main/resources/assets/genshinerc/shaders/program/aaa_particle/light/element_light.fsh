#version 150
uniform sampler2D DepthSampler;
uniform sampler2D ColorSampler;
uniform mat4 IProjMat;
uniform vec4 LightPos;
uniform vec4 LightColor;
in vec2 texCoord;
out vec4 fragColor;

vec3 viewPos(vec2 uv, float depth) {
    vec4 v = IProjMat * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return v.xyz / v.w;
}

void main() {
    float depth = texture(DepthSampler, texCoord).r;
    vec3 pos = viewPos(texCoord, depth);
    vec3 n = normalize(cross(dFdx(pos), dFdy(pos)));
    if (dot(n, pos) > 0.0) n = -n;
    if (depth >= 1.0) discard;

    vec3 toLight = LightPos.xyz - pos;
    float d = length(toLight);
    float att = clamp(1.0 - d / LightPos.w, 0.0, 1.0);
    att *= att;
    if (att <= 0.0) discard;

    float ndl = max(dot(n, toLight / max(d, 1e-4)), 0.0) * 0.7 + 0.3;
    vec3 albedo = 0.15 + 0.85 * texture(ColorSampler, texCoord).rgb;
    fragColor = vec4(LightColor.rgb * albedo * att * ndl, 0.0);
}