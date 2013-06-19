package cs3240project;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

/**
 * Main method for Grammar parser (part 2)
 */

public class GrammarDriver {
    private static class DriverError extends Exception {
        public DriverError(String message) {
            super(message);
        }
    }

    /**
     * Grammar Parser starts here
     * @param args list of arguments from command line
     */

    public static void main(String[] args) throws DriverError {
        if (args.length != 2) {
            Logger.debug("usage: java -jar grammar.jar <grammar file> <output file>");
            throw new DriverError("usage error");
        }

        GrammarParser parser = null;

        try {
            File grammarFile = new File(args[0]);
            FileWriter outputFile = new FileWriter(new File(args[1]));

            parser = new GrammarParser();
            System.out.println("Writing Visual Parse Table to " + args[1]);
            outputFile.write("" + parser.buildTable(grammarFile));

            outputFile.close();
        }
        catch(Exception e) {
            throw new DriverError(e.getMessage());
        }
    }
}
