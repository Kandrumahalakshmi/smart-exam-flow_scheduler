
package com.example.demo.service;

public final class SeedConfiguration {

    private SeedConfiguration() {}

    public static final long DEFAULT_SEED = 20261022L;

    public record SeedProfile(
            int students,
            int courses,
            int enrollments,
            int exams,
            int rooms,
            int timeSlots,
            int invigilators,
            int accommodations
    ) {}

    public static SeedProfile getProfile(String profile) {
        if ("small".equalsIgnoreCase(profile)) {
            return new SeedProfile(
                    20, 5, 40, 10, 5, 4, 10, 4
            );
        }

        if ("full".equalsIgnoreCase(profile)) {
            return new SeedProfile(
                    5000, 300, 10000, 300,
                    100, 20, 200, 250
            );
        }

        throw new IllegalArgumentException(
                "Unknown profile. Use 'small' or 'full'."
        );
    }
}
