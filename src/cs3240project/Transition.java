package cs3240project;

/**
 * Represents transitions for finite automata
 */

public class Transition {
    private Character transChar;
    private Integer next;

    /**
     * Create a new transition
     * @param transChar transition character
     * @param next next state id
     */
    public Transition(char transChar, int next) {
        this.transChar = transChar;
        this.next = next;
    }

    /**
     * Get the transition character
     * @return transition character
     */

    public char transChar() {
        return this.transChar;
    }

    /**
     * Get the next state ID
     * @return next state ID
     */

    public int next() {
        return this.next;
    }

    /**
     * Set the transition character
     * @param transChar transition character
     */

    public void setTransChar(char transChar) {
        this.transChar = transChar;
    }

    /**
     * Set the next state ID
     * @param next next state ID
     */

    public void setNext(int next) {
        this.next = next;
    }
}
