package com.apiit.ludot;

import org.junit.jupiter.api.Assertions;

public class TestAssertions {

    private TestAssertions() {

    }

    public static void assertThat(boolean condition, String message) {
        Assertions.assertTrue(condition, message);
    }

    public static void assertNotNull(Object value, String message) {
        Assertions.assertNotNull(value, message);
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        Assertions.assertEquals(expected, actual, message);
    }

    public static void assertNotEquals(Object unexpected, Object actual, String message) {
        Assertions.assertNotEquals(unexpected, actual, message);
    }
}
