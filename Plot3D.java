package Plot3D;

import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import java.awt.image.*;
import java.util.*;
import java.io.*;

import java.math.*;
import java.lang.*;

public class Plot3D extends Applet
  implements Runnable, MouseListener, MouseMotionListener, ActionListener{

  int nplanet=0, maxplanet=10;
  int zoom=60;
  Planet planet[];

  double rotX = 0, rotY = 0;

  private Thread thread;

  int sw=600, sh=700;
  int w=500, h=500;
  int winx, winy;
  int prevx, prevy;

  boolean pause = false;

  boolean altDown = false;

  Matrix3D mat;

  Image buffer;
  Graphics buffer_g;
  Canvas canvas1 = new Canvas();
  Label lblTitle = new Label();
  Button btnPause = new Button();
  Button btnStart = new Button();
  TextArea txtCoord = new TextArea();
  Label lblCoord = new Label();

  vector vOrigin = new vector();
  vector vX = new vector();;
  vector vY = new vector();;
  vector vZ = new vector();;

  Color colors[];
  Scrollbar scrollSpeed = new Scrollbar();
  Label lblSpeed = new Label();
  Label lblHelp1 = new Label();
  Label lblHelp2 = new Label();
  Button btnView = new Button();



  /**Construct the applet*/
  public Plot3D() {
    colors = new Color[10];
    colors[0] = new Color(255,0,0);
    colors[1] = new Color(0,255,0);
    colors[2] = new Color(0,0,255);
    colors[3] = new Color(255,255,0);
    colors[4] = new Color(0,255,255);
    colors[5] = new Color(255,0,255);
    colors[6] = new Color(255,128,128);
    colors[7] = new Color(128,255,128);
    colors[8] = new Color(128,128,255);
    colors[9] = new Color(128,128,128);
  }

  public void main(){
    init();
  }

  /**Initialize the applet*/
  public void init() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(null);
    this.setSize(new Dimension(627, 722));

    resize(w,h);

    winx=w/2;
    winy=h/2;

    mat=new Matrix3D();
    vOrigin.set(0,0,0);
    vX.set(10,0,0);
    vY.set(0,-10,0);
    vZ.set(0,0,10);


    canvas1.addMouseListener(this);
    canvas1.addMouseMotionListener(this);
    canvas1.setBackground(SystemColor.activeCaptionText);
    canvas1.setForeground(Color.white);
    canvas1.setBounds(new Rectangle(90, 40, w, h));

    lblTitle.setFont(new java.awt.Font("Dialog", 1, 16));
    lblTitle.setText("3D Planetary Motion Plotter");
    lblTitle.setBounds(new Rectangle(130, 10, 250, 30));

    btnStart.addActionListener(this);
    btnStart.setBounds(new Rectangle(6, 50, 80, 25));
    btnStart.setLabel("Start");
    btnStart.setActionCommand("start");
    btnStart.setBackground(Color.white);

    btnPause.setActionCommand("pause");
    btnPause.setBackground(Color.white);
    btnPause.setLabel("Pause");
    btnPause.setBounds(new Rectangle(6, 80, 80, 25));
    btnPause.addActionListener(this);

    lblCoord.setText("Planet Positions:");
    lblCoord.setBounds(new Rectangle(90, 80+h, 120, 14));

    txtCoord.setColumns(4);
    txtCoord.setRows(20);
    txtCoord.setBounds(new Rectangle(90, 100+h, w, 100));
    txtCoord.setText("1.5582,   0, 0, 0, -0.5958,  0, 1,"+"\n"+
                     "0.8964, 0, 0, 0, 2.5091, 0, 5,"+"\n"+
                     "-0.6040, 0, 0, 0, -1.1950, 0, 10,");

    this.setBackground(Color.white);

    scrollSpeed.setBackground(Color.white);
    scrollSpeed.setForeground(Color.white);
    scrollSpeed.setLocale(java.util.Locale.getDefault());
    scrollSpeed.setMaximum(990);
    scrollSpeed.setMinimum(500);
    scrollSpeed.setOrientation(0);
    scrollSpeed.setUnitIncrement(1);
    scrollSpeed.setValue(800);
    scrollSpeed.setBounds(new Rectangle(6, 179, 80, 15));

    lblSpeed.setText("Speed");
    lblSpeed.setBounds(new Rectangle(7, 162, 62, 15));
    lblHelp1.setText("Drag = Zoom");
    lblHelp1.setBounds(new Rectangle(4, 220, 80, 21));
    lblHelp2.setBounds(new Rectangle(3, 242, 91, 21));
    lblHelp2.setText("Alt+Drag = Rotate");

    btnView.setBackground(Color.white);
    btnView.setForeground(Color.black);
    btnView.setLabel("Center");
    btnView.setBounds(new Rectangle(5, 109, 80, 25));
    btnView.setActionCommand("center");
    btnView.addActionListener(this);

    this.add(canvas1, null);
    this.add(lblTitle, null);
    this.add(btnPause, null);
    this.add(btnStart, null);
    this.add(txtCoord, null);
    this.add(lblHelp1, null);
    this.add(lblHelp2, null);

    //ReadPlanets();

    reStart();
  }

  void ReadPlanets() throws IOException
  {
    String line;
    FileInputStream in = new FileInputStream("D:\\planets.txt");
    BufferedInputStream s = new BufferedInputStream(in);
    BufferedReader myInput = new BufferedReader(new InputStreamReader(s));

    double x,y,z,vx,vy,vz,m;

    while ((line = myInput.readLine()) != null) {
      StringTokenizer st =new StringTokenizer(line, " ,\t\n\r\f");

      x = Double.valueOf(st.nextToken()).doubleValue();
      y = Double.valueOf(st.nextToken()).doubleValue();
      z = Double.valueOf(st.nextToken()).doubleValue();
      vx = Double.valueOf(st.nextToken()).doubleValue();
      vy = Double.valueOf(st.nextToken()).doubleValue();
      vz = Double.valueOf(st.nextToken()).doubleValue();
      m = Double.valueOf(st.nextToken()).doubleValue();
    }
  }


  void reStart(){
    clearPlanet();

    pause = false;
    btnPause.setLabel("Pause");

    double x,y,z,vx,vy,vz,m;

    try{
      String text=txtCoord.getText();
      StringTokenizer st = new StringTokenizer(text, " ,\t\n\r\f");

      while(st.hasMoreElements())
      {
        x = Double.valueOf(st.nextToken()).doubleValue();
        y = Double.valueOf(st.nextToken()).doubleValue();
        z = Double.valueOf(st.nextToken()).doubleValue();
        vx = Double.valueOf(st.nextToken()).doubleValue();
        vy = Double.valueOf(st.nextToken()).doubleValue();
        vz = Double.valueOf(st.nextToken()).doubleValue();
        m = Double.valueOf(st.nextToken()).doubleValue();

        addPlanet(x,y,z,vx,vy,vz,m);
      }
    }catch(Exception e) {}

    this.add(canvas1, null);
    this.add(btnStart, null);
    this.add(btnPause, null);
    this.add(txtCoord, null);
    this.add(txtCoord, null);
    this.add(lblCoord, null);
    this.add(btnView, null);
    this.add(scrollSpeed, null);
    this.add(lblSpeed, null);
    this.add(lblHelp1, null);
    this.add(lblHelp2, null);
    this.add(lblHelp1, null);
    this.add(btnPause, null);
    this.add(btnStart, null);
    this.add(btnPause, null);
    this.add(btnStart, null);
    this.add(btnPause, null);
  }


  void clearPlanet() {
    nplanet=0;
    maxplanet=0;
    planet = new Planet[maxplanet];
  }

  void addPlanet(double x, double y, double z, double vx, double vy, double vz, double m) {
    if (nplanet >= maxplanet){
      maxplanet+=10;
      Planet np[] = new Planet[maxplanet];
      System.arraycopy(planet, 0, np, 0, planet.length);
      planet=np;
    }
    planet[nplanet]=new Planet(x,y,z,vx,vy,vz,m);
    planet[nplanet].id = nplanet;
    nplanet++;
  }

  public void paint(Graphics g){
    if (!pause){
      RunPlanets();
    }

    Graphics g2 = canvas1.getGraphics();

    if (buffer == null){
      buffer = createImage(w, h);
      buffer_g = buffer.getGraphics();
      System.out.print("Creating Buffer...");

      if (buffer==null){
          System.out.print("Failed");
          return;
      }
      else
        System.out.print("Succeeded");
    }
    buffer_g.setColor(new Color(255,255,255));
    buffer_g.fillRect(0, 0, w, h);

    SortPlanets();


    mat.unit();
    mat.xrot(rotX);
    mat.yrot(rotY);


    if (altDown){
      mat.transform(vX, 20);
      mat.transform(vY, 20);
      mat.transform(vZ, 20);

      buffer_g.setColor(colors[0]);
      buffer_g.drawLine(winx, winy, vX.tx + winx, vX.ty + winy);
      buffer_g.setColor(colors[1]);
      buffer_g.drawLine(winx, winy, vY.tx + winx, vY.ty + winy);
      buffer_g.setColor(colors[2]);
      buffer_g.drawLine(winx, winy, vZ.tx + winx, vZ.ty + winy);
    }


    for (int i=0; i<nplanet; i++){
      buffer_g.setColor(colors[planet[i].id%10]);
      for(int j=0; j<Planet.TAIL-1; j++){
        mat.transform(planet[i].tail[j+1], zoom);
        buffer_g.drawLine(planet[i].tail[j].tx+winx, planet[i].tail[j].ty+winy, planet[i].tail[j+1].tx+winx, planet[i].tail[j+1].ty+winy);
      }
    }

    for (int i=0; i<nplanet; i++){
      mat.transform(planet[i].r, zoom);
      buffer_g.setColor(colors[planet[i].id%10]);
      int radius = (int)Math.round(Math.sqrt(planet[i].mass*zoom));
      buffer_g.fillOval(planet[i].r.tx+winx-radius/2, planet[i].r.ty+winy-radius/2, radius, radius);
      mat.transform(planet[i].tail[0], zoom);
      buffer_g.drawLine(planet[i].r.tx+winx, planet[i].r.ty+winy, planet[i].tail[0].tx+winx, planet[i].tail[0].ty+winy);
    }

    g2.drawImage(buffer, 0, 0, canvas1);
  }
  public void mouseClicked(MouseEvent e) {
  }
  public void mousePressed(MouseEvent e) {
    prevx = e.getX();
    prevy = e.getY();

    e.consume();
  }
  public void mouseReleased(MouseEvent e) {
    altDown = false;
  }
  public void mouseEntered(MouseEvent e) {
  }
  public void mouseExited(MouseEvent e) {
  }
  public void mouseDragged(MouseEvent e) {
    int x = e.getX();
    int y = e.getY();

    if (e.isAltDown()){
      altDown = true;
      float xtheta = (prevy - y) * (360.0f / w);
      float ytheta = (x - prevx) * (360.0f / h);

      rotX += xtheta;
      rotY += ytheta;

      if (rotX > 90)
        rotX = 90;
      if (rotX < -90)
        rotX = -90;
    }
    else{
      altDown = false;
      int dy=y-prevy;
      zoom-=dy;

      if (zoom<1)
        zoom=1;
      else if (zoom>1000)
        zoom=1000;
    }

    prevx = x;
    prevy = y;
    e.consume();
    repaint();
  }
  public void mouseMoved(MouseEvent e) {
  }

  public void start() {
    if (thread==null)
    {
      thread = new Thread(this);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }
  public synchronized void stop() {
    thread = null;
  }
  public void run() {
    Thread me = Thread.currentThread();
    while (thread == me) {
      repaint();
    }
    thread = null;
  }

  void CalculateAccelerations()
  {
    double distance;
    vector radius=new vector();

    for(int i=0; i<nplanet; i++){
      for (int j=i+1; j<nplanet; j++){

        if (planet[i]!=null && planet[j]!=null)
        {
           //distance vector
          radius.copy(planet[i].r,1);
          radius.subtract(planet[j].r,1);
          distance=radius.mag();

          //acceleration
          radius.scalar(1/(distance*distance*distance));

          planet[i].a.subtract(radius,planet[j].mass);
          planet[j].a.add(radius,planet[i].mass);
        }
      }
    }
  }
  void UpdatePlanets(){
    CalculateAccelerations();

    planet[0].iter++;
    for(int i=0; i<nplanet; i++) {
      planet[i].Update();
    }
  }
  void RunPlanets()
  {
    for (int i=0; i<planet[0].STEP/(1000-scrollSpeed.getValue()); i++){
      UpdatePlanets();
    }
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand()=="pause"){
      pause=!pause;
      if (pause) btnPause.setLabel("Resume");
      else btnPause.setLabel("Pause");
    }
    else if (e.getActionCommand()=="start"){
      reStart();
    }
    else if (e.getActionCommand()=="center"){
      rotX = 0;
      rotY = 0;
      zoom = 60;
    }
  }

  void SortPlanets(){
    int min = -1;

    for (int i=0; i<nplanet; i++){
      min = i;
      for (int j=i; j<nplanet; j++){
        if (planet[j].r.tz < planet[min].r.tz){
          min = j;
        }
        if (min != i){
          Planet temp = planet[i];
          planet[i] = planet[min];
          planet[min] = temp;
        }
      }
    }
  }
}



class Planet
{
  public static int TAIL=1000;
  public static int STEP=1000;
  public static int iter=0;


  public vector r=new vector();	//position vector
  public vector a=new vector();	//acceleration vector
  public vector v=new vector();	//velocity vector
  public vector tail[];

  public double mass;
  public int id;

  Planet(double rx,double ry,double rz,double vx,double vy,double vz, double imass){
    v.set(vx,vy,vz);
    r.set(rx,ry,rz);
    mass=imass;

    tail=new vector[TAIL];
    for (int i=0; i<TAIL; i++)
    {	tail[i]=new vector();
        tail[i].copy(r,1);
    }
  }

  Planet(){
  }

  void Update(){
    a.scalar(1.0/STEP);
    v.add(a,1);
    r.add(v,1.0/STEP);
    a.set(0,0,0);

    if (iter%(STEP/40+20)==0)
    {	UpdateTail();
    }
  }

  void UpdateTail(){
    for (int i=TAIL-2; i>=0; i--){
      tail[i+1].copy(tail[i],1);
    }
    tail[0].copy(r,1);
  }
}

class vector
{
  public double x,y,z;
        public int tx,ty,tz;

  vector(){
    set(0,0,0);
  }

  void set(double ix, double iy, double iz){
    x=ix;
    y=iy;
    z=iz;
  }

  void add(vector v, double s){
    x+=v.x*s;
    y+=v.y*s;
    z+=v.z*s;
  }
  void subtract(vector v, double s){
    x-=v.x*s;
    y-=v.y*s;
    z-=v.z*s;
  }
  void copy(vector v, double s){
    x=v.x*s;
    y=v.y*s;
    z=v.z*s;
  }
  void scalar(double s){
    x*=s;
    y*=s;
    z*=s;
  }

  double mag(){
    return Math.sqrt(x*x+y*y+z*z);
  }

  void changetounit(){
    double adj=mag();
    x/=adj;
    y/=adj;
    z/=adj;
  }
}


class Matrix3D {
    float xx, xy, xz, xo;
    float yx, yy, yz, yo;
    float zx, zy, zz, zo;
    static final double pi = 3.14159265;
    /** Create a new unit matrix */
    Matrix3D () {
        xx = 1.0f;
        yy = 1.0f;
        zz = 1.0f;
    }
    /** Scale by f in all dimensions */
    void scale(float f) {
        xx *= f;
        xy *= f;
        xz *= f;
        xo *= f;
        yx *= f;
        yy *= f;
        yz *= f;
        yo *= f;
        zx *= f;
        zy *= f;
        zz *= f;
        zo *= f;
    }
    /** Scale along each axis independently */
    void scale(float xf, float yf, float zf) {
        xx *= xf;
        xy *= xf;
        xz *= xf;
        xo *= xf;
        yx *= yf;
        yy *= yf;
        yz *= yf;
        yo *= yf;
        zx *= zf;
        zy *= zf;
        zz *= zf;
        zo *= zf;
    }
    /** Translate the origin */
    void translate(float x, float y, float z) {
        xo += x;
        yo += y;
        zo += z;
    }
    /** rotate theta degrees about the y axis */
    void yrot(double theta) {
        theta *= (pi / 180);
        double ct = Math.cos(theta);
        double st = Math.sin(theta);

        float Nxx = (float) (xx * ct + zx * st);
        float Nxy = (float) (xy * ct + zy * st);
        float Nxz = (float) (xz * ct + zz * st);
        float Nxo = (float) (xo * ct + zo * st);

        float Nzx = (float) (zx * ct - xx * st);
        float Nzy = (float) (zy * ct - xy * st);
        float Nzz = (float) (zz * ct - xz * st);
        float Nzo = (float) (zo * ct - xo * st);

        xo = Nxo;
        xx = Nxx;
        xy = Nxy;
        xz = Nxz;
        zo = Nzo;
        zx = Nzx;
        zy = Nzy;
        zz = Nzz;
    }
    /** rotate theta degrees about the x axis */
    void xrot(double theta) {
        theta *= (pi / 180);
        double ct = Math.cos(theta);
        double st = Math.sin(theta);

        float Nyx = (float) (yx * ct + zx * st);
        float Nyy = (float) (yy * ct + zy * st);
        float Nyz = (float) (yz * ct + zz * st);
        float Nyo = (float) (yo * ct + zo * st);

        float Nzx = (float) (zx * ct - yx * st);
        float Nzy = (float) (zy * ct - yy * st);
        float Nzz = (float) (zz * ct - yz * st);
        float Nzo = (float) (zo * ct - yo * st);

        yo = Nyo;
        yx = Nyx;
        yy = Nyy;
        yz = Nyz;
        zo = Nzo;
        zx = Nzx;
        zy = Nzy;
        zz = Nzz;
    }
    /** rotate theta degrees about the z axis */
    void zrot(double theta) {
        theta *= (pi / 180);
        double ct = Math.cos(theta);
        double st = Math.sin(theta);

        float Nyx = (float) (yx * ct + xx * st);
        float Nyy = (float) (yy * ct + xy * st);
        float Nyz = (float) (yz * ct + xz * st);
        float Nyo = (float) (yo * ct + xo * st);

        float Nxx = (float) (xx * ct - yx * st);
        float Nxy = (float) (xy * ct - yy * st);
        float Nxz = (float) (xz * ct - yz * st);
        float Nxo = (float) (xo * ct - yo * st);

        yo = Nyo;
        yx = Nyx;
        yy = Nyy;
        yz = Nyz;
        xo = Nxo;
        xx = Nxx;
        xy = Nxy;
        xz = Nxz;
    }
    /** Multiply this matrix by a second: M = M*R */
    void mult(Matrix3D rhs) {
        float lxx = xx * rhs.xx + yx * rhs.xy + zx * rhs.xz;
        float lxy = xy * rhs.xx + yy * rhs.xy + zy * rhs.xz;
        float lxz = xz * rhs.xx + yz * rhs.xy + zz * rhs.xz;
        float lxo = xo * rhs.xx + yo * rhs.xy + zo * rhs.xz + rhs.xo;

        float lyx = xx * rhs.yx + yx * rhs.yy + zx * rhs.yz;
        float lyy = xy * rhs.yx + yy * rhs.yy + zy * rhs.yz;
        float lyz = xz * rhs.yx + yz * rhs.yy + zz * rhs.yz;
        float lyo = xo * rhs.yx + yo * rhs.yy + zo * rhs.yz + rhs.yo;

        float lzx = xx * rhs.zx + yx * rhs.zy + zx * rhs.zz;
        float lzy = xy * rhs.zx + yy * rhs.zy + zy * rhs.zz;
        float lzz = xz * rhs.zx + yz * rhs.zy + zz * rhs.zz;
        float lzo = xo * rhs.zx + yo * rhs.zy + zo * rhs.zz + rhs.zo;

        xx = lxx;
        xy = lxy;
        xz = lxz;
        xo = lxo;

        yx = lyx;
        yy = lyy;
        yz = lyz;
        yo = lyo;

        zx = lzx;
        zy = lzy;
        zz = lzz;
        zo = lzo;
    }

    /** Reinitialize to the unit matrix */
    void unit() {
        xo = 0;
        xx = 1;
        xy = 0;
        xz = 0;
        yo = 0;
        yx = 0;
        yy = 1;
        yz = 0;
        zo = 0;
        zx = 0;
        zy = 0;
        zz = 1;
    }
    /** Transform nvert points from v into tv.  v contains the input
        coordinates in floating point.  Three successive entries in
        the array constitute a point.  tv ends up holding the transformed
        points as integers; three successive entries per point */
    void transform(float v[], int tv[], int nvert) {
        float lxx = xx, lxy = xy, lxz = xz, lxo = xo;
        float lyx = yx, lyy = yy, lyz = yz, lyo = yo;
        float lzx = zx, lzy = zy, lzz = zz, lzo = zo;
        for (int i = nvert * 3; (i -= 3) >= 0;) {
            float x = v[i];
            float y = v[i + 1];
            float z = v[i + 2];
            tv[i    ] = (int) (x * lxx + y * lxy + z * lxz + lxo);
            tv[i + 1] = (int) (x * lyx + y * lyy + z * lyz + lyo);
            tv[i + 2] = (int) (x * lzx + y * lzy + z * lzz + lzo);
        }
    }
    void transform(vector v, double scale) {
        double lxx = xx, lxy = xy, lxz = xz, lxo = xo;
        double lyx = yx, lyy = yy, lyz = yz, lyo = yo;
        double lzx = zx, lzy = zy, lzz = zz, lzo = zo;

        v.tx = (int) ((v.x * lxx + v.y * lxy + v.z * lxz + lxo)*scale);
        v.ty = (int) ((v.x * lyx + v.y * lyy + v.z * lyz + lyo)*scale);
        v.tz = (int) (v.x * lzx + v.y * lzy + v.z * lzz + lzo);
    }
    public String toString() {
        return ("[" + xo + "," + xx + "," + xy + "," + xz + ";"
                + yo + "," + yx + "," + yy + "," + yz + ";"
                + zo + "," + zx + "," + zy + "," + zz + "]");
    }
}
