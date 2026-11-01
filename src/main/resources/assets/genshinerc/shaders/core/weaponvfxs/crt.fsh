#version 150
uniform sampler2D Sampler0;
uniform float Time;
uniform vec2 OutSize;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec4 col = texture(Sampler0, texCoord);
    if (col.a < 0.05) discard;
    col.rgb *= mix(0.3, 1.0, smoothstep(0.0, 1.0, sin(texCoord.y * OutSize.y * 3.14159) * 0.5 + 0.5)) * (1.0 - sin(Time * 50.0) * 0.1);
    fragColor = col * vertexColor;
}
