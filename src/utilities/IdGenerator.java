package utilities;

public class IdGenerator {
    private static int counter = 0;

    /**
     * This method generates a unique ID by incrementing a counter each time it's called.
     * The counter starts at 0 and is incremented by 1 for each new ID generated.
     *
     * @return int - the next unique ID
     */
    public static int generateId(){
        counter++;
        return counter;
    }
}
