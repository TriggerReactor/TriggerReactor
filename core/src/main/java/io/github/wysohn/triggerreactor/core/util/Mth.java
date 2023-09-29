package io.github.wysohn.triggerreactor.core.util;

public final class Mth {

    /**
     * Returns {@code value} clamped to the inclusive range of {@code min} and {@code max}.
     *
     * @param value the value to be clamped
     * @param min the lower bound of the result
     * @param max the upper bound of the result
     * @return {@code value} if {@code min} <= {@code value} <= {@code max},
     *          {@code min} if {@code value} < {@code min},
     *          or {@code max} if {@code max} < {@code value}
     */
    public static int clamp(final int value, final int min, final int max) {
        return Math.max(Math.min(value, max), min);
    }


    /**
     * Returns {@code value} clamped to the inclusive range of {@code min} and {@code max}.
     *
     * @param value the value to be clamped
     * @param min the lower bound of the result
     * @param max the upper bound of the result
     * @return {@code value} if {@code min} < {@code value} < {@code max},
     *          {@code min} if {@code value} < {@code min},
     *          or {@code max} if {@code max} < {@code value}
     */
    public static long clamp(final long value, final long min, final long max) {
        return Math.max(Math.min(value, max), min);
    }


    /**
     * Returns {@code value} clamped to the inclusive range of {@code min} and {@code max}.
     *
     * @param value the value to be clamped
     * @param min the lower bound of the result
     * @param max the upper bound of the result
     * @return {@code value} if {@code min} < {@code value} < {@code max},
     *          {@code min} if {@code value} < {@code min},
     *          or {@code max} if {@code max} < {@code value}
     */
    public static double clamp(final double value, final double min, final double max) {
        return Math.max(Math.min(value, max), min);
    }


    /**
     * Returns {@code value} clamped to the inclusive range of {@code min} and {@code max}.
     *
     * @param value the value to be clamped
     * @param min the lower bound of the result
     * @param max the upper bound of the result
     * @return {@code value} if {@code min} < {@code value} < {@code max},
     *          {@code min} if {@code value} < {@code min},
     *          or {@code max} if {@code max} < {@code value}
     */
    public static float clamp(final float value, final float min, final float max) {
        return Math.max(Math.min(value, max), min);
    }

    private Mth() {
        throw new UnsupportedOperationException();
    }

}