#version 330 core
layout(location = 0) in vec2 vertex;

uniform mat4 projection;
uniform vec2 offset;
uniform float scale;

void main() {
    vec2 pos = vertex * scale + offset;
    gl_Position = projection * vec4(pos, 0.0, 1.0);
}
