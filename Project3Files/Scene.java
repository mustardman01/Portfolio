package finalProject;

import graphicslib3D.*;
import graphicslib3D.light.*;
import graphicslib3D.shape.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.nio.*;
import javax.swing.*;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CCW;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_LINEAR_MIPMAP_LINEAR;
import static com.jogamp.opengl.GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE1;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES3.GL_DEPTH;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.common.nio.Buffers;

@SuppressWarnings("serial")
public class Scene extends JFrame implements GLEventListener, KeyListener
{	private GLCanvas myCanvas;
	private Material thisMaterial;
	private String[] vertShaderSource, fragShaderSource;
	private int rendering_program;
	private int vao[] = new int[1];
	private int vbo[] = new int[9];
	private int mv_location, proj_location, n_location;
	private float aspect;
	private float orbitSpeed = 1f; //higher values mean higher rotation speed
	private int numSphereVertices;
	
	// location of sphere and camera
	private Point3D sphereLoc = new Point3D(0, 0, -1);
	private Point3D cameraLoc = new Point3D(0, 0, 15);
	private Point3D lightLoc = new Point3D(9, 8, 9);
	
	private Matrix3D m_matrix = new Matrix3D();
	private Matrix3D v_matrix = new Matrix3D();
	private Matrix3D mv_matrix = new Matrix3D();
	private Matrix3D proj_matrix = new Matrix3D();
	
	// light stuff
	private float [] globalAmbient = new float[] { 0.2f, 0.2f, 0.2f, 1.0f };
	private PositionalLight currentLight = new PositionalLight();
	
	// shadow stuff
	private int scSizeX, scSizeY;
	private int [] shadow_tex = new int[1];
	private int [] shadow_buffer = new int[1];
	private Matrix3D lightV_matrix = new Matrix3D();
	private Matrix3D lightP_matrix = new Matrix3D();
	private Matrix3D shadowMVP1 = new Matrix3D();
	private Matrix3D shadowMVP2 = new Matrix3D();
	private Matrix3D b = new Matrix3D();

	// model stuff
	private Sphere mySphere = new Sphere(64);
	
	private float lightXOffset, lightYOffset, lightZOffset, cameraXOffset, cameraYOffset, cameraZOffset;
	private float cameraXRotate, cameraYRotate, cameraZRotate;
	private int lightFlag = 0;
	
	private int skyboxTexture, earthTexture, moonTexture;
	private Texture skyboxTex, earthTex, moonTex;
	
	private int shuttleTexture;
	private Texture joglShuttleTexture;
		
	private int numObjVertices;
	private ImportedModel myObj;
	
	private float rotY;
	
	public Scene(){
		setTitle("Project 3");
		setSize(1600, 1600);
		GLProfile profile = GLProfile.get(GLProfile.GL4);
		GLCapabilities capabilities = new GLCapabilities(profile);
		myCanvas = new GLCanvas(capabilities);
		myCanvas.addGLEventListener(this);
		getContentPane().add(myCanvas);
		addKeyListener(this);
		setVisible(true);
		FPSAnimator animator = new FPSAnimator(myCanvas, 60);
		animator.start();
	}
	
	public void init(GLAutoDrawable drawable){
		GL4 gl = (GL4) GLContext.getCurrentGL();
	
		myObj = new ImportedModel("../shuttle.obj");
	
		createShaderPrograms();
		setupVertices();
		setupShadowBuffers();
				
		b.setElementAt(0,0,0.5);b.setElementAt(0,1,0.0);b.setElementAt(0,2,0.0);b.setElementAt(0,3,0.5f);
		b.setElementAt(1,0,0.0);b.setElementAt(1,1,0.5);b.setElementAt(1,2,0.0);b.setElementAt(1,3,0.5f);
		b.setElementAt(2,0,0.0);b.setElementAt(2,1,0.0);b.setElementAt(2,2,0.5);b.setElementAt(2,3,0.5f);
		b.setElementAt(3,0,0.0);b.setElementAt(3,1,0.0);b.setElementAt(3,2,0.0);b.setElementAt(3,3,1.0f);
		
		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		
		Material earthMaterial = new Material();
		float[]amb = new float[]{.2f, .2f, .2f,1f};
		float[] dif = new float[]{.7f, .7f, .7f, 1f};
		float[] spec = new float[]{.5f, .5f, .5f, 1f};
		earthMaterial.setAmbient(amb);
		earthMaterial.setDiffuse(dif);
		earthMaterial.setSpecular(spec);
		earthMaterial.setShininess(10.178125f);
		
		thisMaterial = earthMaterial;
		
		joglShuttleTexture = loadTexture("project3Files/spstob_1.jpg");
		shuttleTexture = joglShuttleTexture.getTextureObject();
		gl.glBindTexture(GL_TEXTURE_2D, shuttleTexture);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glGenerateMipmap(GL_TEXTURE_2D);
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")){
			float aniso[] = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, aniso, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, aniso[0]);
		}
		
		
		skyboxTex = loadTexture("project3Files/skybox3.jpg");
		skyboxTexture = skyboxTex.getTextureObject();
		gl.glBindTexture(GL_TEXTURE_2D, skyboxTexture);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glGenerateMipmap(GL_TEXTURE_2D);
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")){
			float aniso[] = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, aniso, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, aniso[0]);
		}
			
		earthTex = loadTexture("project3Files/earth.jpg");
		earthTexture = earthTex.getTextureObject();
		// apply mipmapping and anisotropic filtering to moon surface texture
		gl.glBindTexture(GL_TEXTURE_2D, earthTexture);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glGenerateMipmap(GL_TEXTURE_2D);
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")){
			float aniso[] = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, aniso, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, aniso[0]);
		}
		
		moonTex = loadTexture("project3Files/moon.jpg");
		moonTexture = moonTex.getTextureObject();
		gl.glBindTexture(GL_TEXTURE_2D, moonTexture);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glGenerateMipmap(GL_TEXTURE_2D);
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")){
			float aniso[] = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, aniso, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, aniso[0]);
		}
	}

	public void display(GLAutoDrawable drawable){
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		lightLoc.setX(9+lightXOffset);
		lightLoc.setY(8+lightYOffset);
		lightLoc.setZ(9+lightZOffset);
		currentLight.setPosition(lightLoc);
		
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		proj_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);
		
		float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);

		gl.glBindFramebuffer(GL_FRAMEBUFFER, shadow_buffer[0]);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadow_tex[0], 0);
	
		gl.glDrawBuffer(GL_NONE);
		gl.glEnable(GL_DEPTH_TEST);

		gl.glEnable(GL_POLYGON_OFFSET_FILL);	// for reducing
		gl.glPolygonOffset(2.0f, 4.0f);			//  shadow artifacts

		passOne();
		
		gl.glDisable(GL_POLYGON_OFFSET_FILL);	// artifact reduction, continued
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
	
		gl.glDrawBuffer(GL_FRONT);
		
		passTwo();
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passOne(){
		GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glUseProgram(rendering_program);
		
		Point3D origin = new Point3D(0.0, 0.0, 0.0);
		Vector3D up = new Vector3D(0.0, 1.0, 0.0);
		lightV_matrix.setToIdentity();
		lightP_matrix.setToIdentity();
	
		lightV_matrix = lookAt(currentLight.getPosition(), origin, up);	// vector from light to origin
		lightP_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);

		int shadow_location = gl.glGetUniformLocation(rendering_program, "shadowMVP");
		gl.glClear(GL_DEPTH_BUFFER_BIT);

		// ---- draw the sphere
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(sphereLoc.getX(),sphereLoc.getY(),sphereLoc.getZ());
		m_matrix.scale(2.5, 2.5, 2.5);

		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);

		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		int texFlagLocation = gl.glGetUniformLocation(rendering_program, "texFlag");
		gl.glUniform1i(texFlagLocation, 1);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVertices);
			
		//draw the moon
		m_matrix.setToIdentity();
		m_matrix.translate(0, 0, -1);
		double x = (double)(System.currentTimeMillis())*orbitSpeed/1000f;
		x = x/30;
		x = x*12;
		m_matrix.translate(8*Math.sin(x), 0, 8*Math.cos(x));
		m_matrix.scale(.45,.45,.45);
		
		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);

		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		gl.glUniform1i(texFlagLocation, 1);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVertices);	
		
		//draw the shuttle
		m_matrix.setToIdentity();
		m_matrix.translate(0, 0, -1);
		x = x*2.3;
		m_matrix.translate(4*Math.sin(x), 0, 4*Math.cos(x));
		m_matrix.rotate(0.0, -90.0, 270.0);
		m_matrix.scale(.25,.25,.25);
				
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);

		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(),0);
		texFlagLocation = gl.glGetUniformLocation(rendering_program, "texFlag");
		gl.glUniform1i(texFlagLocation, 1);
		
		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);

		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		gl.glUniform1i(texFlagLocation, 1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDepthFunc(GL_LEQUAL);
		int numVerts = myObj.getVertices().length;
		gl.glDrawArrays(GL_TRIANGLES, 0, numVerts);
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passTwo(){
		GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glUseProgram(rendering_program);
		
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		float depthClearVal[] = new float[1];
		depthClearVal[0] = 1.0f;
		gl.glClearBufferfv(GL_DEPTH, 0, depthClearVal,0);
		
		Point3D cameraLocation = new Point3D(cameraLoc.getX() + cameraXOffset,cameraLoc.getY() + cameraYOffset,cameraLoc.getZ() + cameraZOffset);
		Point3D lookAtPoint = new Point3D(cameraXRotate, cameraYRotate, cameraZRotate);
		Vector3D up = new Vector3D(0.0,1.0,0.0);
		
		v_matrix.setToIdentity();
		v_matrix = lookAt(cameraLocation, lookAtPoint, up);
		lightP_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);		
		
		mv_location = gl.glGetUniformLocation(rendering_program, "mv_matrix");
		proj_location = gl.glGetUniformLocation(rendering_program, "proj_matrix");
		n_location = gl.glGetUniformLocation(rendering_program, "normalMat");
		int texFlagLocation = gl.glGetUniformLocation(rendering_program, "texFlag");	
		int shadow_location = gl.glGetUniformLocation(rendering_program,  "shadowMVP");

		installLights(v_matrix);
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(9+lightXOffset,8+lightYOffset,9+lightZOffset);
		m_matrix.scale(.05,.05, .05);
						
		// build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);

		//  put the MV and PROJ matrices into the corresponding uniforms
		mv_location = gl.glGetUniformLocation(rendering_program, "mv_matrix");
		proj_location = gl.glGetUniformLocation(rendering_program, "proj_matrix");
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniform1i(texFlagLocation, 3);
						
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);

		// draw the earth
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(sphereLoc.getX(),sphereLoc.getY(),sphereLoc.getZ());
		m_matrix.scale(3, 3, 3);
		m_matrix.rotateY(rotY);
		rotY = rotY + 0.05f;

		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);

		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniform1i(texFlagLocation, 2);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		// set up normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, earthTexture); // texture

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDepthFunc(GL_LEQUAL);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVertices);
			
		//draw moon
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(0, 0, -1);
		double x = (double)(System.currentTimeMillis())*orbitSpeed/1000f;
		x = x/30;
		x = x*12;
		m_matrix.translate(8*Math.sin(x), 0, 8*Math.cos(x));
		m_matrix.scale(.6,.6,.6);

		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);

		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniform1i(texFlagLocation, 2);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, moonTexture); // texture

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVertices);
		
		//draw the shuttle
		m_matrix.setToIdentity();
		m_matrix.translate(0, 0, -1);
		x = x*2.3;		
		m_matrix.translate(4*Math.sin(x), 0, 4*Math.cos(x));
		m_matrix.rotate(0.0, -90.0, 270.0);
		m_matrix.scale(.35,.35,.35);
		
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);

		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(),0);
		texFlagLocation = gl.glGetUniformLocation(rendering_program, "texFlag");
		gl.glUniform1i(texFlagLocation, 2);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, shuttleTexture); // texture

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDepthFunc(GL_LEQUAL);
		int numVerts = myObj.getVertices().length;
		gl.glDrawArrays(GL_TRIANGLES, 0, numVerts);
		
		
		//make the skybox
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(cameraLoc.getX() + cameraXOffset,cameraLoc.getY() + cameraYOffset,cameraLoc.getZ() + cameraZOffset);
		m_matrix.scale(100, 100, 100);
		
		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);

		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniform1i(texFlagLocation, 4);
				
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
				
		// set up texture coordinates buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		// activate the skybox texture
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, skyboxTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);	// cube is CW, but we are viewing the inside
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
	}
	
	public void setupShadowBuffers(){
		GL4 gl = (GL4) GLContext.getCurrentGL();
		scSizeX = myCanvas.getWidth();
		scSizeY = myCanvas.getHeight();
	
		gl.glGenFramebuffers(1, shadow_buffer, 0);
	
		
		gl.glGenTextures(1, shadow_tex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
						scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	}

	// -----------------------------
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height){
		setupShadowBuffers();
	}

	private void setupVertices(){
		GL4 gl = (GL4) GLContext.getCurrentGL();

		Vertex3D[] vertices = mySphere.getVertices();
		int[] indices = mySphere.getIndices();
	
		float[] fvalues = new float[indices.length*3];
		float[] tvalues = new float[indices.length*2];
		float[] nvalues = new float[indices.length*3];
		float[] TANvals = new float[indices.length*3];
	
		for (int i=0; i<indices.length; i++){
			fvalues[i*3] = (float) (vertices[indices[i]]).getX();
			fvalues[i*3+1] = (float) (vertices[indices[i]]).getY();
			fvalues[i*3+2] = (float) (vertices[indices[i]]).getZ();
			tvalues[i*2] = (float) (vertices[indices[i]]).getS();
			tvalues[i*2+1] = (float) (vertices[indices[i]]).getT();
			nvalues[i*3] = (float) (vertices[indices[i]]).getNormalX();
			nvalues[i*3+1]= (float)(vertices[indices[i]]).getNormalY();
			nvalues[i*3+2]=(float) (vertices[indices[i]]).getNormalZ();

			TANvals[i*3] = (float) (vertices[indices[i]]).getTangent().getX();
			TANvals[i*3+1] = (float) (vertices[indices[i]]).getTangent().getY();
			TANvals[i*3+2] = (float) (vertices[indices[i]]).getTangent().getZ();
		}
	
		float[] cube_vertices = {
				-1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
				1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f,
				1.0f, -1.0f, -1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
				1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
				1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
				-1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
				-1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
				-1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
				-1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,
				1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,
				-1.0f,  1.0f, -1.0f, 1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f,
				1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f
		};

		float[] cube_texture_coord ={
					.25f,  .666666666f, .25f, .3333333333f, .5f, .3333333333f,	// front face lower left
					.5f, .333333333333f, .5f,  .66666666666f, .25f,  .66666666666f,	// front face upper right
					.5f, .3333333333f, .75f, .33333333333f,  .5f,  .6666666666f,	// right face lower left
					.75f, .33333333333f,  .75f,  .66666666666f, .5f,  .6666666666f,	// right face upper right
					.75f, .3333333333f,  1.0f, .3333333333f, .75f,  .66666666666f,	// back face lower
					1.0f, .3333333333f, 1.0f,  .6666666666f, .75f,  .6666666666f,	// back face upper
					0.0f, .333333333f,  .25f, .333333333f, 0.0f,  .666666666f,	// left face lower
					.25f, .333333333f, .25f,  .666666666f, 0.0f,  .666666666f,	// left face upper
					.25f, 0.0f,  .5f, 0.0f,  .5f, .333333333f,			// bottom face front
					.5f, .333333333f, .25f, .333333333f, .25f, 0.0f,		// bottom face back
					.25f,  .666666666f, .5f,  .666666666f, .5f,  1.0f,		// top face back
					.5f,  1.0f,  .25f,  1.0f, .25f,  .666666666f			// top face front
			};
	
		
		Vertex3D[] objvertices = myObj.getVertices();
		numObjVertices = myObj.getNumVertices();
		
		float[] objpvalues = new float[numObjVertices*3];
		float[] objtvalues = new float[numObjVertices*2];
		float[] objnvalues = new float[numObjVertices*3];
		
		for (int i=0; i<numObjVertices; i++){
			objpvalues[i*3]   = (float) (objvertices[i]).getX();
			objpvalues[i*3+1] = (float) (objvertices[i]).getY();
			objpvalues[i*3+2] = (float) (objvertices[i]).getZ();
			objtvalues[i*2]   = (float) (objvertices[i]).getS();
			objtvalues[i*2+1] = (float) (objvertices[i]).getT();
			objnvalues[i*3]   = (float) (objvertices[i]).getNormalX();
			objnvalues[i*3+1] = (float) (objvertices[i]).getNormalY();
			objnvalues[i*3+2] = (float) (objvertices[i]).getNormalZ();
		}
	
	
		numSphereVertices = indices.length;
	
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);
	
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(fvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);
	
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4, norBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer tanBuf = Buffers.newDirectFloatBuffer(TANvals);
		gl.glBufferData(GL_ARRAY_BUFFER, tanBuf.limit()*4, tanBuf, GL_STATIC_DRAW);
	
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		FloatBuffer cubeVertBuf = Buffers.newDirectFloatBuffer(cube_vertices);
		gl.glBufferData(GL_ARRAY_BUFFER, cubeVertBuf.limit()*4, cubeVertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		FloatBuffer	cubeTexBuf = Buffers.newDirectFloatBuffer(cube_texture_coord);
		gl.glBufferData(GL_ARRAY_BUFFER, cubeTexBuf.limit()*4, cubeTexBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		FloatBuffer objvertBuf = Buffers.newDirectFloatBuffer(objpvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, objvertBuf.limit()*4, objvertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		FloatBuffer objtexBuf = Buffers.newDirectFloatBuffer(objtvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, objtexBuf.limit()*4, objtexBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		FloatBuffer objnorBuf = Buffers.newDirectFloatBuffer(objnvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, objnorBuf.limit()*4,objnorBuf, GL_STATIC_DRAW);
	}
	
	private void installLights(Matrix3D v_matrix){
		GL4 gl = (GL4) GLContext.getCurrentGL();
	
		Material currentMaterial = new Material();
		currentMaterial = thisMaterial;
		
		currentLight.setAmbient(new float[]{.7f,.7f,.7f,1.0f});
		
		Point3D lightP = currentLight.getPosition();
		Point3D lightPv = lightP.mult(v_matrix);
		
		float [] currLightPos = new float[] { (float) lightPv.getX(),
			(float) lightPv.getY(),
			(float) lightPv.getZ() };

		// get the location of the global ambient light field in the shader
		int globalAmbLoc = gl.glGetUniformLocation(rendering_program, "globalAmbient");
	
		// set the current globalAmbient settings
		gl.glProgramUniform4fv(rendering_program, globalAmbLoc, 1, globalAmbient, 0);

		// get the locations of the light and material fields in the shader
		int ambLoc = gl.glGetUniformLocation(rendering_program, "light.ambient");
		int diffLoc = gl.glGetUniformLocation(rendering_program, "light.diffuse");
		int specLoc = gl.glGetUniformLocation(rendering_program, "light.specular");
		int posLoc = gl.glGetUniformLocation(rendering_program, "light.position");

		int MambLoc = gl.glGetUniformLocation(rendering_program, "material.ambient");
		int MdiffLoc = gl.glGetUniformLocation(rendering_program, "material.diffuse");
		int MspecLoc = gl.glGetUniformLocation(rendering_program, "material.specular");
		int MshiLoc = gl.glGetUniformLocation(rendering_program, "material.shininess");

		// set the uniform light and material values in the shader
		gl.glProgramUniform4fv(rendering_program, ambLoc, 1, currentLight.getAmbient(), 0);
		gl.glProgramUniform4fv(rendering_program, diffLoc, 1, currentLight.getDiffuse(), 0);
		gl.glProgramUniform4fv(rendering_program, specLoc, 1, currentLight.getSpecular(), 0);
		gl.glProgramUniform3fv(rendering_program, posLoc, 1, currLightPos, 0);
	
		gl.glProgramUniform4fv(rendering_program, MambLoc, 1, currentMaterial.getAmbient(), 0);
		gl.glProgramUniform4fv(rendering_program, MdiffLoc, 1, currentMaterial.getDiffuse(), 0);
		gl.glProgramUniform4fv(rendering_program, MspecLoc, 1, currentMaterial.getSpecular(), 0);
		gl.glProgramUniform1f(rendering_program, MshiLoc, currentMaterial.getShininess());
	}

	public static void main(String[] args) { new Scene(); }

	@Override
	public void dispose(GLAutoDrawable drawable){
		GL4 gl = (GL4) drawable.getGL();
		gl.glDeleteVertexArrays(1, vao, 0);
	}

//-----------------
	private void createShaderPrograms(){
		GL4 gl = (GL4) GLContext.getCurrentGL();
		vertShaderSource = GLSLUtils.readShaderSource("project3Files/vert.shader");
		fragShaderSource = GLSLUtils.readShaderSource("project3Files/frag.shader");

		int vertexShader = gl.glCreateShader(GL_VERTEX_SHADER);
		int fragmentShader = gl.glCreateShader(GL_FRAGMENT_SHADER);

		gl.glShaderSource(vertexShader, vertShaderSource.length, vertShaderSource, null, 0);
		gl.glShaderSource(fragmentShader, fragShaderSource.length, fragShaderSource, null, 0);

		gl.glCompileShader(vertexShader);
		gl.glCompileShader(fragmentShader);
		GLSLUtils.checkOpenGLError();
		GLSLUtils.printShaderLog(fragmentShader);
		GLSLUtils.printShaderLog(vertexShader);

		rendering_program = gl.glCreateProgram();
		gl.glAttachShader(rendering_program, vertexShader);
		gl.glAttachShader(rendering_program, fragmentShader);

		gl.glLinkProgram(rendering_program);
		GLSLUtils.printProgramLog(rendering_program);
	}

//------------------
	private Matrix3D perspective(float fovy, float aspect, float n, float f){
		float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
		float A = q / aspect;
		float B = (n + f) / (n - f);
		float C = (2.0f * n * f) / (n - f);
		Matrix3D r = new Matrix3D();
		r.setElementAt(0,0,A);
		r.setElementAt(1,1,q);
		r.setElementAt(2,2,B);
		r.setElementAt(3,2,-1.0f);
		r.setElementAt(2,3,C);
		r.setElementAt(3,3,0.0f);
		return r;
	}
	
	public Texture loadTexture(String textureFileName){
		Texture tex = null;
		try { 
			tex = TextureIO.newTexture(new File(textureFileName), false);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return tex;
	}

	private Matrix3D lookAt(Point3D eye, Point3D target, Vector3D y){
		Vector3D eyeV = new Vector3D(eye);
		Vector3D targetV = new Vector3D(target);
		Vector3D fwd = (targetV.minus(eyeV)).normalize();
		Vector3D side = (fwd.cross(y)).normalize();
		Vector3D up = (side.cross(fwd)).normalize();
		Matrix3D look = new Matrix3D();
		look.setElementAt(0,0, side.getX());
		look.setElementAt(1,0, up.getX());
		look.setElementAt(2,0, -fwd.getX());
		look.setElementAt(3,0, 0.0f);
		look.setElementAt(0,1, side.getY());
		look.setElementAt(1,1, up.getY());
		look.setElementAt(2,1, -fwd.getY());
		look.setElementAt(3,1, 0.0f);
		look.setElementAt(0,2, side.getZ());
		look.setElementAt(1,2, up.getZ());
		look.setElementAt(2,2, -fwd.getZ());
		look.setElementAt(3,2, 0.0f);
		look.setElementAt(0,3, side.dot(eyeV.mult(-1)));
		look.setElementAt(1,3, up.dot(eyeV.mult(-1)));
		look.setElementAt(2,3, (fwd.mult(-1)).dot(eyeV.mult(-1)));
		look.setElementAt(3,3, 1.0f);
		return(look);
	}
	
	public void keyTyped(KeyEvent e) {
		   
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		
		if(key == KeyEvent.VK_SPACE){
			if(lightFlag == 0){
				currentLight.setDiffuse(new float[]{0,0,0,1});
				currentLight.setSpecular(new float[]{0,0,0,1});
				lightFlag = 1;
			}
			else{
				currentLight.setDiffuse(new float[]{1,1,1,1});
				currentLight.setSpecular(new float[]{1,1,1,1});
				lightFlag = 0;
			}
		}
		
		if (key == KeyEvent.VK_J){
			cameraXRotate -= .1;
		}
		if (key == KeyEvent.VK_L){
			cameraXRotate += .1;
		}
		if (key == KeyEvent.VK_I){
			cameraYRotate += .1;
		}
		if (key == KeyEvent.VK_K){
			cameraYRotate -= .1;
		}
		if(key == KeyEvent.VK_U){
			cameraZRotate -= .1;
		}
		if(key == KeyEvent.VK_O){
			cameraZRotate += .1;
		}
		if (key == KeyEvent.VK_UP) {
	        cameraZOffset -= .1;
	        cameraZRotate -= .1;
	    }
	    if (key == KeyEvent.VK_DOWN) {
	    	cameraZOffset += .1;
	    	cameraZRotate += .1;
	    }
	    if(key == KeyEvent.VK_RIGHT){
	    	cameraXOffset += .1;
	    	cameraXRotate += .1;
	    }
	    if(key == KeyEvent.VK_LEFT){
	    	cameraXOffset -= .1;
	    	cameraXRotate -= .1;
	    }
	    if(key == KeyEvent.VK_SLASH){
	    	cameraYOffset += .1;
	    	cameraYRotate += .1;
	    }
	    if(key == KeyEvent.VK_PERIOD){
	    	cameraYOffset -= .1;
	    	cameraYRotate -= .1;
	    }
	    if(key == KeyEvent.VK_D){
	    	lightXOffset += .1;
	    }
	    if(key == KeyEvent.VK_A){
	    	lightXOffset -= .1;
	    }
	    if(key == KeyEvent.VK_W){
	    	lightZOffset += .1;
	    }
	    if(key == KeyEvent.VK_S){
	    	lightZOffset -= .1;
	    }
	    if(key == KeyEvent.VK_Q){
	    	lightYOffset -= .1;
	    }
	    if(key == KeyEvent.VK_E){
	    	lightYOffset += .1;
	    }
	}

	public void keyReleased(KeyEvent e) {
		
	}
}
