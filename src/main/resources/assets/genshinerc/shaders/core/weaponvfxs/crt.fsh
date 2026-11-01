#version 150
uniform sampler2D DiffuseSampler;
uniform float Time;
uniform vec2 OutSize;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec4 col = texture(DiffuseSampler, texCoord);
    vec3 finalColor = col.rgb * vertexColor.rgb;
    if (col.a < 0.05 || dot(finalColor, vec3(0.299, 0.587, 0.114)) < 0.08) discard;
    float scanline = sin(texCoord.y * OutSize.y * 3.14159);
    scanline = smoothstep(0.0, 1.0, scanline * 0.5 + 0.5);
    float flicker = 1.0 - (sin(Time * 50.0) * 0.05);
    finalColor *= mix(0.5, 1.0, scanline) * flicker;
    fragColor = vec4(finalColor, col.a * vertexColor.a);
}
