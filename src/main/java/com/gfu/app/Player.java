package com.gfu.app;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.List;
import java.util.Vector;

public class Player implements Runnable {
    Vector<byte[]> rawQueue;
    public byte[] rawBuffer; // use alternating buffers

    AudioFormat audioFormat;

    public Player(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
        rawQueue = new Vector<>();
    }

    @Override
    public void run() {
        try {
            if (rawBuffer != null) {
                playFromBuffer();
            } else {
                playFromQueue();
            }
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private synchronized void playFromBuffer() throws LineUnavailableException {
        SourceDataLine line = getSourceLine();

        if (line != null) {
            line.start();

            line.write(rawBuffer, 0, rawBuffer.length);

            line.drain();
            line.stop();
            line.close();
        }
    }

    private synchronized void playFromQueue() throws LineUnavailableException {
        SourceDataLine line = getSourceLine();

        if (line != null) {
            line.start();

            while (true) {
                if (rawQueue.size() == 0) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                byte[] buffer = rawQueue.remove(0);

                line.write(buffer, 0, buffer.length);
            }
        }
    }

    public synchronized void enqueue(byte[] buffer) {
        rawQueue.add(buffer);
        System.out.println("Enqueued");
        notify();
    }

    private SourceDataLine getSourceLine() throws LineUnavailableException {
        SourceDataLine line;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(audioFormat);
        return line;
    }
}
