#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D TrailSampler;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec2 p = texCoord - 0.5;
    float r2 = dot(p, p);
    vec2 uv = 0.5 + p * (1.0 + r2 * 3.0);
    if(uv.x<0.0||uv.x>1.0||uv.y<0.0||uv.y>1.0) discard;
    vec3 scene = texture(DiffuseSampler, uv).rgb;
    vec3 trail = texture(TrailSampler, uv).rgb;
    vec3 blended = 1.0 - (1.0 - scene) * (1.0 - trail);
    fragColor = vec4(blended, 1.0);
}
