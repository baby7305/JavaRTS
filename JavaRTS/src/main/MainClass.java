package main;


import NaturalResources.Mineral;
import NaturalResources.Vespene;
import Structures.Barracks;
import Structures.CommandCentre;
import Tiles.TileMap;
import Units.Formation;
import Units.SCV;
import Units.Unit;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class MainClass extends JPanel implements KeyListener, MouseListener {

    public static final int SCREENWIDTH = 1920;
    public static final int SCREENHEIGHT = 1080;
    boolean quit = false;
    public int curCount = 0;
    public Vector<Unit> focusedUnits = new Vector<Unit>();
    boolean shift = false;
    public Formation formUnits = new Formation();
    boolean mousePressed = false;
    public static Vector<GameObject> focusables = new Vector<GameObject>();
    public static int numMinerals = 0;
    public JFrame frame;
    public TileMap gameMap;
    public static int numVespene = 0;
    public static Controllable gameFocus = null;
    boolean shouldMoveUnits = false;
    Point movePoint = new Point();
    public double cursorFrameChange = 1250.0;
    public Cursor[] c = new Cursor[5];
    public Point topLeft, bottomRight;
    public static boolean passedBackInput = false;
    BufferedImage mineralIcon;

    public MainClass() {
        focusables.add(new Barracks(800, 600));
        focusables.add(new CommandCentre(700, 350));
        focusables.add(new Mineral(1200, 225));
        focusables.add(new Mineral(1270, 200));
        focusables.add(new Mineral(1320, 250));
        focusables.add(new Vespene(1380, 550));
        focusables.add(new Vespene(1470, 500));
        focusables.add(new Vespene(1525, 570));
        focusables.add(new SCV(1250, 750));

        try {
            gameMap = new TileMap("res/DefaultMap.txt");
            mineralIcon = ImageIO.read(new File("res/mineralIcon.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setSize(SCREENWIDTH, SCREENHEIGHT);
        this.setVisible(true);
        this.setDoubleBuffered(true);
        this.addMouseListener(this);
        frame = new JFrame("JavaRTS");
        frame.setSize(SCREENWIDTH, SCREENHEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);
        frame.setVisible(true);
        frame.addKeyListener(this);
        try{
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            c[0] = toolkit.createCustomCursor(ImageIO.read(new File("res/cur1.png")), new Point(frame.getX(), frame.getY()), "CF1");
            c[1] = toolkit.createCustomCursor(ImageIO.read(new File("res/cur2.png")), new Point(frame.getX(), frame.getY()), "CF2");
            c[2] = toolkit.createCustomCursor(ImageIO.read(new File("res/cur3.png")), new Point(frame.getX(), frame.getY()), "CF3");
            c[3] = toolkit.createCustomCursor(ImageIO.read(new File("res/cur4.png")), new Point(frame.getX(), frame.getY()), "CF4");
            c[4] = toolkit.createCustomCursor(ImageIO.read(new File("res/cur5.png")), new Point(frame.getX(), frame.getY()), "CF5");
            frame.setCursor(c[0]);
        }catch(IOException e){
                e.printStackTrace();
        }
        frame.add(this);
    }

    public static void main(String[] args){
        MainClass mc = new MainClass();
        double previousTime = System.currentTimeMillis();
        double currentTime;
        double deltaTime;
        double maxDelta = 1000 / 30;
        double minDelta = 1000 / 120;
        while (!mc.quit) {
            currentTime = System.currentTimeMillis();
            deltaTime = currentTime - previousTime;
            previousTime = System.currentTimeMillis();
            while (deltaTime < minDelta) {
                mc.frame.repaint();
                deltaTime += System.currentTimeMillis() - previousTime;
            }
            if (deltaTime <= maxDelta) {
                mc.update(deltaTime);
            } else if (deltaTime > maxDelta) {
                long div = Math.round(deltaTime / maxDelta) + 1;
                for (int i = 0; i < div; i++) {
                    mc.update(deltaTime / (double) div);
                }
            }
            mc.frame.repaint();
        }
        System.exit(0);

    }

    public void update(double pTimeElapsed) {
        if (shouldMoveUnits) {
            shouldMoveUnits = false;
            formUnits.moveToLocation(movePoint);
        }
        for(int i=0;i<focusables.size();i++){
            focusables.get(i).update(pTimeElapsed);
        }

        cursorFrameChange-=pTimeElapsed;
        if(cursorFrameChange<=0){
            if(curCount==4){
                curCount=0;
            }else{
                curCount++;
            }
            frame.setCursor(c[curCount]);
            cursorFrameChange += 1250.0;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.clearRect(0, 0, SCREENWIDTH, SCREENHEIGHT);
        g.setColor(Color.MAGENTA);
        g.fillRect(0, 0, SCREENWIDTH, SCREENHEIGHT);
        gameMap.draw(g);
        for(int i=0;i<focusables.size();i++){
            focusables.get(i).draw(g);
        }
        if (gameFocus != null) {
            gameFocus.drawGUI(g);
        }

        if (mousePressed) {
            g.setColor(Color.BLACK);
            g.drawLine(topLeft.x, topLeft.y, getMousePosition().x, topLeft.y);
            g.drawLine(topLeft.x, topLeft.y, topLeft.x, getMousePosition().y);
            g.drawLine(topLeft.x, getMousePosition().y, getMousePosition().x, getMousePosition().y);
            g.drawLine(getMousePosition().x, topLeft.y, getMousePosition().x, getMousePosition().y);
            g.setColor(new Color(65, 190, 190, 80));
            g.fillRect(topLeft.x, topLeft.y, getMousePosition().x - topLeft.x, getMousePosition().y - topLeft.y);
        }
        g.setColor(Color.WHITE);
        g.setFont(new Font("Times New Roman", 0, 24));
        g.drawImage(mineralIcon.getScaledInstance(32, 32, BufferedImage.SCALE_FAST), 10, 0, null);
        g.drawString("Minerals: " + numMinerals, 50, 25);
        g.drawString("Vespene Gas: " + numVespene, 225, 25);
    }

    public void mouse1Press(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            topLeft = e.getPoint();
            mousePressed = true;
        }
    }

    public void mouse1Release(MouseEvent e) {
        mousePressed = false;
        bottomRight = e.getPoint();
        boolean changeInFocus = false;
        focusedUnits.clear();
        Point tempTL = new Point(topLeft);
        Point tempBR = new Point(bottomRight);
        if (topLeft.x > bottomRight.x) {
            if (bottomRight.y < topLeft.y) {
                topLeft = new Point(tempBR.x, tempBR.y);
                bottomRight = new Point(tempTL.x, tempTL.y);
            } else {
                topLeft = new Point(tempBR.x, tempTL.y);
                bottomRight = new Point(tempTL.x, tempBR.y);
            }
        } else if (bottomRight.y < topLeft.y) {
            topLeft = new Point(tempTL.x, tempBR.y);
            bottomRight = new Point(tempBR.x, tempTL.y);
        }

        for (int i = 0; i < focusables.size(); i++) {
            if (focusables.get(i).getShape().intersects(topLeft.x - focusables.get(i).getXPos(), topLeft.y - focusables.get(i).getYPos(), bottomRight.x - topLeft.x, bottomRight.y - topLeft.y) && focusables.get(i).isUnit()) {
                if (!changeInFocus) {
                    changeInFocus = true;
                }
                focusedUnits.add((Unit) focusables.get(i));
            }
        }

        for (int i = 0; i < focusables.size(); i++) {
            if (focusables.get(i).isInArea(new Point(e.getX() - (int) focusables.get(i).getXPos(), e.getY() - (int) focusables.get(i).getYPos())) && !changeInFocus && focusables.get(i).isUnit()) {
                focusedUnits.clear();
                focusedUnits.add((Unit) focusables.get(i));
                changeInFocus = true;
            }
        }
        formUnits.setUnits(focusedUnits);
        if (!changeInFocus) {
            gameFocus = null;
            for (int i = 0; i < focusables.size(); i++) {
                if (focusables.get(i).isInArea(new Point(e.getX() - (int) focusables.get(i).getXPos(), e.getY() - (int) focusables.get(i).getYPos())) && !changeInFocus && !focusables.get(i).isUnit()) {
                    gameFocus = focusables.get(i);
                }
            }
        } else {
            gameFocus = formUnits;
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_SHIFT){
            for (int i = 0; i < focusedUnits.size(); i++) {
                focusedUnits.get(i).passInKeyboardPressed(e);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            quit = true;
        }else if(e.getKeyCode() == KeyEvent.VK_SHIFT){
            for (int i = 0; i < focusedUnits.size(); i++) {
                focusedUnits.get(i).passInKeyboardReleased(e);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (gameFocus == null) {
            mouse1Press(e);
        } else {
            gameFocus.passInMousePressedEvent(e);
            if (passedBackInput) {
                mouse1Press(e);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (gameFocus == null) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                shouldMoveUnits = true;
                movePoint = e.getPoint();
            } else if (e.getButton() == MouseEvent.BUTTON1) {
                mouse1Release(e);
            }
        } else {
            gameFocus.passInMouseReleasedEvent(e);
            if (passedBackInput) {
                passedBackInput = false;
                mouse1Release(e);
            }
        }

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
