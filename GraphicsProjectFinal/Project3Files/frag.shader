#version 430

in vec3 vNormal, vLightDir, vVertPos, vHalfVec;
in vec4 shadow_coord;
flat in int shaderFlag;
in vec2 tc;

out vec4 fragColor;

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
	if(shaderFlag == 1){ //for pass one
		
	}
	else if(shaderFlag == 2){ //for pass 2 with lights and shadows
		vec3 L = normalize(vLightDir);
		vec3 N = normalize(vNormal);
		vec3 V = normalize(-vVertPos);
		vec3 H = normalize(vHalfVec);
	
		float inShadow = textureProj(shadowTex, shadow_coord);
		
		vec4 texC = texture(t,tc);
	
		fragColor = globalAmbient * material.ambient
					+ light.ambient * material.ambient * texC;
		//fragColor = vec4(1.0, 0.0, 0.0, 1.0);
	
		if (inShadow != 0.0)
		{	//fragColor = vec4(1.0,1.0,0.0,1.0);
			fragColor += light.diffuse * material.diffuse * texC * max(dot(L,N),0.0)
					+ light.specular * material.specular * texC
					* pow(max(dot(H,N),0.0),material.shininess*3.0);
		}
	}
	else if(shaderFlag == 3){ //for pass light cube
		fragColor = vec4(1.0, 1.0, 0.0, 1.0);
	}
	else{ //for skybox
		fragColor = texture(s,tc);
	}
}
