#version 150
uniform sampler2D DiffuseSampler;
uniform float Time;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 p = texCoord - 0.5;
    float radius = length(p);
    float angle = atan(p.y, p.x) + Time * 2.0;
    float segments = 6.0;
    angle = mod(angle, 6.283185 / segments);
    if (angle > 3.14159 / segments) angle = (3.14159 / segments) - angle;
    vec2 newUV = vec2(cos(angle), sin(angle)) * radius + 0.5;
    vec4 tex = texture(DiffuseSampler, newUV);
    vec3 finalColor = tex.rgb * vertexColor.rgb;
    if (tex.a < 0.05 || dot(finalColor, vec3(0.299, 0.587, 0.114)) < 0.08) discard;
    float edge = smoothstep(0.5, 0.2, radius);
    fragColor = vec4(finalColor, tex.a * vertexColor.a * edge);
}
