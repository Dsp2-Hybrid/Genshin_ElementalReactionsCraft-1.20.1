#version 150
uniform sampler2D Sampler0;
uniform float Time;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
float rand(vec2 co){ return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453); }
void main() {
    float slice = floor(texCoord.y * 40.0);
    float glitch = step(0.4, rand(vec2(slice, floor(Time * 20.0))));
    vec2 uv = texCoord;
    if (glitch > 0.5) uv.x += (rand(vec2(slice, Time)) - 0.5) * 0.6;
    vec4 col = texture(Sampler0, uv);
    if (col.a < 0.05) discard;
    fragColor = col * vertexColor;
}
