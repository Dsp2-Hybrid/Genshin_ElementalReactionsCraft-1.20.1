#version 150
uniform sampler2D Sampler0;
uniform float Time;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
float rand(vec2 co){ return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453); }
void main() {
    float slice = floor(texCoord.y * 30.0);
    vec2 uv = texCoord;
    if (step(0.5, rand(vec2(slice, floor(Time * 15.0)))) > 0.5) uv.x += (rand(vec2(slice, Time)) - 0.5) * 0.3;
    vec4 tex = texture(Sampler0, uv);
    if (tex.a < 0.05) discard; fragColor = tex * vertexColor;
}
