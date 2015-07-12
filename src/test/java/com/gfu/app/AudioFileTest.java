package com.gfu.app;

import com.gfu.app.AudioFile;
import org.junit.Test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class AudioFileTest {
    public static final AudioFormat.Encoding DEFAULT_AUDIO_ENCODING = AudioFormat.Encoding.PCM_SIGNED;
    public static final float DEFAULT_SAMPLE_RATE = 44100;
    public static final int DEFAULT_SAMPLE_SIZE_IN_BITS = 16;
    public static final int DEFAULT_CHANNELS = 2;
    public static final int DEFAULT_FRAME_SIZE = 4;
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

    public static final int DEFAULT_RAW_STEREO_BUFFER_SIZE = 128;

    @Test
    public void leftChannelLoadsCorrectly() throws IOException, UnsupportedAudioFileException {
        // setup
        byte[] testData = new byte[DEFAULT_RAW_STEREO_BUFFER_SIZE];

        for (int i = 0; i < DEFAULT_RAW_STEREO_BUFFER_SIZE; i++) {
            testData[i] = (byte) i;
        }

        // act
        AudioFile audioFile = new AudioFile();
        audioFile.setBaseFormat(DEFAULT_FORMAT);
        audioFile.setRawStereoBuffer(testData);

        byte[] leftChannel = audioFile.left.getRawBufferInstance();

        // verify
        int sampleSizeInBytes = DEFAULT_SAMPLE_SIZE_IN_BITS / 8;
        assertEquals(DEFAULT_RAW_STEREO_BUFFER_SIZE / DEFAULT_CHANNELS, leftChannel.length);
        for (int i = 0; i < DEFAULT_RAW_STEREO_BUFFER_SIZE / DEFAULT_CHANNELS; i++) {
            if (i % sampleSizeInBytes == 0) {
                assertEquals(testData[i * sampleSizeInBytes], leftChannel[i]);
                assertEquals(testData[i * sampleSizeInBytes + 1], leftChannel[i + 1]);
            }
        }
    }

    @Test
    public void rightChannelLoadsCorrectly() throws IOException, UnsupportedAudioFileException {
        // setup
        byte[] testData = new byte[DEFAULT_RAW_STEREO_BUFFER_SIZE];

        for (int i = 0; i < DEFAULT_RAW_STEREO_BUFFER_SIZE; i++) {
            testData[i] = (byte) i;
        }

        // act
        AudioFile audioFile = new AudioFile();
        audioFile.setBaseFormat(DEFAULT_FORMAT);
        audioFile.setRawStereoBuffer(testData);

        byte[] leftChannel = audioFile.right.getRawBufferInstance();

        // verify
        int sampleSizeInBytes = DEFAULT_SAMPLE_SIZE_IN_BITS / 8;
        assertEquals(DEFAULT_RAW_STEREO_BUFFER_SIZE / DEFAULT_CHANNELS, leftChannel.length);
        for (int i = 0; i < DEFAULT_RAW_STEREO_BUFFER_SIZE / DEFAULT_CHANNELS; i++) {
            if (i % sampleSizeInBytes == 0) {
                assertEquals(testData[i * sampleSizeInBytes + 2], leftChannel[i]);
                assertEquals(testData[i * sampleSizeInBytes + 3], leftChannel[i + 1]);
            }
        }
    }

    // TODO: Add tests for multi-channel support
}
