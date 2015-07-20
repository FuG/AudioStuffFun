package com.gfu.app;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class Main {
    static String filepath = "nara_16.wav";

    public static final AudioFormat.Encoding DEFAULT_AUDIO_ENCODING = AudioFormat.Encoding.PCM_SIGNED;
    public static final float DEFAULT_SAMPLE_RATE = 44100;
    public static final int DEFAULT_SAMPLE_SIZE_IN_BITS = 16;
    public static final int DEFAULT_CHANNELS = 1;
    public static final int DEFAULT_FRAME_SIZE = 2;
    public static final float DEFAULT_FRAME_RATE = 44100;
    public static final boolean DEFAULT_BIG_ENDIAN = false;
    public static final AudioFormat DEFAULT_FORMAT =
            new AudioFormat(
                    DEFAULT_AUDIO_ENCODING,
                    DEFAULT_SAMPLE_RATE,
                    DEFAULT_SAMPLE_SIZE_IN_BITS,
                    DEFAULT_CHANNELS,
                    DEFAULT_FRAME_SIZE,
                    DEFAULT_FRAME_RATE,
                    DEFAULT_BIG_ENDIAN
            );

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
        sandboxTest();
    }

    // offset = 648 @ 5m
    public static void sandboxTest() {
        double ratio = 0.999;
        int steps = 1000000;
        double sum = 0;
        double runningSum = 1;

        for (int i = 0; i < steps; i++) {
            runningSum *= ratio;
            sum += runningSum;
        }
        System.out.println("Sum: " + sum);

        if (true) System.exit(1);

        try {
            AudioFile audioFile = new AudioFile(filepath);

            Player player = new Player(DEFAULT_FORMAT);
            Thread t = new Thread(player);
            t.start();

            byte[] data = audioFile.getRawBufferLeft();

            int bufferSize = 4410;
//            int speakerDistanceOffset = 0;
            int speakerDistanceOffset = 648;
            double volumeFactor = 0.95;
            byte[] buffer = new byte[bufferSize];
            double[] carryover = new double[bufferSize];
            for (int i = 0; i < data.length; i++) {
                if (i % bufferSize == 0) {
                    byte[] mixed = Utility.doublesToBytes(Mixer.mix(Utility.bytesToDoubles(data, 2, true), carryover, speakerDistanceOffset, 0.8), 2, true);
                    player.enqueue(mixed);
                    buffer = new byte[bufferSize];
//                    Thread.sleep(40);
                    System.out.println("Enqueued: " + i);
                }

                buffer[i % bufferSize] = data[i];
            }
            System.out.println("Data: Fully Loaded!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
