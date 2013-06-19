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

public class ParserTest extends TestCase {

    public void parserCheckOutput(String inputFile, String outputFile, String specFile, String verifyFile) {
        String[] args = new String[3];

        args[0] = specFile;
        args[1] = inputFile;
        args[2] = outputFile;
        
        try {
            Driver.main(args);

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

    // our custom tests have changed
    /*
    public void testFullParser1() {
        String inputFile = "tests/ParserTest1Input";
        String outputFile = "tests/ParserTest1Output";
        String specFile = "tests/ParserTest1Spec";
        String verifyFile = "tests/ParserTest1Verify";

        parserCheckOutput(inputFile, outputFile, specFile, verifyFile);
    }
    public void testFullParser2() {
        String inputFile = "tests/ParserTest2Input";
        String outputFile = "tests/ParserTest2Output";
        String specFile = "tests/ParserTest2Spec";
        String verifyFile = "tests/ParserTest2Verify";

        parserCheckOutput(inputFile, outputFile, specFile, verifyFile);
    }
    */

    public void testFullParser3() {
        String inputFile = "tests/ParserTest3Input";
        String outputFile = "tests/ParserTest3Output";
        String specFile = "tests/ParserTest3Spec";
        String verifyFile = "tests/ParserTest3Verify";

        parserCheckOutput(inputFile, outputFile, specFile, verifyFile);
    }
    public void testFullParser4() {
        String inputFile = "tests/ParserTest4Input";
        String outputFile = "tests/ParserTest4Output";
        String specFile = "tests/ParserTest4Spec";
        String verifyFile = "tests/ParserTest4Verify";

        parserCheckOutput(inputFile, outputFile, specFile, verifyFile);
    }
    public void testFullParser5() {
        String inputFile = "tests/ParserTest5Input";
        String outputFile = "tests/ParserTest5Output";
        String specFile = "tests/ParserTest5Spec";
        String verifyFile = "tests/ParserTest5Verify";

        parserCheckOutput(inputFile, outputFile, specFile, verifyFile);
    }
    public void testFullParser6() {
        String inputFile = "tests/ParserTest6Input";
        String outputFile = "tests/ParserTest6Output";
        String specFile = "tests/ParserTest6Spec";
        String verifyFile = "tests/ParserTest6Verify";

        parserCheckOutput(inputFile, outputFile, specFile, verifyFile);
    }
    public void testFullParser7() {
        String inputFile = "tests/ParserTest7Input";
        String outputFile = "tests/ParserTest7Output";
        String specFile = "tests/ParserTest7Spec";
        String verifyFile = "tests/ParserTest7Verify";

        parserCheckOutput(inputFile, outputFile, specFile, verifyFile);
    }
    public void testFullParser8() {
        String inputFile = "tests/ParserTest8Input";
        String outputFile = "tests/ParserTest8Output";
        String specFile = "tests/ParserTest8Spec";
        String verifyFile = "tests/ParserTest8Verify";

        parserCheckOutput(inputFile, outputFile, specFile, verifyFile);
    }

    /*
    public void testFullParser9() {
        String inputFile = "tests/ParserTest9Input";
        String outputFile = "tests/ParserTest9Output";
        String specFile = "tests/ParserTest9Spec";
        String verifyFile = "tests/ParserTest9Verify";

        parserCheckOutput(inputFile, outputFile, specFile, verifyFile);
    }
    */
}
