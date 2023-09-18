package org.yourorghere;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.*;
import java.math.*;
import java.util.*;//утилиты
import java.io.*; //ввод вывод
import java.nio.ByteBuffer; //работа с заворачиванием в буффер
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
        File f = new File(filename); //файл из которого подгружается текстура
        try {
            BufferedImage img; //объект типа буфферизованное изображение
            img = ImageIO.read(f); //чтение изображения из файла

            Raster r = img.getData(); //получение растра из файла
            byte tex[];
            int w=r.getWidth(), h=r.getHeight(); //получение высоты и ширины из растра
            tex = new byte[w*h*3]; //размер массива задается автоматически при чтении растра

            double pixel[] = new double[3]; //массив содержит 3 элемента R,G,B, чтение содержимого растра
            int count = 0; //счетчик для массива
            for(int i=0; i<=w-1; i++) //ширина
            {
                for(int j=0; j<=h-1; j++) //высота
                {
                    r.getPixel(i, j, pixel); //записывает значения в массив
                    for(int k=0; k<=2; k++) //от 0 до 2, потому что р г б
                    {
                        tex[count] = (byte)pixel[k]; //чтение растровой структуры и запись в одномерный массив
                        count++; 
                    }
                }
            }
            ByteBuffer btex = ByteBuffer.wrap(tex); //заворачиваем массив tex
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, w, h, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, btex);//текстурный вычислитель //цель, левел(мипмеппинг), формат, ширина, высота, граница текстуры, формат
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR); //текстурная фильтрация //mag как ведет текстура когда много мест, min когда мало
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR); //к чему применяется, наименование параметра текстуры, значение
            } 
            catch (IOException ex) {
                Logger.getLogger(GLRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
    
    void SetTreeTexture(GL gl, int num)
    {
        byte tex[];
        int w=256; //разрешение текстуры дерева
        int h=256;
        
        try {
            File ftt = new File("tree"+num+".jpg"); //файл из которого подгружается текстура
            BufferedImage imgt; //объект типа буфферизованное изображение
            imgt = ImageIO.read(ftt); //чтение изображения из файла
            Raster r = imgt.getData(); //получение растра из файла
            w=r.getWidth(); //получение ширины из растра
            h=r.getHeight(); //получение высоты из растра
            tex = new byte[w*h*4]; //
            
            int pixel[] = new int[3]; //массив содержит 3 элемента R,G,B, чтение содержимого растра
            int count = 0; //счетчик для массива
            for(int i=0; i<w-1; i++) //ширина
            {
                for (int j=0; j<=h-1; j++) //высота
                {
                    r.getPixel(i, j, pixel); //записывает значения в массив
                    tex[count]=(byte)pixel[0]; //R //ручное высчитывание компонентов
                    count++;
                    tex[count]=(byte)pixel[1]; //G
                    count++;
                    tex[count]=(byte)pixel[2]; //B
                    count++;
                    if ((pixel[0]>120) && (pixel[1]>120) && (pixel[2]>120))
                    {
                        tex[count]=0; //если пиксел светлее, то устанавливется альфа канал 0 - пиксель непрозрачен
                    }
                    else
                    {
                        tex[count]=(byte)255; //иначе альфа 255 - пиксель прозрачен
                    }
                    count++;
                }
            }
            ByteBuffer bbtex = ByteBuffer.wrap(tex); //заворачиваем массив tex
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, w, h, 0, GL.GL_RGBA,GL.GL_UNSIGNED_BYTE, bbtex);//текстурный вычислитель //цель, левел(мипмеппинг), формат, ширина, высота, граница текстуры, формат
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR); //текстурная фильтрация //mag как ведет текстура когда много мест, min когда мало
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR); //к чему применяется, наименование параметра текстуры, значение
            } catch (IOException ex) {
            Logger.getLogger(GLRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    void Tree(GL gl, float x, float y)
    {
        gl.glPushMatrix();
        gl.glTranslatef(x,y, 0);
        
        gl.glAlphaFunc(GL.GL_GREATER, 0.5f); //функция смешивания, те пиксели, у которых альфа больше чем 0.5, будут проходить обработку альфа теста, остальные отброшены
        gl.glEnable(GL.GL_ALPHA_TEST); //включение режима прозрачности                                  //0.5f = 128 byte
        gl.glBegin(GL.GL_QUADS); //построение дерева с помощью вертексов
            gl.glTexCoord2d(0, 0); gl.glVertex3f(0,-2.5f, 5); //первая плоскость дерева
            gl.glTexCoord2d(0, 1); gl.glVertex3f(0, 2.5f, 5);
            gl.glTexCoord2d(1, 1); gl.glVertex3f(0, 2.5f, 0);
            gl.glTexCoord2d(1, 0); gl.glVertex3f(0,-2.5f, 0);
            
            gl.glTexCoord2d(0, 0); gl.glVertex3f(-2.5f, 0, 5); //вторая плоскость дерева
            gl.glTexCoord2d(0, 1); gl.glVertex3f( 2.5f, 0, 5);
            gl.glTexCoord2d(1, 1); gl.glVertex3f( 2.5f, 0, 0);
            gl.glTexCoord2d(1, 0); gl.glVertex3f(-2.5f, 0, 0);
        gl.glEnd();
        gl.glPopMatrix(); //возвращение системы координат после транслейта
        gl.glDisable(GL.GL_ALPHA_TEST); //выключение режима прозрачности
    }

    public void init(GLAutoDrawable drawable) {

        GL gl = drawable.getGL();
        GLU glu = new GLU();
        System.err.println("INIT GL IS: " + gl.getClass().getName());
        
        gl.glNewList(121, GL.GL_COMPILE);
            //Построения
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
                for (int j=1; j<=5; j++) //переборка всех возможных текстур деревьев
                {
                    SetTreeTexture(gl, j);
                    for (int i=1; i<=500; i++) //количество деревьев
                    {
                        Tree(gl, (float)Math.random()*300-140, (float)Math.random()*300-140); //случайные координаты
                    }
                } 
             gl.glEndList();
             
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////             
             gl.glNewList(120, GL.GL_COMPILE);
             
                try {                                //обработка исключения
                    File F = new File("c:/budka.obj"); //тип данных сканер используется для организации потоков
                    Scanner SC = new Scanner(F); //если файл не найден, возникает исключение, сканер это тип данных, который работает с файлом
                    int nv=0; //количество вершин
                    String s;
                    String coord[]; //строковый массив для хранения элементов на которые будет разделена строка
                    String faces[]; //массив для работы с гранями
                    
                    double cx,cy,cz;
                    int face[];
                    face = new int [5]; //5 потому что 4 вершины и f в начале строки
                                                //используем динамический потому что не знаем его размер изначально
                    ArrayList<Double> xa,ya,za; //в сущности динамический массив, тип динамического массива(веществ. двойной точности)
                    
                    xa = new ArrayList(); //3 динамических массива
                    ya = new ArrayList();
                    za = new ArrayList();
                    
                    //чтение данных из файла
                    while(SC.hasNext()) //до тех пор пока есть возможность читать из файла, hasnext метод сканера
                    {
                        //обработка вершин
                        s=SC.nextLine(); //следующая строка ИЗБЕГАЕТ ЗАЦИКЛИВАНИЕ
                        if((s.charAt(0) == 'v') && (s.charAt(1) == ' ')) //чарэт возвращает символ в заданной позиции. в скобах номер символа. 
                        {                                                //условие И чтобы не попасть на нормаль к вершинам vn
                            nv++; //переход к следующей вершине
                            coord = s.split(" "); //метод для разделения строки результатом которого является строковый массив
                            xa.add(Double.parseDouble(coord[1])/15); //парсдубль преобразование строки в вещественное число
                            ya.add(Double.parseDouble(coord[2])/15); //метод эдд добавить //деление уменьшпет конечный размер объекта
                            za.add(Double.parseDouble(coord[3])/15); //записываем в каждый из массивов соотв координаты
                        }
                        //обработка граней
                        if(s.charAt(0) == 'f') //чарэт возвращает символ в заданной позиции. в скобах номер символа. 
                        {
                            faces=s.split(" "); //метод для разделения строки результатом которого является строковый массив
                            gl.glColor3f(0.7f,0.7f,0.4f);  //цвет граней
                            gl.glBegin(GL.GL_POLYGON);
                                for (int j=1; j<=faces.length-1; j++) //от нуля, а не от единицы потому что первая f, длина -1 потому что нумерация от нуля
                                {
                                    cx=xa.get(Integer.parseInt(faces[j])-1); //-1 потому что нумерация от нуля
                                    cy=ya.get(Integer.parseInt(faces[j])-1);
                                    cz=za.get(Integer.parseInt(faces[j])-1);
                                    gl.glVertex3d(cx,cz,cy);
                                }
                            gl.glEnd();
                            gl.glColor3f(0f,0f,0f); //цвет линий
                            gl.glBegin(GL.GL_LINE_LOOP);
                                for (int j=1; j<=faces.length-1; j++) //от нуля, а не от единицы потому что первая f, длина -1 потому что нумерация от нуля
                                {
                                    cx=xa.get(Integer.parseInt(faces[j])-1); //-1 потому что нумерация от нуля
                                    cy=ya.get(Integer.parseInt(faces[j])-1);
                                    cz=za.get(Integer.parseInt(faces[j])-1);
                                    gl.glVertex3d(cx,cz,cy);
                                }
                            gl.glEnd();
                        }
                        
                    } 
                   
                    //визуализация точек
                    gl.glPointSize(3);
                    gl.glColor3f(0,0,0);
                    gl.glBegin(GL.GL_POINTS);
                        for (int i=0; i<=nv-1; i++)
                        {
                           gl.glVertex3d(xa.get(i), za.get(i), ya.get(i)); //get метод получения значений их массива, сначала z а потом y, потому что y вертикаль, а z глубина по умолчанию
                        }
                    gl.glEnd(); 
                    
                }    
                catch (FileNotFoundException ex) { //если файла не будет, то фрагмент программы не выполнится
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
        
        if (ez>1) //гравитация
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
        
        lastex = ex; //запоминание последней точки до коллизии
        lastey = ey;
        lastez = ez;
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }
    
    public void RecountPxPyPz() //пересчет координат точки на которую смотрит наблюдатель
    {
        px=ex+rad*Math.cos(rot);
        py=ey+rad*Math.sin(rot);
        pz=ez+rotz;
    }
    
    public void collision()
    {   
        if (ez<=1) //колизия пола
        {
            ez=1;
        }
        
        if ((ex>-1 && ex<1)&&(ey>-1 && ey<1)) //колизия будки
        {
            ex=lastex;
            ey=lastey;
            System.out.println("**");
        }
        
        if ((ex>-10.2 && ex<-6.8)&&(ey>-20.2 && ey<-16.8)) //колизия бытовки
        {
            ex=lastex;
            ey=lastey;
            System.out.println("**");
        }
    }
    
    public void Key(int keycode) //метод обработки нажатий
    {
        switch (keycode) //оператор множественного выбора
        {
            case 65: ez=ez+speed; RecountPxPyPz(); collision(); break; //вверх
            case 90: ez=ez-speed; RecountPxPyPz(); collision(); break; //вниз
                
            case 38: ex=ex+(speed*Math.cos(rot));
                     ey=ey+(speed*Math.sin(rot)); 
                     RecountPxPyPz(); collision(); break; //вперед
            case 40: ex=ex-(speed*Math.cos(rot));
                     ey=ey-(speed*Math.sin(rot));
                     RecountPxPyPz(); collision(); break; //назад
            case 37: rot=rot+speed; RecountPxPyPz(); collision(); break; //влево
            case 39: rot=rot-speed; RecountPxPyPz(); collision(); break; //вправо
            case 33: pz=pz+speed; break; //взгляд вверх
            case 34: pz=pz-speed; break; //взгляд вниз
        }
        //System.out.println(keycode);
    }
}

