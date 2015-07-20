package com.gfu.app;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Font;
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
        setSize(1400, 800);
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);

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

        addMouseListener(this);
        addMouseMotionListener( this );
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
        frameReg.start();
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

    int yLineMin = 199, yLineMax = 599;
    int widthVol = 20, heightVol = 40;
    int xVol = 0, yVol = 0;
    int mxStartDelta = 0; int myStartDelta = 0;
    boolean isMouseDraggingVolume = false;

    // repaint() schedules the AWT thread to call update()
    public void update(Graphics g) {
        g.drawImage(backBuffer, 0, 0, this);
        getToolkit().sync();
    }

    @Override
    public void run() {
        frameReg.start();
        xVol = getWidth() / 2 - widthVol / 2;
        yVol = yLineMin;

        setupBackBuffer();
        updateVolumeSlider();
        while (true) {
            try {
                frameReg.waitForNextFrame();
                repaint();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupBackBuffer() {
        // wipe the image
        backGraphics.setColor(Color.BLACK);
        backGraphics.fillRect(0, 0, getWidth(), getHeight());

        // volume text
        backGraphics.setColor(Color.WHITE);
        backGraphics.setFont(new Font("Custom", Font.BOLD, 20));
        backGraphics.drawString("Volume", 667, 180);

        // volume line
        backGraphics.setColor(Color.WHITE);
        backGraphics.drawLine(getWidth() / 2, yLineMin, getWidth() / 2, yLineMax);
    }

    private void updateVolumeSlider() {
        backGraphics.fillRect(xVol, yVol, widthVol, heightVol);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        if (mx >= xVol && mx < xVol + widthVol) {
            if (my >= yVol && my < yVol + heightVol) {
                isMouseDraggingVolume = true;
                myStartDelta = my - yVol;
            }
        }
        e.consume();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        isMouseDraggingVolume = false;
        myStartDelta = 0;
        e.consume();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (isMouseDraggingVolume) {
            int mx = e.getX();
            int my = e.getY();
            int yVolCandidate = my - myStartDelta; // safer to use candidate, otherwise graphics may get wonky

            // if outside of slider bounds, set to min/max
            if (my <= yLineMin - myStartDelta) yVolCandidate = yLineMin;
            else if (my >= yLineMax - myStartDelta) yVolCandidate = yLineMax - heightVol;

            if (yVolCandidate >= yLineMin && yVolCandidate <= yLineMax - heightVol) {
                yVol = yVolCandidate;

                float gain = 1.0f - (float) (yVol - yLineMin) / ((yLineMax - heightVol) - yLineMin);
                player.setMasterGain(gain);

                setupBackBuffer();
                updateVolumeSlider();
                repaint();
            }
        }
        e.consume();
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
