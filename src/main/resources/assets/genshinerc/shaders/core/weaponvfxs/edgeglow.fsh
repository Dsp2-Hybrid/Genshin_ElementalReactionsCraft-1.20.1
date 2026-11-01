#version 150
uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
float getLum(vec3 c) { return dot(c, vec3(0.299, 0.587, 0.114)); }
void main() {
    vec2 px = 1.5 / OutSize;
    vec4 centerTex = texture(DiffuseSampler, texCoord);
    vec3 finalColor = centerTex.rgb * vertexColor.rgb;
    if (centerTex.a < 0.05 || dot(finalColor, vec3(0.299, 0.587, 0.114)) < 0.08) discard;
    float l = getLum(texture(DiffuseSampler, texCoord - vec2(px.x, 0)).rgb);
    float r = getLum(texture(DiffuseSampler, texCoord + vec2(px.x, 0)).rgb);
    float u = getLum(texture(DiffuseSampler, texCoord - vec2(0, px.y)).rgb);
    float d = getLum(texture(DiffuseSampler, texCoord + vec2(0, px.y)).rgb);
    float edge = sqrt(pow(r - l, 2.0) + pow(d - u, 2.0));
    vec3 glow = vec3(0.2, 0.8, 1.0) * step(0.2, edge); 
    fragColor = vec4(finalColor + glow, centerTex.a * vertexColor.a);
}
