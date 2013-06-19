package cs3240project;

/**
 * Logging convenience functions
 */

public class Logger {

    /**
     * Normal output
     * @param message message displayed to user
     */

    public static void debug(String message) {
        System.out.println(String.format("[debug] %s ...", message));
    }

    /**
     * Output on error
     * @param message message displayed to user
     */

    public static void error(String message) {
        System.err.println(String.format("[error] %s", message));
    }
}
