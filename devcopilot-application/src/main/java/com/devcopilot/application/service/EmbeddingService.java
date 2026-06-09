package com.devcopilot.application.service;

import java.util.Arrays;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {

    private static final int DIMENSIONS = 64;

    public String embed(String text) {
        double[] vector = new double[DIMENSIONS];
        if (text != null && !text.isBlank()) {
            Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9\\u4e00-\\u9fa5]+"))
                    .filter(token -> !token.isBlank())
                    .forEach(token -> vector[Math.floorMod(token.hashCode(), DIMENSIONS)] += 1.0);
        }
        normalize(vector);
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(String.format(Locale.ROOT, "%.6f", vector[i]));
        }
        return builder.append(']').toString();
    }

    private void normalize(double[] vector) {
        double sum = 0.0;
        for (double value : vector) {
            sum += value * value;
        }
        if (sum == 0.0) {
            vector[0] = 1.0;
            return;
        }
        double norm = Math.sqrt(sum);
        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] / norm;
        }
    }
}
