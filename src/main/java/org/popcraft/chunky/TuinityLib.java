package org.popcraft.chunky;

public class TuinityLib {

    private static boolean isTuinity;
    private static int delayChunkUnloadsBy;

    static {
        @SuppressWarnings("rawtypes")
        Class tuinityConfig = null;
        try {
            tuinityConfig = Class.forName("com.tuinity.tuinity.config.TuinityConfig");
            isTuinity = true;
        } catch (ClassNotFoundException ignored) {
        }
        try {
            if (isTuinity && tuinityConfig != null) {
                delayChunkUnloadsBy = tuinityConfig.getDeclaredField("delayChunkUnloadsBy").getInt(null);
            }
        } catch (IllegalAccessException | NoSuchFieldException ignored) {
        }
    }

    public static boolean isTuinity() {
        return isTuinity;
    }

    public static int getDelayChunkUnloadsBy() {
        return delayChunkUnloadsBy;
    }

}
