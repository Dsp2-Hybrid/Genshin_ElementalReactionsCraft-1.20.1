#version 150
uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 px = 1.0 / OutSize;
    float tl = dot(texture(DiffuseSampler, texCoord + vec2(-px.x, -px.y)).rgb, vec3(0.299, 0.587, 0.114));
    float br = dot(texture(DiffuseSampler, texCoord + vec2(px.x, px.y)).rgb, vec3(0.299, 0.587, 0.114));
    float diff = (tl - br) * 3.0;
    vec3 col = texture(DiffuseSampler, texCoord).rgb + diff;
    fragColor = vec4(col, 1.0) * vertexColor;
}
