import java.awt.AWTException;
import java.awt.AlphaComposite;

import java.awt.Color;

import java.awt.Component;
import java.awt.Dialog;

import java.awt.Dimension;
import java.awt.Font;

import java.awt.Graphics;

import java.awt.Graphics2D;

import java.awt.MouseInfo;
import java.awt.Polygon;

import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.awt.Robot;
import java.awt.Shape;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;

import java.awt.event.MouseEvent;

import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.awt.image.BufferedImage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import javax.swing.JButton;

import javax.swing.JColorChooser;
import javax.swing.JComboBox;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import javax.swing.JOptionPane;

import javax.swing.JPanel;

import javax.swing.UIManager;

public class Draw implements MouseListener {

    BufferedImage iag = null;
    
    private GeneralPath path;
    private ArrayList<ArrayList<Line>> lnx = new ArrayList<ArrayList<Line>>();
    private ArrayList<Line> lines = new ArrayList<Line>();
    private ArrayList<Polygon> plist = new ArrayList<Polygon>();
    private Polygon p1 = new Polygon();
    
    class Line {
        boolean closed = false;
        int x1;
        int y1;
        int x2;
        int y2;
    }

    class Point 
    { 
        int x; 
        int y; 
    }; 

    // Given three colinear points p, q, r, the function checks if 
    // point q lies on line segment 'pr' 
    boolean onSegment(Point p, Point q, Point r) 
    { 
        if (q.x <= Math.max(p.x, r.x) && q.x >= Math.min(p.x, r.x) && 
            q.y <= Math.max(p.y, r.y) && q.y >= Math.min(p.y, r.y)) 
           return true; 

        return false; 
    } 

    // To find orientation of ordered triplet (p, q, r). 
    // The function returns following values 
    // 0 --> p, q and r are colinear 
    // 1 --> Clockwise 
    // 2 --> Counterclockwise 
    int orientation(Point p, Point q, Point r) 
    { 
        // See https://www.geeksforgeeks.org/orientation-3-ordered-points/ 
        // for details of below formula. 
        int val = (q.y - p.y) * (r.x - q.x) - 
                  (q.x - p.x) * (r.y - q.y); 

        if (val == 0) return 0;  // colinear 

        return (val > 0)? 1: 2; // clock or counterclock wise 
    } 

    // The main function that returns true if line segment 'p1q1' 
    // and 'p2q2' intersect. 
    boolean doIntersect(Point p1, Point q1, Point p2, Point q2) 
    { 
        // Find the four orientations needed for general and 
        // special cases 
        int o1 = orientation(p1, q1, p2); 
        int o2 = orientation(p1, q1, q2); 
        int o3 = orientation(p2, q2, p1); 
        int o4 = orientation(p2, q2, q1); 

        // General case 
        if (o1 != o2 && o3 != o4) 
            return true; 

        // Special Cases 
        // p1, q1 and p2 are colinear and p2 lies on segment p1q1 
        if (o1 == 0 && onSegment(p1, p2, q1)) return true; 

        // p1, q1 and q2 are colinear and q2 lies on segment p1q1 
        if (o2 == 0 && onSegment(p1, q2, q1)) return true; 

        // p2, q2 and p1 are colinear and p1 lies on segment p2q2 
        if (o3 == 0 && onSegment(p2, p1, q2)) return true; 

         // p2, q2 and q1 are colinear and q1 lies on segment p2q2 
        if (o4 == 0 && onSegment(p2, q1, q2)) return true; 

        return false; // Doesn't fall in any of the above cases 
    } 

    private int etnTn = 20;
    private int ptnTn = 1;

    private int xx1;
    private int yy1;
    private int xx2;
    private int yy2;

    private BufferedImage bi = null;
    private Graphics2D gf = null;

    private ArrayList<Shape> sl = new ArrayList<Shape>();

    private int xxxx, yyyy;

    private JFrame j = new JFrame("iDraw v.2");
    private JPanel p = new JPanel();
    private JPanel q = new JPanel();
    private JMenuBar m = new JMenuBar();

    private JMenu file = new JMenu();
    private JMenu edit = new JMenu();
    private JMenu image = new JMenu();
    private JMenu help = new JMenu();

    private String option = "pencil";

    private Color CLR = Color.BLACK;

    public class ColorChooserButton extends JButton {

        private static final long serialVersionUID = 1L;

        private Color current;

        public ColorChooserButton(Color c) {
            setSelectedColor(c);
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    Color newColor = JColorChooser.showDialog(null, "Choose a color", current);
                    setSelectedColor(newColor);
                    CLR = newColor;
                    q.setBackground(newColor);
                }
            });
        }

        public Color getSelectedColor() {
            return current;
        }

        public void setSelectedColor(Color newColor) {
            setSelectedColor(newColor, true);
            q.setBackground(newColor);
        }

        public void setSelectedColor(Color newColor, boolean notify) {

            if (newColor == null) {
                return;
            }

            current = newColor;
            setIcon(createIcon(current, 16, 16));
            repaint();

            if (notify) {
                // Notify everybody that may be interested.
                for (ColorChangedListener l : listeners) {
                    l.colorChanged(newColor);
                }
            }
        }

        private List<ColorChangedListener> listeners = new ArrayList<ColorChangedListener>();

        public void addColorChangedListener(ColorChangedListener toAdd) {
            listeners.add(toAdd);
        }

        public ImageIcon createIcon(Color main, int width, int height) {
            BufferedImage image = new BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(main);
            graphics.fillRect(0, 0, width, height);
            graphics.setXORMode(Color.DARK_GRAY);
            graphics.drawRect(0, 0, width - 1, height - 1);
            image.flush();
            ImageIcon icon = new ImageIcon(image);
            return icon;
        }
    }

    private class Action {

        int id = -1;

        String action = "pencil";

        int etnTn = 20;

        int ptnTn = 1;

        boolean pencilFirst = false;

        int x = 0;
        int y = 0;
        int x0 = 0;
        int y0 = 0;

        boolean tmp = false;

        Color CLR = Color.BLACK;
    }

    private ColorChooserButton btn = new ColorChooserButton(Color.BLACK);
    public ArrayList<Action> undoList = new ArrayList<Action>();

    public ArrayList<Action> actionList = new ArrayList<Action>();

    private int x, y;
    private int ox, oy;

    private Graphics g;

    volatile private boolean mouseDown = false;

    private boolean off;

    boolean get_line_intersection(float p0_x, float p0_y, float p1_x, float p1_y, 
        float p2_x, float p2_y, float p3_x, float p3_y, float i_x, float i_y) {
        float s1_x, s1_y, s2_x, s2_y;
        s1_x = p1_x - p0_x;     s1_y = p1_y - p0_y;
        s2_x = p3_x - p2_x;     s2_y = p3_y - p2_y;

        float s, t;
        s = (-s1_y * (p0_x - p2_x) + s1_x * (p0_y - p2_y)) / (-s2_x * s1_y + s1_x * s2_y);
        t = ( s2_x * (p0_y - p2_y) - s2_y * (p0_x - p2_x)) / (-s2_x * s1_y + s1_x * s2_y);

        if (s >= 0 && s <= 1 && t >= 0 && t <= 1)
        {
            return true;
        }

        return false;
    }

    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            nw = true;
            mouseDown = true;
            ox = MouseInfo.getPointerInfo().getLocation().x - 200;
            oy = MouseInfo.getPointerInfo().getLocation().y - 50;
            initThread();
            if (e.getClickCount() < 2) {
                if (off) {
                    xx1 = MouseInfo.getPointerInfo().getLocation().x - 200;
                    yy1 = MouseInfo.getPointerInfo().getLocation().y - 50;
                }
            }
            if (option.equals("fill")) {
                for (int i = 0; i < sl.size(); i++) {
                    Shape pp = sl.get(i);
                    if (pp.contains(ox, oy)) {
                        Graphics2D g2 = (Graphics2D) p.getGraphics();
                        g2.setColor(CLR);
                        g2.fill(pp);
                        Action action = new Action();
                        action.action = "fill";
                        action.CLR = CLR;
                        action.x = i;
                        addAction(action);
                    }
                }
            } else if(option.equals("filla")) {
                boolean addit = false;
                int npoints = 0;
                plist.clear();
                for(int j=0; j<lnx.size(); j++) {
                    p1 = new Polygon();
                    npoints = 0;
                    for(int k=0; k<lnx.get(j).size(); k++) {
                        int x1 = lnx.get(j).get(k).x1;
                        int y1 = lnx.get(j).get(k).y1;
                        int x2 = lnx.get(j).get(k).x2;
                        int y2 = lnx.get(j).get(k).y2;
                        Point point1 = new Point();
                        point1.x = x1;
                        point1.y = y1;
                        Point point2 = new Point();
                        point2.x = x2;
                        point2.y = y2;
                        int yyy = lnx.get(j).size();
                        for(int l=0; l<yyy && l!=k; l++) {
                            int _x1 = lnx.get(j).get(l).x1;
                            int _y1 = lnx.get(j).get(l).y1;
                            int _x2 = lnx.get(j).get(l).x2;
                            int _y2 = lnx.get(j).get(l).y2;
                            Point point3 = new Point();
                            point3.x = _x1;
                            point3.y = _y1;
                            Point point4 = new Point();
                            point4.x = _x2;
                            point4.y = _y2;
                            if(get_line_intersection(point1.x, point1.y, point2.x, point2.y, point3.x, point3.y, point4.x, point4.y, 0, 0)) {
                                for(int mm=l; mm<=k; mm++) {
                                    if(1==1||lnx.get(j).get(mm).closed) {
                                        Point pp = new Point();
                                        addit = true;
                                        pp.x = lnx.get(j).get(mm).x1;
                                        pp.y = lnx.get(j).get(mm).y1;
                                        if(mm == l || mm == k) {
                                            pp.x = point2.x;
                                            pp.y = point2.y;
                                        }
                                        p1.addPoint(pp.x, pp.y);
                                        p1.npoints = ++npoints;
                                    } else if(mm != k) {
//                                        addit = false;
//                                        p1.reset();
//                                        npoints = 0;
                                    }
                                }
                                for(int mm=k; mm<=l; mm++) {
                                    if(2==2||lnx.get(j).get(mm).closed) {
                                        Point pp = new Point();
                                        addit = true;
                                        pp.x = lnx.get(j).get(mm).x1;
                                        pp.y = lnx.get(j).get(mm).y1;
                                        if(mm == l || mm == k) {
                                            pp.x = point2.x;
                                            pp.y = point2.y;
                                        }
                                        p1.addPoint(pp.x, pp.y);
                                        p1.npoints = ++npoints;
                                    } else if(mm != l)  {
//                                        addit = false;
//                                        p1.reset();
//                                        npoints = 0;
                                    }
                                }
                            }
                        }
                    }
                    if(addit) {
                        plist.add(p1);
                        addit = false;
                    }
                }
                int yiy = plist.size();
                System.out.println(yiy+"cxxc");
                boolean tri = false;
                for(int i=0; i<yiy; i++) {
                    if (plist.get(i).contains(ox, oy)) {
                        Graphics2D g2 = (Graphics2D) p.getGraphics();
                        g2.setColor(CLR);
                        g2.fill(plist.get(i));
                        tri = true;
                        Action action = new Action();
                        action.action = "filla";
                        action.CLR = CLR;
                        action.x = i;
                        addAction(action);
                    }
                }
                if(!tri) {

                    Action action = new Action();
                    action.action = "filla";
                    action.CLR = CLR;
                    action.x = -646;
                    addAction(action);

                    path = new GeneralPath();

                    path.moveTo(0,0);
                    path.lineTo(0,p.getHeight());
                    path.lineTo(p.getWidth(),p.getHeight());
                    path.lineTo(p.getWidth(),0);
                    path.lineTo(0,0);
                    //path.closePath();
                    int yry = plist.size();
                    for(int ui=0; ui<yry && 1==2; ui++) {
                        int npointz = plist.get(ui).npoints;
                        int xpointz[] = plist.get(ui).xpoints;
                        int ypointz[] = plist.get(ui).ypoints;
                        ArrayList<Integer> xp = new ArrayList<Integer>();
                        ArrayList<Integer> yp = new ArrayList<Integer>();
                        for(int b=0; b<npointz; b++) {
                            boolean w = false;
                            for(int c=0; b!=c && c<npointz; c++) {
                                if(xpointz[c] == xpointz[b] &&
                                        ypointz[c] == ypointz[b]) {
                                    w = true;
                                }
                            }
                            if(!w) {
                                xp.add(xpointz[b]);
                                yp.add(ypointz[b]);
                            }
                        }
                        for(int j = 0; j<xp.size(); j++) {
                            if(j==0)
                                path.moveTo( xp.get(j),  yp.get(j));
                            else
                                path.lineTo( xp.get(j),  yp.get(j));
                            System.out.println(j);
                        }
                        //path.closePath();
                    }
                    
                    Graphics2D g2 = (Graphics2D) p.getGraphics();
                    //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                      //              RenderingHints.VALUE_ANTIALIAS_ON);

                    path.closePath();
                    g2.setColor(CLR);
                    g2.fill(path);
                    
                    doActionList();
                }
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            if (e.getClickCount() > 1) {
                xxxx = MouseInfo.getPointerInfo().getLocation().x - 200;
                yyyy = MouseInfo.getPointerInfo().getLocation().y - 50;

                BufferedImage b = new BufferedImage(Math.abs(xx2 - xx1), Math.abs(yy2 - yy1), BufferedImage.TYPE_INT_RGB);

                Graphics2D g2d = b.createGraphics();
                g2d.setComposite(
                        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));

                g = p.getGraphics();
                g.drawImage(bi, xxxx, yyyy, xxxx + Math.abs(xx2 - xx1), yyyy + Math.abs(yy2 - yy1), xx1, yy1, xx2, yy2, p);
            }
        }
    }

    public void mouseReleased(MouseEvent e) {

        //if(option.equals("pencil"))
        ft = true;
        
        nw = true;

        if (option.equals("copy") && e.getButton() == MouseEvent.BUTTON1) {
            xx2 = MouseInfo.getPointerInfo().getLocation().x - 200;
            yy2 = MouseInfo.getPointerInfo().getLocation().y - 50;
            drawSelecting();
            off = false;
        } else if (e.getButton() == MouseEvent.BUTTON1) {
            off = true;
            mouseDown = false;
            if (option.equals("oval")) {
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "oval";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.x0 = ox;
                action.y0 = oy;
                action.CLR = CLR;
                action.tmp = false;
                addAction(action);
                doAction(action);
            }
            if (option.equals("line")) {
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "line";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.x0 = ox;
                action.y0 = oy;
                action.CLR = CLR;
                action.tmp = false;
                addAction(action);
                doAction(action);
            } else if (option.equals("drect")) {
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "drect";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.x0 = ox;
                action.y0 = oy;
                Shape s = new Shape() {
                    @Override
                    public Rectangle getBounds() {
                        return null;
                    }

                    @Override
                    public Rectangle2D getBounds2D() {
                        return null;
                    }

                    @Override
                    public boolean contains(double x, double y) {
                        return false;
                    }

                    @Override
                    public boolean contains(Point2D p) {
                        return false;
                    }

                    @Override
                    public boolean intersects(double x, double y, double w, double h) {
                        return false;
                    }

                    @Override
                    public boolean intersects(Rectangle2D r) {
                        return false;
                    }

                    @Override
                    public boolean contains(double x, double y, double w, double h) {
                        if (x == action.x0 && y == action.y0
                                && Math.abs(action.x0 - action.x) == w
                                && Math.abs(action.y0 - action.y) == h) {
                            return true;
                        } else {
                            return false;
                        }
                    }

                    public boolean contains(Rectangle2D r) {
                        return false;
                    }

                    public PathIterator getPathIterator(AffineTransform at) {
                        return null;
                    }

                    public PathIterator getPathIterator(AffineTransform at, double flatness) {
                        return null;
                    }
                };
                Shape s1 = new Rectangle(action.x0, action.y0, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
                sl.add(s1);
                action.CLR = CLR;
                action.tmp = false;
                addAction(action);
                doAction(action);
            } else if (option.equals("frect")) {
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "frect";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.x0 = ox;
                action.y0 = oy;
                action.CLR = CLR;
                action.tmp = false;
                addAction(action);
                doAction(action);
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void undrawSelecting() {
        try {
            g = p.getGraphics();

            g.setColor(Color.white);

            g.drawLine(xx1, yy1, xx2, yy1);
            g.drawLine(xx1, yy2, xx2, yy2);

            g.drawLine(xx1, yy1, xx1, yy2);
            g.drawLine(xx2, yy1, xx2, yy2);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    public void drawSelecting() {
        try {
            g = p.getGraphics();

            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(xx1, yy1, xx2, yy1);
            g.setColor(Color.GRAY);
            g.drawLine(xx1, yy2, xx2, yy2);

            g.setColor(Color.BLUE);
            g.drawLine(xx1, yy1, xx1, yy2);
            g.setColor(Color.RED);
            g.drawLine(xx2, yy1, xx2, yy2);

            Dimension d = p.getPreferredSize();
            int w = (int) d.getWidth();
            int h = (int) d.getHeight();

            bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

            gf = bi.createGraphics();

            p.paint(gf);

            gf.dispose();

        } catch (IllegalStateException is) {
            is.printStackTrace();
        }
    }

    private boolean isRunning = false;

    private boolean checkAndMark() {
        if (isRunning) {
            return false;
        }
        isRunning = true;
        return true;
    }

    Thread tt = new Thread() {
        public void run() {
            do {
                if (isRunning) {
                    mousMoved(null);
                }
                if (!mouseDown) {
                    ft = true;
                    isRunning = false;
                }
            } while (true);
        }
    };

    boolean ttf = true;

    private void initThread() {
        if (checkAndMark()) {
            if (ttf) {
                g = p.getGraphics();
                tt.start();
                ttf = false;
            }
        }
    }

    public void clearScreen() {
        g = p.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 1200, 750);
    }
    
    boolean nw = true;

    public void mousMoved(MouseEvent arg0) {
        try {
            if (option.equals("line")) {
                clearScreen();
                doActionList();
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "line";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.x0 = ox;
                action.y0 = oy;
                action.CLR = CLR;
                action.tmp = true;
                //doAction(action);
            } else if (option.equals("drect")) {
                clearScreen();
                doActionList();
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "drect";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.x0 = ox;
                action.y0 = oy;
                action.CLR = CLR;
                action.tmp = true;
                //doAction(action);
            } else if (option.equals("frect")) {
                clearScreen();
                doActionList();
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "frect";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.x0 = ox;
                action.y0 = oy;
                action.CLR = CLR;
                action.tmp = true;
                //doAction(action);
            } else if (option.equals("pencil")) {
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "pencil";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.x0 = ox;
                action.y0 = oy;
                //if(ft == true)
                action.ptnTn = ptnTn;
                action.pencilFirst = false;
                action.CLR = CLR;
                Action a = null;
                boolean br = false;
                for(int i=actionList.size()-1; i>=0 && !br; i--) {
                    if(actionList.get(i).action.equals("pencil")) {
                        a = actionList.get(i);
                        br = true;
                    }
                }
//                try {
//                    a = actionList.get(actionList.size()-2);
//                } catch(Exception e) {
//                    e.printStackTrace();
//                }
                if(a != null) {
                    Line line = new Line();
                    line.x1 = a.x;
                    line.y1 = a.y;
                    line.x2 = action.x;
                    line.y2 = action.y;
                    line.closed = false;
                    if(lines.size() == 0) {
                        action.action = "bpencil";
                        line.closed = false;
                    }
                    boolean t = true;
                    for(int u=0; u<lines.size(); u++) {
                        Line lin = lines.get(u);
                        if(line.x1 == lin.x1 &&
                                line.y1 == lin.y1 &&
                                line.x2 == lin.x2 &&
                                line.y2 == lin.y2) {
                            t = false;
                        }
                    }
                    if(t) {
                        if(!nw)
                            lines.add(line);
                        try {
                            if(nw && !(a.x == ox && a.y == oy)) {
                                if(lines.size() > 0){
                                    action.action = "bpencil";
                                    lines.get(lines.size()-1).closed = false;
                                    lnx.add(lines);
                                }
                                lines = new ArrayList<Line>();
                            } else {
                                lines.get(lines.size()-1).closed = true;
                            }
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                        if(nw)
                            lines.add(line);
                    }
                }
                doAction(action);
                addAction(action);
                nw = false;
            } else if (option.equals("erase")) {
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "erase";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.etnTn = etnTn;
                action.x0 = ox;
                action.y0 = oy;
                action.CLR = CLR;
                action.tmp = false;
                addAction(action);
                doAction(action);
            } else if (option.equals("spray")) {
                Action action = new Action();
                action.id = actionList.get(actionList.size() - 1).id + 1;
                action.action = "spray";
                x = MouseInfo.getPointerInfo().getLocation().x - 200;
                y = MouseInfo.getPointerInfo().getLocation().y - 50;
                action.x = x;
                action.y = y;
                action.x0 = ox;
                action.y0 = ox;
                //if(ft == true)
                action.ptnTn = ptnTn;
                action.pencilFirst = false;
                action.CLR = CLR;
                doAction(action);
                addAction(action);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void addAction(Action action) {
        actionList.add(action);
    }

    private void doActionList() {
        try {
            int index = 0;
            while (index < actionList.size()) {
                if (!actionList.get(index).tmp) {
                    if (actionList.get(index).action.equals("pencil")
                            && actionList.get(index).pencilFirst == true) {
                        ft = true;
                    }
                    if(actionList.get(index).x != -646)
                        doAction(actionList.get(index));
                }
                index++;
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    boolean ft = true;

    public void doAction(Action action) {
        if (action.action.equals("oval")) {
            g.setColor(action.CLR);
            g.drawOval(action.x0, action.y0, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
        } else if (action.action.equals("fill")) {
            Shape pp = sl.get(action.x);
            Graphics2D g2 = (Graphics2D) p.getGraphics();
            g2.setColor(action.CLR);
            g2.fill(pp);
        } else if (action.action.equals("filla")) {
            if(action.x != -646) {
                Graphics2D g2 = (Graphics2D) p.getGraphics();
                g2.setColor(action.CLR);
                g2.fill(plist.get(action.x));
            }
            if(action.x == -646) {
                path = new GeneralPath();

                path.moveTo(0,0);
                path.lineTo(0,p.getHeight());
                path.lineTo(p.getWidth(),p.getHeight());
                path.lineTo(p.getWidth(),0);
                path.lineTo(0,0);

                Graphics2D g22 = (Graphics2D) p.getGraphics();

                path.closePath();
                g22.setColor(CLR);
                g22.fill(path);

                doActionList();
            }
        } else if (action.action.equals("line")) {
            g.setColor(action.CLR);
            g.drawLine(action.x0, action.y0, action.x, action.y);
        } else if (action.action.equals("spray")) {
            g.setColor(action.CLR);
            for (int i = 0; i < 1; i++) {
                Random rd = new Random();
                int x = rd.nextInt(20);
                int y = rd.nextInt(20);
                int x1 = rd.nextInt(18) - rd.nextInt(18);
                int y1 = rd.nextInt(18) - rd.nextInt(18);
                g.drawOval(action.x + x1, action.y + y1, x, y);
            }
        } else if (action.action.contains("pencil")) {
            g.setColor(action.CLR);
            if (!action.pencilFirst) {
                Action a = null;
                for (int i = actionList.size() - 1; i >= 0; i--) {
                    if (i == action.id - 1 && !ft) {
                        a = actionList.get(i);
                    }
                }
                //if(!ft)
                //a = actionList.get(action.id - 1);
                try {
                    if(!action.action.equals("bpencil")) {
                        if (action.ptnTn == 1) {
                            g.drawLine(a.x, a.y, action.x, action.y);
                        }
                        if (action.ptnTn == 2) {
                            g.drawLine(a.x, a.y, action.x, action.y);
    //                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
                            g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                        }
                        if (action.ptnTn == 3) {
                            g.drawLine(a.x, a.y, action.x, action.y);
    //                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
    //                                    g.drawLine(a.x+2, a.y, action.x+2, action.y);                                
                            g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                            g.drawLine(a.x, a.y + 2, action.x, action.y + 2);
                        }
                        if (action.ptnTn == 4) {
                            g.drawLine(a.x, a.y, action.x, action.y);
    //                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
    //                                    g.drawLine(a.x+2, a.y, action.x+2, action.y);                                
    //                                    g.drawLine(a.x+3, a.y, action.x+3, action.y);                                
                            g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                            g.drawLine(a.x, a.y + 2, action.x, action.y + 2);
                            g.drawLine(a.x, a.y + 3, action.x, action.y + 3);
                        }
                        if (action.ptnTn == 5) {
                            g.drawLine(a.x, a.y, action.x, action.y);
    //                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
    //                                    g.drawLine(a.x+2, a.y, action.x+2, action.y);                                
    //                                    g.drawLine(a.x+3, a.y, action.x+3, action.y);                                
    //                                    g.drawLine(a.x+4, a.y, action.x+4, action.y);                                
                            g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                            g.drawLine(a.x, a.y + 2, action.x, action.y + 2);
                            g.drawLine(a.x, a.y + 3, action.x, action.y + 3);
                            g.drawLine(a.x, a.y + 4, action.x, action.y + 4);
                        }
                        if (action.ptnTn == 10) {
                            g.drawLine(a.x, a.y, action.x, action.y);
    //                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
    //                                    g.drawLine(a.x+2, a.y, action.x+2, action.y);                                
    //                                    g.drawLine(a.x+3, a.y, action.x+3, action.y);                                
    //                                    g.drawLine(a.x+4, a.y, action.x+4, action.y);                                
    //                                    g.drawLine(a.x+5, a.y, action.x+5, action.y);                                
    //                                    g.drawLine(a.x+6, a.y, action.x+6, action.y);                                
    //                                    g.drawLine(a.x+7, a.y, action.x+7, action.y);                                
    //                                    g.drawLine(a.x+8, a.y, action.x+8, action.y);                                
    //                                    g.drawLine(a.x+9, a.y, action.x+9, action.y);                                
    //                                    
                            g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                            g.drawLine(a.x, a.y + 2, action.x, action.y + 2);
                            g.drawLine(a.x, a.y + 3, action.x, action.y + 3);
                            g.drawLine(a.x, a.y + 4, action.x, action.y + 4);
                            g.drawLine(a.x, a.y + 5, action.x, action.y + 5);
                            g.drawLine(a.x, a.y + 6, action.x, action.y + 6);
                            g.drawLine(a.x, a.y + 7, action.x, action.y + 7);
                            g.drawLine(a.x, a.y + 8, action.x, action.y + 8);
                            g.drawLine(a.x, a.y + 9, action.x, action.y + 9);
                        }
                        if (action.ptnTn == 20) {
                            g.drawLine(a.x, a.y, action.x, action.y);
                            for (int i = 1; i < 20; i++) {
                                //g.drawLine(a.x+i, a.y, action.x+i, action.y);                                
                                g.drawLine(a.x, a.y + i, action.x, action.y + i);
                            }
                        }
                        if (action.ptnTn == 50) {
                            g.drawLine(a.x, a.y, action.x, action.y);
                            for (int i = 1; i < 50; i++) {
                                //g.drawLine(a.x+i, a.y, action.x+i, action.y);                                
                                g.drawLine(a.x, a.y + i, action.x, action.y + i);
                            }
                        }
                        if (action.ptnTn == 100) {
                            g.drawLine(a.x, a.y, action.x, action.y);
                            for (int i = 1; i < 100; i++) {
                                //g.drawLine(a.x-i, a.y, action.x-i, action.y);                                
                                g.drawLine(a.x, a.y + i, action.x, action.y + i);
                                //g.drawLine(a.x-i, a.y+i, action.x-i, action.y+i);                                
                            }
                        }
                    }

                } catch (Exception e) {
                    g.drawLine(action.x, action.y, action.x - 1, action.y);
                    ft = false;
                }
            } else {
                g.drawLine(action.x, action.y, action.x - 1, action.y);
                ft = false;
            }
        } else if (action.action.equals("erase")) {
            g.setColor(Color.WHITE);
            if (!action.pencilFirst) {
                Action a = null;
                for (int i = actionList.size() - 1; i >= 0; i--) {
                    if (i == action.id - 1 && !ft) {
                        a = actionList.get(i);
                    }
                }
                try {
                    if (action.etnTn == 1) {
                        g.drawLine(a.x, a.y, action.x, action.y);
                    }
                    if (action.etnTn == 2) {
                        g.drawLine(a.x, a.y, action.x, action.y);
//                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
                        g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                    }
                    if (action.etnTn == 3) {
                        g.drawLine(a.x, a.y, action.x, action.y);
//                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
//                                    g.drawLine(a.x+2, a.y, action.x+2, action.y);                                
                        g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                        g.drawLine(a.x, a.y + 2, action.x, action.y + 2);
                    }
                    if (action.etnTn == 4) {
                        g.drawLine(a.x, a.y, action.x, action.y);
//                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
//                                    g.drawLine(a.x+2, a.y, action.x+2, action.y);                                
//                                    g.drawLine(a.x+3, a.y, action.x+3, action.y);                                
                        g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                        g.drawLine(a.x, a.y + 2, action.x, action.y + 2);
                        g.drawLine(a.x, a.y + 3, action.x, action.y + 3);
                    }
                    if (action.etnTn == 5) {
                        g.drawLine(a.x, a.y, action.x, action.y);
//                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
//                                    g.drawLine(a.x+2, a.y, action.x+2, action.y);                                
//                                    g.drawLine(a.x+3, a.y, action.x+3, action.y);                                
//                                    g.drawLine(a.x+4, a.y, action.x+4, action.y);                                
                        g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                        g.drawLine(a.x, a.y + 2, action.x, action.y + 2);
                        g.drawLine(a.x, a.y + 3, action.x, action.y + 3);
                        g.drawLine(a.x, a.y + 4, action.x, action.y + 4);
                    }
                    if (action.etnTn == 10) {
                        g.drawLine(a.x, a.y, action.x, action.y);
//                                    g.drawLine(a.x+1, a.y, action.x+1, action.y);                                
//                                    g.drawLine(a.x+2, a.y, action.x+2, action.y);                                
//                                    g.drawLine(a.x+3, a.y, action.x+3, action.y);                                
//                                    g.drawLine(a.x+4, a.y, action.x+4, action.y);                                
//                                    g.drawLine(a.x+5, a.y, action.x+5, action.y);                                
//                                    g.drawLine(a.x+6, a.y, action.x+6, action.y);                                
//                                    g.drawLine(a.x+7, a.y, action.x+7, action.y);                                
//                                    g.drawLine(a.x+8, a.y, action.x+8, action.y);                                
//                                    g.drawLine(a.x+9, a.y, action.x+9, action.y);                                
//                                    
                        g.drawLine(a.x, a.y + 1, action.x, action.y + 1);
                        g.drawLine(a.x, a.y + 2, action.x, action.y + 2);
                        g.drawLine(a.x, a.y + 3, action.x, action.y + 3);
                        g.drawLine(a.x, a.y + 4, action.x, action.y + 4);
                        g.drawLine(a.x, a.y + 5, action.x, action.y + 5);
                        g.drawLine(a.x, a.y + 6, action.x, action.y + 6);
                        g.drawLine(a.x, a.y + 7, action.x, action.y + 7);
                        g.drawLine(a.x, a.y + 8, action.x, action.y + 8);
                        g.drawLine(a.x, a.y + 9, action.x, action.y + 9);
                    }
                    if (action.etnTn == 20) {
                        g.drawLine(a.x, a.y, action.x, action.y);
                        for (int i = 1; i < 20; i++) {
                            //g.drawLine(a.x+i, a.y, action.x+i, action.y);                                
                            g.drawLine(a.x, a.y + i, action.x, action.y + i);
                        }
                    }
                    if (action.etnTn == 50) {
                        g.drawLine(a.x, a.y, action.x, action.y);
                        for (int i = 1; i < 50; i++) {
                            //g.drawLine(a.x+i, a.y, action.x+i, action.y);                                
                            g.drawLine(a.x, a.y + i, action.x, action.y + i);
                        }
                    }
                    if (action.etnTn == 100) {
                        g.drawLine(a.x, a.y, action.x, action.y);
                        for (int i = 1; i < 100; i++) {
                            //g.drawLine(a.x-i, a.y, action.x-i, action.y);                                
                            g.drawLine(a.x, a.y + i, action.x, action.y + i);
                            //g.drawLine(a.x-i, a.y+i, action.x-i, action.y+i);                                
                        }
                    }
                } catch (Exception e) {
                    g.drawLine(action.x, action.y, action.x - 1, action.y);
                    ft = false;
                }
            } else {
                g.drawLine(action.x, action.y, action.x - 1, action.y);
                ft = false;
            }
        } else if (action.action.equals("drect")) {
            g.setColor(action.CLR);
            if (action.x0 < action.x && action.y0 < action.y) {
                g.drawRect(action.x0, action.y0, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
            }
            if (action.x0 > action.x && action.y0 < action.y) {
                g.drawRect(action.x, action.y0, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
            }
            if (action.x0 < action.x && action.y0 > action.y) {
                g.drawRect(action.x0, action.y, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
            }
            if (action.x0 > action.x && action.y0 > action.y) {
                g.drawRect(action.x, action.y, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
            }
        } else if (action.action.equals("frect")) {
            g.setColor(action.CLR);
            if (action.x0 < action.x && action.y0 < action.y) {
                g.fillRect(action.x0, action.y0, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
            }
            if (action.x0 > action.x && action.y0 < action.y) {
                g.fillRect(action.x, action.y0, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
            }
            if (action.x0 < action.x && action.y0 > action.y) {
                g.fillRect(action.x0, action.y, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
            }
            if (action.x0 > action.x && action.y0 > action.y) {
                g.fillRect(action.x, action.y, Math.abs(action.x0 - action.x), Math.abs(action.y0 - action.y));
            }
        }
    }

    public Draw() {
        Action actio = new Action();
        actio.id = 0;
        actionList.add(actio);
        j.setLayout(null);
        j.setBounds(0, 0, 1200, 750);
        p.setBounds(200, 0, 1000, 750);
        p.setBackground(Color.white);
        j.add(p);
        p.addMouseListener(this);
        j.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        JMenuItem ab = new JMenuItem();

        ab.setText("about");

        help.add(ab);

        ab.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                final JDialog d = new JDialog(j);
                d.setBounds(0, 0, 500, 500);
                JLabel l = new JLabel("by DHC");
                l.setBounds(0, 0, 50, 20);
                d.add(l);
                d.setLayout(null);
                JButton b = new JButton("Close");
                d.add(b);
                b.setBounds(100, 100, 75, 60);
                b.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {

                        d.dispose();
                    }
                });
                d.setVisible(true);
            }
        });

        JMenuItem mi5 = new JMenuItem();

        mi5.setText("undo");

        mi5.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                clearScreen();
                if (actionList.get(actionList.size() - 1).action.equals("pencil")) {
                    boolean isPencilStill = true;
                    for (int i = actionList.size() - 1; i >= 0 && isPencilStill; i--) {
                        if (actionList.get(actionList.size() - 1).action.equals("pencil")) {
                            undoList.add(actionList.get(i));
                            actionList.remove(actionList.get(i));
                            actionList.trimToSize();
                        } else {
                            isPencilStill = false;
                        }
                        if (isPencilStill == false) {
                            break;
                        }
                    }
                } else {
                    undoList.add(actionList.get(actionList.size() - 1));
                    actionList.remove(actionList.size() - 1);
                    actionList.trimToSize();
                }
                doActionList();
            }
        });

        JMenuItem mi6 = new JMenuItem();

        mi6.setText("redo");

        mi6.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                clearScreen();
                try {
                    if (undoList.get(undoList.size() - 1).action.equals("pencil")) {
                        boolean isPencilStill = true;
                        for (int i = undoList.size() - 1; i > 0 && isPencilStill; i--) {
                            if (undoList.get(undoList.size() - 1).action.equals("pencil")) {
                                actionList.add(undoList.get(i));
                                undoList.remove(undoList.get(i));
                            }
                        }
                    } else {
                        actionList.add(undoList.get(undoList.size() - 1));
                        undoList.remove(undoList.get(undoList.size() - 1));
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                }
                undoList.trimToSize();
                doActionList();
            }
        });

        edit.add(mi5);
        edit.add(mi6);

        JMenuItem mi1 = new JMenuItem();

        mi1.setText("save");

        JMenuItem mi3 = new JMenuItem();

        JMenuItem mi7 = new JMenuItem();

        mi7.setText("use eraser");

        mi7.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                option = "erase";
            }
        });

        image.add(mi7);

        mi3.setText("use pencil");

        mi3.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                option = "pencil";
            }
        });

        JMenuItem mi2 = new JMenuItem();

        JMenuItem mi9 = new JMenuItem();

        mi9.setText("fill rectangle");

        mi9.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                option = "frect";
            }
        });

        image.add(mi9);

        JMenuItem mi8 = new JMenuItem();

        mi8.setText("draw rectangle");

        mi8.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                option = "drect";
            }
        });

        image.add(mi8);

        mi2.setText("end program");

        m.add(file);

        m.add(edit);

        JMenuItem black = new JMenuItem();

        black.setText("BLACK");

        black.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CLR = Color.BLACK;
                btn.setSelectedColor(CLR);
            }
        });

        JMenuItem org = new JMenuItem();

        org.setText("ORANGE");

        org.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CLR = Color.ORANGE;
                btn.setSelectedColor(CLR);
            }
        });

        JMenuItem re = new JMenuItem();

        re.setText("RED");

        re.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CLR = Color.RED;
                btn.setSelectedColor(CLR);
            }
        });

        edit.add(re);

        JMenuItem ye = new JMenuItem();

        ye.setText("YELLOW");

        ye.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CLR = Color.YELLOW;
                btn.setSelectedColor(CLR);
            }
        });

        edit.add(ye);

        JMenuItem gr = new JMenuItem();

        gr.setText("GRAY");

        gr.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CLR = Color.GRAY;
                btn.setSelectedColor(CLR);
            }
        });

        edit.add(gr);

        JMenuItem bl = new JMenuItem();

        bl.setText("BLUE");

        bl.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CLR = Color.BLUE;
                btn.setSelectedColor(CLR);
            }
        });

        edit.add(bl);
        edit.add(org);
        edit.add(black);

        m.add(image);
        m.add(help);

        edit.setText("Edit");
        image.setText("Tool");
        help.setText("Help");

        JMenuItem mi4 = new JMenuItem();
        mi4.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                option = "line";
            }
        });

        mi4.setText("draw line");

        JMenuItem mii = new JMenuItem();
        mii.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                option = "copy";
            }
        });

        mii.setText("copy");

        image.add(mii);
        image.add(mi3);
        image.add(mi4);

        mi2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.exit(0);
            }
        });

        mi1.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser chooser = new JFileChooser();
                int returnVal = chooser.showSaveDialog(j);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        Robot robot = new Robot();
                        String format = "jpg";
                        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                        BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
                        ImageIO.write(screenFullImage, format, new File(chooser.getSelectedFile().getAbsolutePath()));

                        System.out.println("A full screenshot saved!");
                    } catch (Exception ex) {
                        System.err.println(ex);
                    }
                    JOptionPane.showMessageDialog(null, "saved!");
                }
            }
        });

        file.add(mi2);
        file.setText("File");

        q.setBounds(0, 0, 193, 350);
        q.setBackground(Color.white);
        j.add(q);
        JButton pnc = new JButton("use brush");
        pnc.setBounds(0, 0, 100, 35);
        JButton lne = new JButton("draw a line");
        lne.setBounds(100, 0, 100, 35);
        JButton drect = new JButton("draw rectangle");
        drect.setBounds(0, 35, 100, 35);
        JButton frect = new JButton("filled rectangle");
        frect.setBounds(100, 35, 100, 35);

        q.add(pnc);
        q.add(lne);
        q.add(drect);
        q.add(frect);

        q.add(btn);
        
        JPanel q1 = new JPanel();
        q1.setBackground(Color.orange);
        JPanel q2 = new JPanel();
        q2.setBackground(Color.red);
        JPanel q3 = new JPanel();
        q3.setBackground(Color.green);
        JPanel q4 = new JPanel();
        q4.setBackground(Color.blue);
        JPanel q5 = new JPanel();
        q5.setBackground(Color.green);
        JPanel q6 = new JPanel();
        q6.setBackground(Color.blue);
        JPanel q7 = new JPanel();
        q7.setBackground(Color.pink);
        JPanel q8 = new JPanel();
        q8.setBackground(Color.yellow);
        JPanel q9 = new JPanel();
        q9.setBackground(Color.orange);
        JPanel q10 = new JPanel();
        q10.setBackground(Color.red);
        JPanel q11 = new JPanel();
        q11.setBackground(Color.green);
        JPanel q12 = new JPanel();
        q12.setBackground(Color.blue);
        JPanel q13 = new JPanel();
        q13.setBackground(Color.green);
        JPanel q14 = new JPanel();
        q14.setBackground(Color.blue);
        JPanel q15 = new JPanel();
        q15.setBackground(Color.pink);
        JPanel q16 = new JPanel();
        q16.setBackground(Color.yellow);
        JPanel q17 = new JPanel();
        q17.setBackground(Color.pink);
        JPanel q18 = new JPanel();
        q18.setBackground(Color.yellow);
        JPanel q19 = new JPanel();
        q19.setBackground(Color.yellow);
        JPanel q20 = new JPanel();
        q20.setBackground(Color.yellow);

        q1.setBounds(0,350,193,20);
        q2.setBounds(0,370,193,20);
        q3.setBounds(0,390,193,20);
        q4.setBounds(0,410,193,20);
        q5.setBounds(0,430,193,20);
        q6.setBounds(0,450,193,20);
        q7.setBounds(0,470,193,20);
        q8.setBounds(0,490,193,20);
        q9.setBounds(0,510,193,20);
        q10.setBounds(0,530,193,20);
        q11.setBounds(0,550,193,20);
        q12.setBounds(0,570,193,20);
//        q.add(q1);
//        q.add(q2);
//        q.add(q3);
//        q.add(q4);
//        q.add(q5);
//        q.add(q6);
//        q.add(q7);
//        q.add(q8);
//        q.add(q9);
//        q.add(q10);
//        q.add(q11);
//        q.add(q12);
        
        Thread t = new Thread() {
            public void run() {
                try {
                    while(true) {
                        Thread.sleep(150);
                        Random r = new Random();
                        int v1 = r.nextInt(256);
                        int v2 = r.nextInt(256);
                        int v3 = r.nextInt(256);
                        Color c1 = new Color(v1,v2,v3);
                        v1 = r.nextInt(256);
                        v2 = r.nextInt(256);
                        v3 = r.nextInt(256);
                        Color c2 = new Color(v1,v2,v3);                    
                        v1 = r.nextInt(256);
                        v2 = r.nextInt(256);
                        v3 = r.nextInt(256);
                        Color c3 = new Color(v1,v2,v3);                    
                        v1 = r.nextInt(256);
                        v2 = r.nextInt(256);
                        v3 = r.nextInt(256);
                        Color c4 = new Color(v1,v2,v3);                    
                        v1 = r.nextInt(256);
                        v2 = r.nextInt(256);
                        v3 = r.nextInt(256);
                        Color c5 = new Color(v1,v2,v3);                    
                        v1 = r.nextInt(256);
                        v2 = r.nextInt(256);
                        v3 = r.nextInt(256);
                        Color c6 = new Color(v1,v2,v3);                    
                        v1 = r.nextInt(256);
                        v2 = r.nextInt(256);
                        v3 = r.nextInt(256);
                        Color c7 = new Color(v1,v2,v3);                    
                        v1 = r.nextInt(256);
                        v2 = r.nextInt(256);
                        v3 = r.nextInt(256);
                        Color c8 = new Color(v1,v2,v3);                    
                        v1 = r.nextInt(256);
                        v2 = r.nextInt(256);
                        v3 = r.nextInt(256);
                        Color c9 = new Color(v1,v2,v3);                    
                        v1 = r.nextInt(256);
                        v2 = r.nextInt(256);
                        v3 = r.nextInt(256);
                        Color c10 = new Color(v1,v2,v3);                    
                        v1 = r.nextInt(256);
                        v2 = r.nextInt(256);
                        v3 = r.nextInt(256);
                        Color c11 = new Color(v1,v2,v3);                    
                        v1 = r.nextInt(256);
                        v2 = r.nextInt(256);
                        v3 = r.nextInt(256);
                        Color c12 = new Color(v1,v2,v3);                    
                        v1 = r.nextInt(256);
                        v2 = r.nextInt(256);
                        v3 = r.nextInt(256);
                        q1.setBackground(c1);
                        q2.setBackground(c2);
                        q3.setBackground(c3);
                        q4.setBackground(c4);
                        q5.setBackground(c5);
                        q6.setBackground(c6);
                        q7.setBackground(c7);
                        q8.setBackground(c8);
                        q9.setBackground(c9);
                        q10.setBackground(c10);
                        q11.setBackground(c11);
                        q12.setBackground(c12);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        
        JButton wh = new JButton("white");
        wh.setBounds(0, 370, 100, 30);
        JButton yell = new JButton("yellow");
        yell.setBounds(0, 70, 100, 30);
        yell.setBackground(Color.yellow);
        JButton orag = new JButton("orange");
        orag.setBounds(0, 100, 100, 30);
        orag.setBackground(Color.orange);
        JButton pnk = new JButton("pink");
        pnk.setBounds(0, 130, 100, 30);
        pnk.setBackground(Color.pink);
        JButton ble = new JButton("blue");
        ble.setBounds(0, 160, 100, 30);
        ble.setBackground(Color.blue);
        JButton green = new JButton("green");
        green.setBackground(Color.GREEN);
        green.setBounds(0, 190, 100, 30);
        JButton rd = new JButton("red");
        rd.setBounds(0, 220, 100, 30);
        rd.setBackground(Color.red);
        JButton grey = new JButton("grey");
        grey.setBounds(0, 250, 100, 30);
        grey.setBackground(Color.GRAY);
        JButton balk = new JButton("black");
        balk.setBounds(0, 280, 100, 30);
        balk.setBackground(Color.BLACK);
        balk.setForeground(Color.WHITE);
        JButton cyn = new JButton("cyan");
        cyn.setBackground(Color.CYAN);
        cyn.setBounds(0, 310, 100, 30);
        JButton era = new JButton("erase");
        era.setBounds(0, 340, 100, 30);
        JComboBox etn = new JComboBox();
        q.add(etn);
        etn.addItem("erase thickness");
        etn.addItem("1");
        etn.addItem("2");
        etn.addItem("3");
        etn.addItem("4");
        etn.addItem("5");
        etn.addItem("10");
        etn.addItem("20");
        etn.addItem("50");
        etn.addItem("100");
        etn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int v = Integer.parseInt(((JComboBox) e.getSource()).getSelectedItem().toString());
                etnTn = v;
            }
        });
        JButton fill = new JButton("fill rectangle");
        fill.setBackground(Color.magenta);
        JComboBox ptn = new JComboBox();
        q.add(ptn);
        ptn.addItem("brush thickness");
        ptn.addItem("1");
        ptn.addItem("2");
        ptn.addItem("3");
        ptn.addItem("4");
        ptn.addItem("5");
        ptn.addItem("10");
        ptn.addItem("20");
        ptn.addItem("50");
        ptn.addItem("100");
        ptn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int v = Integer.parseInt(((JComboBox) e.getSource()).getSelectedItem().toString());
                ptnTn = v;
            }
        });

        pnc.setBackground(Color.magenta);
        lne.setBackground(Color.magenta);
        drect.setBackground(Color.magenta);
        frect.setBackground(Color.magenta);
        era.setBackground(Color.magenta);

        JButton spray = new JButton("spray");
        spray.setBackground(Color.magenta);
        spray.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                option = "spray";
            }
        });

        JButton fit = new JButton("fit img to screen");
        fit.setBackground(Color.magenta);
        fit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clearScreen();
                    Graphics2D g2 = (Graphics2D) p.getGraphics();
                    g2.drawImage(image1, 0, 0, p.getWidth(), p.getHeight(), null);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        JButton og = new JButton("fit img to orign. size");
        og.setBackground(Color.magenta);
        og.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clearScreen();
                    Graphics2D g2 = (Graphics2D) p.getGraphics();
                    g2.drawImage(image1, 0, 0, null);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        btn.setBackground(Color.DARK_GRAY);

        q.add(spray);
        q.add(fit);
        q.add(og);
        q.add(fill);
        q.add(yell);
        q.add(orag);
        q.add(pnk);
        q.add(ble);
        q.add(green);
        q.add(rd);
        q.add(grey);
        q.add(balk);
        q.add(cyn);
        q.add(era);
        q.add(wh);

        pnc.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                option = "pencil";
            }
        });
        fill.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                option = "fill";
            }
        });
        wh.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.white;
                btn.setSelectedColor(Color.gray);
            }
        });
        lne.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                option = "line";
            }
        });
        drect.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                option = "drect";
            }
        });
        frect.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                option = "frect";
            }
        });

        yell.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.YELLOW;
                btn.setSelectedColor(CLR);
            }
        });

        grey.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.GRAY;
                btn.setSelectedColor(CLR);
            }
        });

        orag.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.ORANGE;
                btn.setSelectedColor(CLR);
            }
        });

        pnk.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.PINK;
                btn.setSelectedColor(CLR);
            }
        });

        rd.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.RED;
                btn.setSelectedColor(CLR);
            }
        });

        ble.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.blue;
                btn.setSelectedColor(CLR);
            }
        });

        balk.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.BLACK;
                btn.setSelectedColor(CLR);
            }
        });

        cyn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.CYAN;
                btn.setSelectedColor(CLR);
            }
        });

        green.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CLR = Color.green;
                btn.setSelectedColor(CLR);
            }
        });

        era.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                option = "erase";
            }
        });

        JButton oval = new JButton("oval");
        oval.setBackground(Color.magenta);
        q.add(oval);
        oval.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                option = "oval";
            }
        });

        JMenuItem save = new JMenuItem("save");
        file.add(save);
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Rectangle screenRect = new Rectangle(200, 44, j.getWidth() - 200, j.getHeight() - 44);
                    BufferedImage capture = new Robot().createScreenCapture(screenRect);
                    ImageIO.write(capture, "jpg", new File("imagetemp.jpg"));
                    JFileChooser fc = new JFileChooser();
                    int returnVal = fc.showSaveDialog(p);
                    Thread t = new Thread() {
                        public void run() {
                            if (returnVal == JFileChooser.APPROVE_OPTION) {
                                File file = new File(fc.getSelectedFile().getAbsolutePath());
                                File f = new File("imagetemp.jpg");
                                f.renameTo(file);
                            }
                        }
                    };
                    t.start();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (AWTException awte) {
                    awte.printStackTrace();
                }
            }
        });

        JMenuItem open = new JMenuItem("open");
        file.add(open);
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(p);
                Thread t = new Thread() {
                    public void run() {
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = new File(fc.getSelectedFile().getAbsolutePath());
                            if (!file.getAbsolutePath().endsWith(".jpg")
                                    && !file.getAbsolutePath().endsWith(".gif")) {
                                JOptionPane.showMessageDialog(null, "Not a jpg or gif");
                            } else {
                                try {
                                    ///Thread.sleep(2000);
                                    image1 = ImageIO.read(file);
                                    Graphics2D g2 = (Graphics2D) p.getGraphics();
                                    g2.drawImage(image1, 0, 0, p.getWidth(), p.getHeight(), p);
                                } catch (Exception ex) {
                                    System.out.println("ERROR");
                                }
                            }
                        }
                    }
                };
                t.start();
            }
        });

        JMenuItem neww = new JMenuItem("clear screen");
        file.add(neww);
        neww.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                p.setBackground(Color.WHITE);
                clearScreen();
            }
        });

        JButton filla1 = new JButton("fill brush polygon");
        q.add(filla1);
        filla1.setBackground(Color.magenta);
        filla1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                option = "filla";
            }
        });

        JButton main = new JButton("starting up ");
        p.add(main);
        p.setLayout(null);
        p.setBackground(Color.RED);
        main.setFont(new Font("gulim", Font.BOLD, 50));
        main.setBounds(0, 0, j.getWidth(), j.getHeight());
        main.setBackground(Color.white);
        main.setForeground(Color.blue);
        CLR = Color.BLACK;
        btn.setSelectedColor(CLR);
        main.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (f) {
                    p.remove(main);
                    p.setBackground(Color.WHITE);
                    clearScreen();
                }
            }
        });
        Thread t1 = new Thread() {
            public void run() {
                int x = 0;
                while (true) {
                    try {
                        Thread.sleep(100);
                        if(x > 20 && x <50) {
                            main.setText(main.getText().replace("starting up", "loading modules"));
                            j.setTitle(main.getText());
                        } else if(x > 50 && x <70) {
                            main.setText(main.getText().replace("loading modules", "starting modules"));
                            j.setTitle(main.getText());
                        } else if(x > 70 && x <90) {
                            main.setText(main.getText().replace("starting modules", "done loading modules"));
                            j.setTitle(main.getText());
                        } else if(x > 90 && x <132) {
                            main.setText(main.getText().replace("done loading modules ", "done/please wait"));
                            main.setText(main.getText().replace(".", ""));
                            j.setTitle(main.getText());
                        }
                        x++;
                        if(x < 132)
                            main.setText(main.getText() + ".");
                        if (x > 132) {
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                f = true;
                main.setText("click this to begin");
                main.setBackground(Color.CYAN);
                j.setTitle("iDraw v.2");
            }
        };
        t1.start();
        JLabel title = new JLabel("iDraw");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("arial", Font.BOLD, 45));
        q.add(title);
        JLabel title2 = new JLabel("v.2");
        title2.setForeground(Color.WHITE);
        title2.setFont(new Font("arial", Font.BOLD, 25));
        q.add(title2);
        q.setBounds(0,0,190,750);
        j.setJMenuBar(m);
        j.setExtendedState(JFrame.MAXIMIZED_BOTH);
        j.setVisible(true);
        try {
            javax.swing.ImageIcon i = new javax.swing.ImageIcon(getClass().getResource("iag.gif"));
            JLabel jl = new JLabel();
            jl.setIcon(i);
            q.add(jl);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        Draw draw = new Draw();
        draw.q.setBackground(Color.BLACK);
        Dimension DimMax = Toolkit.getDefaultToolkit().getScreenSize();
        draw.j.setExtendedState(JFrame.MAXIMIZED_BOTH);
        draw.j.setMaximumSize(DimMax);
        draw.j.setBounds(0, 0, (int) DimMax.getWidth(), (int) DimMax.getHeight());
        draw.p.setBounds(200, 0, DimMax.width - 200, 750);
        draw.j.setResizable(false);
    }
    boolean f = false;
    BufferedImage image1 = null;
}
