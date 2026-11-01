#version 150
uniform sampler2D Sampler0;
uniform vec2 OutSize;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
float getLum(vec3 c) { return dot(c, vec3(0.299, 0.587, 0.114)); }
void main() {
    vec2 px = 5.0 / OutSize;
    vec4 centerTex = texture(Sampler0, texCoord);
    if (centerTex.a < 0.05) discard;
    float l = getLum(texture(Sampler0, texCoord - vec2(px.x, 0)).rgb);
    float r = getLum(texture(Sampler0, texCoord + vec2(px.x, 0)).rgb);
    float u = getLum(texture(Sampler0, texCoord - vec2(0, px.y)).rgb);
    float d = getLum(texture(Sampler0, texCoord + vec2(0, px.y)).rgb);
    float edge = sqrt(pow(r - l, 2.0) + pow(d - u, 2.0));
    vec3 glow = vec3(0.1, 0.8, 1.0) * step(0.1, edge) * 6.0; 
    fragColor = vec4((centerTex.rgb * vertexColor.rgb + glow), centerTex.a * vertexColor.a);
}
