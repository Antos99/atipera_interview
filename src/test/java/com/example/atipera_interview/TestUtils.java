package com.example.atipera_interview;

import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class TestUtils {

    public static String getResourceAsString(String name) {
        return new String(getResourceAsByteArray(name), StandardCharsets.UTF_8);
    }

    public static byte[] getResourceAsByteArray(String name) {
        try {
            return Objects.requireNonNull(TestUtils.class.getResourceAsStream(name)).readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
