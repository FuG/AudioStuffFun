package com.gfu.app;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class Mixer implements Runnable {
    FrameRegulator frameRegulator;
    Player player;
    AudioFile audioFile;
    double[] leftBuffer, rightBuffer;
    double[] normalStereoBuffer;

    public boolean paused;

    public Mixer(Player player, AudioFile audioFile) throws IOException, UnsupportedAudioFileException {
        frameRegulator = new FrameRegulator(10);
        this.player = player;
        this.audioFile = audioFile;
        normalStereoBuffer = audioFile.getNormalBufferInstance();
        leftBuffer = audioFile.left.getNormalBufferInstance();
        rightBuffer = audioFile.right.getNormalBufferInstance();
        paused = false;
    }

    @Override
    public void run() {
        int bufferSize = 4410;
//        int speakerDistanceOffset = 0;
        int speakerDistanceOffset = (int) (648 * 1.5);
        double volumeFactor = 0.7;
        double[] leftMixInput = new double[bufferSize];
        double[] rightMixInput = new double[bufferSize];
        double[] leftOverflow = new double[bufferSize];
        double[] rightOverflow = new double[bufferSize];
        for (int i = 0; i < leftBuffer.length; i++) {
            if (i % bufferSize == 0) {
                double[] leftMix = mix(leftMixInput, leftOverflow, speakerDistanceOffset, volumeFactor);
                double[] rightMix = mix(rightMixInput, rightOverflow, speakerDistanceOffset, volumeFactor);
                double[] stereoMix = mixToStereo(leftMix, rightMix);
                byte[] stereoMixBytes = Utility.doublesToBytes(stereoMix, 2, true);
                if (paused) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                player.enqueue(stereoMixBytes);
                leftMixInput = new double[bufferSize];
                rightMixInput = new double[bufferSize];
                System.out.println("Enqueued: " + i);
            }

            leftMixInput[i % bufferSize] = leftBuffer[i];
            rightMixInput[i % bufferSize] = rightBuffer[i];
        }
        System.out.println("Data: Fully Loaded!");
    }

    public synchronized void togglePause() {
        if (paused) {
            paused = false;
            notify();
        } else {
            paused = true;
        }
    }

    public static double[] mixToStereo(double[] left, double[] right) {
        int length = left.length + right.length;
        double[] result = new double[length];

        for (int i = 0; i < length / 2; i++) {
            int resultIndex = i * 2;
            result[resultIndex] = left[i];
            result[resultIndex + 1] = right[i];
        }

        return result;
    }

    public static double[] mix(double[] primary, double[] carryover, int offset, double volumeFactor) {
        double[] result = new double[primary.length];
        for (int i = 0; i < offset; i++) {
            result[i] = mix(primary[i], carryover[i], volumeFactor);
            carryover[i] = primary[primary.length - 1 - offset - i];
        }

        for (int i = 0; i < primary.length - offset; i++) {
            result[i + offset] = mix(primary[i + offset], primary[i], volumeFactor);
        }

        return result;
    }

    private static double mix(double primary, double secondary, double volumeFactor) {
        return (primary + secondary * volumeFactor) / 2;
    }

    public static double[] reverb(double[] primary, double[] secondary, int offset, double volumeFactor) {
        // need to use a queue for carryover
        return null;
    }
}
