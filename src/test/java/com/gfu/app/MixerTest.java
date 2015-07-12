package com.gfu.app;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MixerTest {

    @Test
    public void mixToStereoTest() {
        double[] left = new double[] { 0, 2, 4, 6 };
        double[] right = new double[] { 1, 3, 5, 7 };

        double[] result = Mixer.mixToStereo(left, right);

        for (int i = 0; i < 8; i++) {
            assertEquals(i, result[i], 0.0);
        }
    }
}
