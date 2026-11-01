#version 150
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
float lum(vec2 uv) { return dot(texture(Sampler0, uv).rgb, vec3(0.299,0.587,0.114)); }
void main() {
    vec2 px = vec2(1.0/64.0, 0.0);
    vec4 c = texture(Sampler0, texCoord);
    if (c.a < 0.05) discard;
    float gx = lum(texCoord+px.xy) + 2.0*lum(texCoord+px.xx) + lum(texCoord+px.xy) - lum(texCoord-px.xy) - 2.0*lum(texCoord-px.xx) - lum(texCoord-px.xy);
    float gy = lum(texCoord+px.yx) + 2.0*lum(texCoord+px.xx) + lum(texCoord+px.yx) - lum(texCoord-px.yx) - 2.0*lum(texCoord-px.xx) - lum(texCoord-px.yx);
    fragColor = vec4(c.rgb * vertexColor.rgb * (0.5 + clamp(sqrt(gx*gx+gy*gy), 0.0, 1.5)), c.a * vertexColor.a);
}
