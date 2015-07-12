package com.gfu.app;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AppletMain extends Applet implements Runnable, MouseListener, MouseMotionListener {

    Image backBuffer;
    Graphics backGraphics;

    AudioFile audioFile;
    Player player;
    Mixer mixer;
    Thread mainThread, playerThread, mixerThread;
    FrameRegulator frameReg;
    private String filepath = "nara_16.wav";

    public void init() {
        setSize(1280, 720);

        backBuffer = createImage(getWidth(), getHeight());
        backGraphics = backBuffer.getGraphics();

        initRenderHints(backGraphics);

        frameReg = new FrameRegulator();

        try {
            audioFile = new AudioFile(filepath);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        player = new Player(audioFile.getBaseFormat());
        try {
            mixer = new Mixer(player, audioFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (mainThread == null) {
            mainThread = new Thread(this);
            mainThread.start();
        }
        if (playerThread == null) {
            playerThread = new Thread(player);
            playerThread.start();
        }
        if (mixerThread == null) {
            mixerThread = new Thread(mixer);
            mixerThread.start();
        }
    }

    private void initRenderHints(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        Map<RenderingHints.Key, Object> renderingHintsMap = new HashMap<>();
        renderingHintsMap.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderingHintsMap.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        renderingHintsMap.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        renderingHintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        renderingHintsMap.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        renderingHintsMap.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2.setRenderingHints(renderingHintsMap);
    }

    public void update(Graphics g) {
        g.drawImage(backBuffer, 0, 0, this);
        getToolkit().sync();
    }

    public void paint(Graphics g) {
        update(g);
    }

    @Override
    public void run() {
        frameReg.start();

        while (true) {
            try {
                backGraphics.setColor(Color.BLACK);
                backGraphics.fillRect(0, 0, getWidth(), getHeight());
                frameReg.waitForNextFrame();
                repaint();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
