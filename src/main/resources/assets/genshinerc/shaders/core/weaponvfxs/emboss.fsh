#version 150
uniform sampler2D Sampler0;
uniform vec2 OutSize;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 px = 1.0 / OutSize;
    vec4 centerTex = texture(Sampler0, texCoord);
    if (centerTex.a < 0.05) discard;
    float tl = dot(texture(Sampler0, texCoord + vec2(-px.x, -px.y)).rgb, vec3(0.299, 0.587, 0.114));
    float br = dot(texture(Sampler0, texCoord + vec2(px.x, px.y)).rgb, vec3(0.299, 0.587, 0.114));
    float diff = (tl - br) * 3.0;
    vec3 col = centerTex.rgb + diff;
    fragColor = vec4(col * vertexColor.rgb * 8.0, centerTex.a * vertexColor.a);
}


