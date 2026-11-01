#version 150
uniform sampler2D Sampler0;
uniform vec2 OutSize;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 px = 1.0 / OutSize;
    vec4 c = texture(Sampler0, texCoord);
    if (c.a < 0.05) discard;
    float diff = (dot(texture(Sampler0, texCoord + vec2(-px.x, -px.y)).rgb, vec3(0.299, 0.587, 0.114)) - dot(texture(Sampler0, texCoord + vec2(px.x, px.y)).rgb, vec3(0.299, 0.587, 0.114))) * 3.0;
    fragColor = vec4((c.rgb + diff) * vertexColor.rgb, c.a * vertexColor.a);
}
