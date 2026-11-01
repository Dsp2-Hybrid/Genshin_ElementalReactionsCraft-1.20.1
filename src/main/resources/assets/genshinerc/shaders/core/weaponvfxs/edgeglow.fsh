#version 150
uniform sampler2D Sampler0;
uniform vec2 OutSize;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
float getLum(vec3 c) { return dot(c, vec3(0.299, 0.587, 0.114)); }
void main() {
    vec2 px = 2.0 / OutSize;
    vec4 c = texture(Sampler0, texCoord);
    if (c.a < 0.05) discard;
    float edge = sqrt(pow(getLum(texture(Sampler0, texCoord + vec2(px.x, 0)).rgb) - getLum(texture(Sampler0, texCoord - vec2(px.x, 0)).rgb), 2.0) + pow(getLum(texture(Sampler0, texCoord + vec2(0, px.y)).rgb) - getLum(texture(Sampler0, texCoord - vec2(0, px.y)).rgb), 2.0));
    fragColor = vec4((c.rgb * vertexColor.rgb + vec3(0.2, 0.9, 1.0) * step(0.1, edge) * 2.0), c.a * vertexColor.a);
}
