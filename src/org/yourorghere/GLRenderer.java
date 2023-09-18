package org.yourorghere;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.*;
import java.math.*;
import java.util.*;//�������
import java.io.*; //���� �����
import java.nio.ByteBuffer; //������ � �������������� � ������
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.image.*;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * GLRenderer.java <BR>
 * author: Brian Paul (converted to Java by Ron Cemer and Sven Goethel) <P>
 *
 * This version is equal to Brian Paul's version 1.2 1999/10/21
 */
public class GLRenderer implements GLEventListener {
    
    double ex=-10,ey=-10,ez=5,
           px=0,py=0,pz=0,
           speed=0.2,rot=0,drot=0.1,rad=7, rotz=0,
           lastex, lastey, lastez, g=0.001;
    //boolean start = true;
    
    public void SetTexture(GL gl, String filename) {
        File f = new File(filename); //���� �� �������� ������������ ��������
        try {
            BufferedImage img; //������ ���� ��������������� �����������
            img = ImageIO.read(f); //������ ����������� �� �����

            Raster r = img.getData(); //��������� ������ �� �����
            byte tex[];
            int w=r.getWidth(), h=r.getHeight(); //��������� ������ � ������ �� ������
            tex = new byte[w*h*3]; //������ ������� �������� ������������� ��� ������ ������

            double pixel[] = new double[3]; //������ �������� 3 �������� R,G,B, ������ ����������� ������
            int count = 0; //������� ��� �������
            for(int i=0; i<=w-1; i++) //������
            {
                for(int j=0; j<=h-1; j++) //������
                {
                    r.getPixel(i, j, pixel); //���������� �������� � ������
                    for(int k=0; k<=2; k++) //�� 0 �� 2, ������ ��� � � �
                    {
                        tex[count] = (byte)pixel[k]; //������ ��������� ��������� � ������ � ���������� ������
                        count++; 
                    }
                }
            }
            ByteBuffer btex = ByteBuffer.wrap(tex); //������������ ������ tex
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, w, h, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, btex);//���������� ����������� //����, �����(����������), ������, ������, ������, ������� ��������, ������
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR); //���������� ���������� //mag ��� ����� �������� ����� ����� ����, min ����� ����
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR); //� ���� �����������, ������������ ��������� ��������, ��������
            } 
            catch (IOException ex) {
                Logger.getLogger(GLRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
    
    void SetTreeTexture(GL gl, int num)
    {
        byte tex[];
        int w=256; //���������� �������� ������
        int h=256;
        
        try {
            File ftt = new File("tree"+num+".jpg"); //���� �� �������� ������������ ��������
            BufferedImage imgt; //������ ���� ��������������� �����������
            imgt = ImageIO.read(ftt); //������ ����������� �� �����
            Raster r = imgt.getData(); //��������� ������ �� �����
            w=r.getWidth(); //��������� ������ �� ������
            h=r.getHeight(); //��������� ������ �� ������
            tex = new byte[w*h*4]; //
            
            int pixel[] = new int[3]; //������ �������� 3 �������� R,G,B, ������ ����������� ������
            int count = 0; //������� ��� �������
            for(int i=0; i<w-1; i++) //������
            {
                for (int j=0; j<=h-1; j++) //������
                {
                    r.getPixel(i, j, pixel); //���������� �������� � ������
                    tex[count]=(byte)pixel[0]; //R //������ ������������ �����������
                    count++;
                    tex[count]=(byte)pixel[1]; //G
                    count++;
                    tex[count]=(byte)pixel[2]; //B
                    count++;
                    if ((pixel[0]>120) && (pixel[1]>120) && (pixel[2]>120))
                    {
                        tex[count]=0; //���� ������ �������, �� �������������� ����� ����� 0 - ������� �����������
                    }
                    else
                    {
                        tex[count]=(byte)255; //����� ����� 255 - ������� ���������
                    }
                    count++;
                }
            }
            ByteBuffer bbtex = ByteBuffer.wrap(tex); //������������ ������ tex
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, w, h, 0, GL.GL_RGBA,GL.GL_UNSIGNED_BYTE, bbtex);//���������� ����������� //����, �����(����������), ������, ������, ������, ������� ��������, ������
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR); //���������� ���������� //mag ��� ����� �������� ����� ����� ����, min ����� ����
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR); //� ���� �����������, ������������ ��������� ��������, ��������
            } catch (IOException ex) {
            Logger.getLogger(GLRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    void Tree(GL gl, float x, float y)
    {
        gl.glPushMatrix();
        gl.glTranslatef(x,y, 0);
        
        gl.glAlphaFunc(GL.GL_GREATER, 0.5f); //������� ����������, �� �������, � ������� ����� ������ ��� 0.5, ����� ��������� ��������� ����� �����, ��������� ���������
        gl.glEnable(GL.GL_ALPHA_TEST); //��������� ������ ������������                                  //0.5f = 128 byte
        gl.glBegin(GL.GL_QUADS); //���������� ������ � ������� ���������
            gl.glTexCoord2d(0, 0); gl.glVertex3f(0,-2.5f, 5); //������ ��������� ������
            gl.glTexCoord2d(0, 1); gl.glVertex3f(0, 2.5f, 5);
            gl.glTexCoord2d(1, 1); gl.glVertex3f(0, 2.5f, 0);
            gl.glTexCoord2d(1, 0); gl.glVertex3f(0,-2.5f, 0);
            
            gl.glTexCoord2d(0, 0); gl.glVertex3f(-2.5f, 0, 5); //������ ��������� ������
            gl.glTexCoord2d(0, 1); gl.glVertex3f( 2.5f, 0, 5);
            gl.glTexCoord2d(1, 1); gl.glVertex3f( 2.5f, 0, 0);
            gl.glTexCoord2d(1, 0); gl.glVertex3f(-2.5f, 0, 0);
        gl.glEnd();
        gl.glPopMatrix(); //����������� ������� ��������� ����� ����������
        gl.glDisable(GL.GL_ALPHA_TEST); //���������� ������ ������������
    }

    public void init(GLAutoDrawable drawable) {

        GL gl = drawable.getGL();
        GLU glu = new GLU();
        System.err.println("INIT GL IS: " + gl.getClass().getName());
        
        gl.glNewList(121, GL.GL_COMPILE);
            //����������
                double x,y,R=150,alpha=0,da;
                int n = 32;

                da=2*Math.PI/n;
                gl.glColor3f(1, 1, 1);
                SetTexture(gl, "sky1.jpg");
                gl.glBegin(GL.GL_QUAD_STRIP);
                    while(alpha<=Math.PI*2+da)
                    {
                        x=R*Math.cos(alpha);
                        y=R*Math.sin(alpha);
                        gl.glColor3f(1,1,1);
                        gl.glTexCoord2d(((Math.cos(alpha/2)+1)), 0); gl.glVertex3d(x, y, 0);
                        gl.glColor3f(1,1,1);
                        gl.glTexCoord2d(((Math.cos(alpha/2)+1)), 1); gl.glVertex3d(x, y, 100);
                        alpha=alpha+da;
                    }
                gl.glEnd();

                SetTexture(gl, "grass.jpg");
                gl.glColor3f(1, 1, 1);
                gl.glBegin(GL.GL_QUADS);
                    gl.glTexCoord2d(0, 0); gl.glVertex3f(-150, -150, 0);
                    gl.glTexCoord2d(50, 0); gl.glVertex3f(-150, 150, 0);
                    gl.glTexCoord2d(50, 50); gl.glVertex3f(100, 150, 0);
                    gl.glTexCoord2d(0, 50); gl.glVertex3f(150, -150, 0);
                gl.glEnd();

                SetTexture(gl, "sky1.jpg");    
                gl.glColor3f(1, 1f, 1);
                gl.glBegin(GL.GL_QUADS);
                    gl.glTexCoord2d(0, 0); gl.glVertex3f(-150, -150, 50);
                    gl.glTexCoord2d(2, 0); gl.glVertex3f(-150, 150, 50);
                    gl.glTexCoord2d(2, 2); gl.glVertex3f(150, 150, 50);
                    gl.glTexCoord2d(0, 2); gl.glVertex3f(150, -150, 50);
                gl.glEnd();
                
                gl.glPushMatrix();
                gl.glTranslatef(-10, -20, 0);
                
                SetTexture(gl, "brickwall.jpg");
                gl.glColor3f(1, 1f, 1);
                gl.glBegin(GL.GL_QUADS);
                    gl.glTexCoord2d(0, 0); gl.glVertex3f(0, 0, 0);
                    gl.glTexCoord2d(0, 1); gl.glVertex3f(3, 0, 0);
                    gl.glTexCoord2d(1, 1); gl.glVertex3f(3, 0, 1.8f);
                    gl.glTexCoord2d(1, 0); gl.glVertex3f(0, 0, 1.8f);
                    
                    gl.glTexCoord2d(0, 0); gl.glVertex3f(0, 0, 1.8f);
                    gl.glTexCoord2d(1, 0); gl.glVertex3f(0, 0, 0);
                    gl.glTexCoord2d(1, 1); gl.glVertex3f(0, 3, 0);
                    gl.glTexCoord2d(0, 1); gl.glVertex3f(0, 3, 2);
                    
                    gl.glTexCoord2d(0, 0); gl.glVertex3f(0, 3, 0);
                    gl.glTexCoord2d(0, 1); gl.glVertex3f(3, 3, 0);
                    gl.glTexCoord2d(1, 1); gl.glVertex3f(3, 3, 2);
                    gl.glTexCoord2d(1, 0); gl.glVertex3f(0, 3, 2);
                    
                    gl.glTexCoord2d(0, 0); gl.glVertex3f(3, 0, 1.8f);
                    gl.glTexCoord2d(1, 0); gl.glVertex3f(3, 0, 0);
                    gl.glTexCoord2d(1, 1); gl.glVertex3f(3, 3, 0);
                    gl.glTexCoord2d(0, 1); gl.glVertex3f(3, 3, 2);
                gl.glEnd();
                
                SetTexture(gl, "top.jpg");
                gl.glBegin(GL.GL_QUADS);
                    gl.glTexCoord2d(0, 0); gl.glVertex3f(-0.2f, -0.2f, 1.8f);
                    gl.glTexCoord2d(1, 0); gl.glVertex3f(3.2f, -0.2f, 1.8f);
                    gl.glTexCoord2d(1, 1); gl.glVertex3f(3.2f, 3.2f, 2);
                    gl.glTexCoord2d(0, 1); gl.glVertex3f(-0.2f, 3.2f, 2);
                gl.glEnd();
                
                SetTexture(gl, "door.jpg");
                gl.glBegin(GL.GL_QUADS);
                    gl.glTexCoord2d(1, 1); gl.glVertex3f(0.3f, 3.01f, 0);
                    gl.glTexCoord2d(1, 0); gl.glVertex3f(1.3f, 3.01f, 0);
                    gl.glTexCoord2d(0, 0); gl.glVertex3f(1.3f, 3.01f, 1.5f);
                    gl.glTexCoord2d(0, 1); gl.glVertex3f(0.3f, 3.01f, 1.5f);
                gl.glEnd();
                
                SetTexture(gl, "window.jpg");
                gl.glBegin(GL.GL_QUADS);
                    gl.glTexCoord2d(1, 1); gl.glVertex3f(2.8f, 3.01f, 0.5f);
                    gl.glTexCoord2d(1, 0); gl.glVertex3f(1.6f, 3.01f, 0.5f);
                    gl.glTexCoord2d(0, 0); gl.glVertex3f(1.6f, 3.01f, 1.5f);
                    gl.glTexCoord2d(0, 1); gl.glVertex3f(2.8f, 3.01f, 1.5f);
                gl.glEnd();
                gl.glPopMatrix();
                gl.glDisable(GL.GL_TEXTURE_2D);
             gl.glEndList();
             
             gl.glNewList(122, GL.GL_COMPILE);
                gl.glColor3f(1, 1, 1);
                for (int j=1; j<=5; j++) //��������� ���� ��������� ������� ��������
                {
                    SetTreeTexture(gl, j);
                    for (int i=1; i<=500; i++) //���������� ��������
                    {
                        Tree(gl, (float)Math.random()*300-140, (float)Math.random()*300-140); //��������� ����������
                    }
                } 
             gl.glEndList();
             
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////             
             gl.glNewList(120, GL.GL_COMPILE);
             
                try {                                //��������� ����������
                    File F = new File("c:/budka.obj"); //��� ������ ������ ������������ ��� ����������� �������
                    Scanner SC = new Scanner(F); //���� ���� �� ������, ��������� ����������, ������ ��� ��� ������, ������� �������� � ������
                    int nv=0; //���������� ������
                    String s;
                    String coord[]; //��������� ������ ��� �������� ��������� �� ������� ����� ��������� ������
                    String faces[]; //������ ��� ������ � �������
                    
                    double cx,cy,cz;
                    int face[];
                    face = new int [5]; //5 ������ ��� 4 ������� � f � ������ ������
                                                //���������� ������������ ������ ��� �� ����� ��� ������ ����������
                    ArrayList<Double> xa,ya,za; //� �������� ������������ ������, ��� ������������� �������(�������. ������� ��������)
                    
                    xa = new ArrayList(); //3 ������������ �������
                    ya = new ArrayList();
                    za = new ArrayList();
                    
                    //������ ������ �� �����
                    while(SC.hasNext()) //�� ��� ��� ���� ���� ����������� ������ �� �����, hasnext ����� �������
                    {
                        //��������� ������
                        s=SC.nextLine(); //��������� ������ �������� ������������
                        if((s.charAt(0) == 'v') && (s.charAt(1) == ' ')) //����� ���������� ������ � �������� �������. � ������ ����� �������. 
                        {                                                //������� � ����� �� ������� �� ������� � �������� vn
                            nv++; //������� � ��������� �������
                            coord = s.split(" "); //����� ��� ���������� ������ ����������� �������� �������� ��������� ������
                            xa.add(Double.parseDouble(coord[1])/15); //��������� �������������� ������ � ������������ �����
                            ya.add(Double.parseDouble(coord[2])/15); //����� ��� �������� //������� ��������� �������� ������ �������
                            za.add(Double.parseDouble(coord[3])/15); //���������� � ������ �� �������� ����� ����������
                        }
                        //��������� ������
                        if(s.charAt(0) == 'f') //����� ���������� ������ � �������� �������. � ������ ����� �������. 
                        {
                            faces=s.split(" "); //����� ��� ���������� ������ ����������� �������� �������� ��������� ������
                            gl.glColor3f(0.7f,0.7f,0.4f);  //���� ������
                            gl.glBegin(GL.GL_POLYGON);
                                for (int j=1; j<=faces.length-1; j++) //�� ����, � �� �� ������� ������ ��� ������ f, ����� -1 ������ ��� ��������� �� ����
                                {
                                    cx=xa.get(Integer.parseInt(faces[j])-1); //-1 ������ ��� ��������� �� ����
                                    cy=ya.get(Integer.parseInt(faces[j])-1);
                                    cz=za.get(Integer.parseInt(faces[j])-1);
                                    gl.glVertex3d(cx,cz,cy);
                                }
                            gl.glEnd();
                            gl.glColor3f(0f,0f,0f); //���� �����
                            gl.glBegin(GL.GL_LINE_LOOP);
                                for (int j=1; j<=faces.length-1; j++) //�� ����, � �� �� ������� ������ ��� ������ f, ����� -1 ������ ��� ��������� �� ����
                                {
                                    cx=xa.get(Integer.parseInt(faces[j])-1); //-1 ������ ��� ��������� �� ����
                                    cy=ya.get(Integer.parseInt(faces[j])-1);
                                    cz=za.get(Integer.parseInt(faces[j])-1);
                                    gl.glVertex3d(cx,cz,cy);
                                }
                            gl.glEnd();
                        }
                        
                    } 
                   
                    //������������ �����
                    gl.glPointSize(3);
                    gl.glColor3f(0,0,0);
                    gl.glBegin(GL.GL_POINTS);
                        for (int i=0; i<=nv-1; i++)
                        {
                           gl.glVertex3d(xa.get(i), za.get(i), ya.get(i)); //get ����� ��������� �������� �� �������, ������� z � ����� y, ������ ��� y ���������, � z ������� �� ���������
                        }
                    gl.glEnd(); 
                    
                }    
                catch (FileNotFoundException ex) { //���� ����� �� �����, �� �������� ��������� �� ����������
                    Logger.getLogger(GLRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                 
             gl.glEndList(); 
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    } 

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL gl = drawable.getGL();
        GLU glu = new GLU();
    }

    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        GLU glu = new GLU();
        gl.glLoadIdentity();
        
        GLUquadric q;
        
        if (ez>1) //����������
        {
            ez=ez-g;
            pz=pz-g;
            g=g+0.001;
        }
        else
        {
            g=0.001;
        } 
        
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glClear(/*GL.GL_COLOR_BUFFER_BIT | */GL.GL_DEPTH_BUFFER_BIT);
        
        glu.gluPerspective(90, 1, 0.01, 300);
        glu.gluLookAt(ex, ey, ez,  px, py, pz,  0, 0, 1);
        
        gl.glCallList(121);
        gl.glCallList(120);
        gl.glCallList(122);
        
        lastex = ex; //����������� ��������� ����� �� ��������
        lastey = ey;
        lastez = ez;
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }
    
    public void RecountPxPyPz() //�������� ��������� ����� �� ������� ������� �����������
    {
        px=ex+rad*Math.cos(rot);
        py=ey+rad*Math.sin(rot);
        pz=ez+rotz;
    }
    
    public void collision()
    {   
        if (ez<=1) //������� ����
        {
            ez=1;
        }
        
        if ((ex>-1 && ex<1)&&(ey>-1 && ey<1)) //������� �����
        {
            ex=lastex;
            ey=lastey;
            System.out.println("**");
        }
        
        if ((ex>-10.2 && ex<-6.8)&&(ey>-20.2 && ey<-16.8)) //������� �������
        {
            ex=lastex;
            ey=lastey;
            System.out.println("**");
        }
    }
    
    public void Key(int keycode) //����� ��������� �������
    {
        switch (keycode) //�������� �������������� ������
        {
            case 65: ez=ez+speed; RecountPxPyPz(); collision(); break; //�����
            case 90: ez=ez-speed; RecountPxPyPz(); collision(); break; //����
                
            case 38: ex=ex+(speed*Math.cos(rot));
                     ey=ey+(speed*Math.sin(rot)); 
                     RecountPxPyPz(); collision(); break; //������
            case 40: ex=ex-(speed*Math.cos(rot));
                     ey=ey-(speed*Math.sin(rot));
                     RecountPxPyPz(); collision(); break; //�����
            case 37: rot=rot+speed; RecountPxPyPz(); collision(); break; //�����
            case 39: rot=rot-speed; RecountPxPyPz(); collision(); break; //������
            case 33: pz=pz+speed; break; //������ �����
            case 34: pz=pz-speed; break; //������ ����
        }
        //System.out.println(keycode);
    }
}

