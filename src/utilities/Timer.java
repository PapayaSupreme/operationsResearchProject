package utilities;

public class Timer {
    public static void runTimer(String action, long t0, long t1){
        long t = t1 - t0;
        System.out.println("\n" + MenuHelper.ConsoleColors.ANSI_PURPLE + "TIMER: " + MenuHelper.ConsoleColors.ANSI_RESET
                + action+ " done in " + (t / 1000000.0) + " ms.\n");
    }
}
