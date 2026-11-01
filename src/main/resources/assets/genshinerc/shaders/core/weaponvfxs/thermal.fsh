#version 150
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec4 tex = texture(Sampler0, texCoord);
    if (tex.a < 0.05) discard;
    float l = dot(tex.rgb, vec3(0.299, 0.587, 0.114));
    vec3 col = l < 0.5 ? mix(vec3(0.0,0.0,0.8), vec3(0.6,0.0,0.6), l*2.0) : mix(vec3(0.6,0.0,0.6), vec3(1.0,1.0,0.8), (l-0.5)*2.0);
    fragColor = vec4(col * vertexColor.a, tex.a * vertexColor.a);
}
