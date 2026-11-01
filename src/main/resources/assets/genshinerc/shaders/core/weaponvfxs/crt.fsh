#version 150
uniform sampler2D DiffuseSampler;
uniform float Time;
uniform vec2 OutSize;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    float scanline = sin(texCoord.y * OutSize.y * 3.14159);
    scanline = smoothstep(0.0, 1.0, scanline * 0.5 + 0.5);
    float flicker = 1.0 - (sin(Time * 50.0) * 0.05);
    vec4 col = texture(DiffuseSampler, texCoord);
    col.rgb *= mix(0.5, 1.0, scanline) * flicker;
    fragColor = col * vertexColor;
}
