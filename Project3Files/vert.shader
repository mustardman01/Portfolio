#version 430

layout (location=0) in vec3 vertPos;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertNormal;


out vec3 vNormal, vLightDir, vVertPos, vHalfVec; 
out vec4 shadow_coord;
flat out int shaderFlag;
out vec2 tc;

layout (binding=0) uniform sampler2D s;
layout (binding=1) uniform sampler2D t;

struct PositionalLight
{	vec4 ambient, diffuse, specular;
	vec3 position;
};
struct Material
{	vec4 ambient, diffuse, specular;   
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 normalMat;
uniform mat4 shadowMVP;
layout (binding=0) uniform sampler2DShadow shadowTex;
uniform int texFlag;

void main(void)
{	
	if(texFlag == 1){ //for pass one
		gl_Position = shadowMVP * vec4(vertPos,1.0);
		shaderFlag = 1;
	}
	else if(texFlag == 2){ //for pass two
		//output the vertex position to the rasterizer for interpolation
		vVertPos = (mv_matrix * vec4(vertPos,1.0)).xyz;
        
		//get a vector from the vertex to the light and output it to the rasterizer for interpolation
		vLightDir = light.position - vVertPos;

		//get a vertex normal vector in eye space and output it to the rasterizer for interpolation
		vNormal = (normalMat * vec4(vertNormal,1.0)).xyz;
	
		// calculate the half vector (L+V)
		vHalfVec = (vLightDir-vVertPos).xyz;
	
		shadow_coord = shadowMVP * vec4(vertPos,1.0);
	
		tc = texCoord;
		gl_Position = proj_matrix * mv_matrix * vec4(vertPos,1.0);
		shaderFlag = 2;
	}
	else if(texFlag == 3){ //for the light box
		gl_Position = proj_matrix * mv_matrix * vec4(vertPos,1.0);
		shaderFlag = 3;
	}
	else{ //for the skybox
		tc = texCoord;
		gl_Position = proj_matrix * mv_matrix * vec4(vertPos,1.0);
		shaderFlag = 4;
	}
}
