#version 150
uniform sampler2D DiffuseSampler;
uniform float Time;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
float rand(vec2 co){ return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453); }
void main() {
    float slice = floor(texCoord.y * 20.0);
    float glitch = step(0.7, rand(vec2(slice, floor(Time * 10.0))));
    vec2 uv = texCoord;
    if (glitch > 0.5) uv.x += (rand(vec2(slice, Time)) - 0.5) * 0.2;
    vec4 col = texture(DiffuseSampler, uv);
    vec3 finalColor = col.rgb * vertexColor.rgb;
    if (col.a < 0.05 || dot(finalColor, vec3(0.299, 0.587, 0.114)) < 0.08) discard;
    fragColor = vec4(finalColor, col.a * vertexColor.a);
}
