package cs3240project; 

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Main method for the Spec Parser (part 1)
 */

public class Driver {
    private static class DriverError extends Exception {
        public DriverError(String message) {
            super(message);
        }
    }

    /**
     * Spec Parser starts here
     * @param args list of arguments from command line
     */

    public static void main(String[] args) throws DriverError {
        if (args.length != 3) {
            Logger.debug("usage: java -jar parser.jar <spec file> <input file> <output file>");
            throw new DriverError("usage error");
        }

        try {
            File specFile = new File(args[0]);
            File inputFile = new File(args[1]);
            FileWriter outputFile = new FileWriter(new File(args[2]));

            Scanner scanner = new Scanner(inputFile);

            NFA grammarNFA = new NFA();
            List<NFA> identifierNFAs = new ArrayList<NFA>();
            SpecScanner sScanner = new SpecScanner(specFile);

            while (sScanner.hasNext()) {
                Token token = sScanner.next();
                if (token.getType().equals("newline")) {
                    continue;
                }
                SpecParser specParser = new SpecParser(sScanner);
                specParser.useDefs(identifierNFAs);
                specParser.parseSpec();
                NFA identifierNFA = specParser.getDerivation();

                identifierNFA.setName(token.getInstance());
                identifierNFAs.add(identifierNFA);

                grammarNFA.link(identifierNFA);
                    
                if (specParser.isTokenDefinition()) {
                    /* all of the definitions need to begin checking from the start */
                    grammarNFA.linkStart();
                }
            }
		
            DFATable dfa = new DFATable();
            dfa.importNFA(grammarNFA);

            TableWalker walker = new TableWalker(dfa, scanner);
            System.out.println("Writing tokenized input to " + args[2]);
            outputFile.write(walker.walk());

            outputFile.close();
        } catch (Exception e) {
            throw new DriverError(e.getMessage());
        } 
    }
}
