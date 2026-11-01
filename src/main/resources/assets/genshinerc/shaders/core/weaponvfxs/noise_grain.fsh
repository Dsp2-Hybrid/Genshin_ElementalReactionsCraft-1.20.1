#version 150
uniform sampler2D Sampler0;
uniform float Time;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
float rand(vec2 co) { return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453); }
void main() {
    vec4 tex = texture(Sampler0, texCoord);
    if (tex.a < 0.05) discard;
    fragColor = vec4((tex.rgb + rand(texCoord + fract(Time)) * 0.3) * vertexColor.rgb, tex.a * vertexColor.a);
}
