package cs3240project;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

/**
 * Walk through input and consult DFA to generate classes with instances
 */

public class TableWalker {
    private DFATable dfa;
    private Scanner scanner;
    private String specialChar = "`~!@#$%^&*()+={}[]:;',/?\\";

    /**
     * Create a new table walker
     * @param dfa DFATable to consult
     * @param scanner Scanner to get data from
     */

    public TableWalker(DFATable dfa, Scanner scanner) {
        this.dfa = dfa;
        this.scanner = scanner;
    }

    /**
     * Read and execute a character from source
     * @param nextWalkChar character to execute
     */

    public void read(char nextWalkChar) {
        dfa.execute(nextWalkChar);
    }

    /**
     * Walk over tokens and find classes. Output string of results
     * @return string of results from walking
     */

    public String walk() {
        String result = "";

        while (this.scanner.hasNext()) {
            String instance = scanner.next();

            for (int i = 0; i < instance.length(); i++) {
                if ((specialChar.indexOf(instance.charAt(i)) >= 0) && i != 0) {
                    result += getClassificationString(instance.substring(0, i)) + "\n";
                    resetExecution();
                    read(instance.charAt(i));
                    result += getClassificationString(String.valueOf(instance.charAt(i))) + "\n";
                    resetExecution();
                    instance = instance.substring(i+1);
                    i = 0;
                }
                read(instance.charAt(i));
            }

            if (dfa.getCurrentID().equals("error")) {
                result += String.format("%s %s", instance, instance) + "\n";
            }
            else {
                result += getClassificationString(instance) + "\n";
            }
            resetExecution();
        }

        return result;
    }

    /**
     * Get the current classification, given a specific instance
     * @param instance literal instance in source
     * @return classification
     */

    public String getClassification(String instance) {
        String result;

        for (int i = 0; i < instance.length(); i++) {
            read(instance.charAt(i));
        }
            
        result = dfa.getCurrentID();
        resetExecution();

        return result;
    }

    /**
     * Get the current classification, given a specific instance
     * @param instance literal instance in source
     * @return classification + literal instance (for output)
     */

    private String getClassificationString(String instance) {
        return String.format("%s %s", dfa.getCurrentID(), instance);
    }

    /**
     * Reset the DFA
     */

    public void resetExecution() {
        this.dfa.resetExecution();
    }
}
