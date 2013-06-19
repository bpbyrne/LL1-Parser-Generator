package cs3240project;

import org.junit.*;
import junit.framework.TestCase;
import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;

public class GrammarTest extends TestCase {

    public void parserCheckOutput(String inputFile, String outputFile, String verifyFile) {
        String[] args = new String[2];

        args[0] = inputFile;
        args[1] = outputFile;
        
        try {
            GrammarDriver.main(args);

            String verification, output;
            BufferedReader vr = new BufferedReader(new FileReader(verifyFile));
            BufferedReader or = new BufferedReader(new FileReader(outputFile));
            
            while ((verification = vr.readLine()) != null) {
                output = or.readLine();
                assertEquals(verification, output);
            }

            assertNull(or.readLine());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testFullGrammar1() {
        String inputFile = "tests/GrammarTest1Input";
        String outputFile = "tests/GrammarTest1Output";
        String verifyFile = "tests/GrammarTest1Verify";

        parserCheckOutput(inputFile, outputFile, verifyFile);
    }

}
