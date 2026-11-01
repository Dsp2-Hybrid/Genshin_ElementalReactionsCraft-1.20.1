#version 150
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 grid = floor(texCoord * 40.0); vec2 c = (grid + 0.5) / 40.0;
    vec4 tex = texture(Sampler0, c);
    if (tex.a < 0.05) discard;
    float alpha = step(length(texCoord - c) * 40.0, dot(tex.rgb, vec3(0.299, 0.587, 0.114))) * tex.a;
    fragColor = vec4(tex.rgb * vertexColor.rgb, alpha * vertexColor.a);
}
