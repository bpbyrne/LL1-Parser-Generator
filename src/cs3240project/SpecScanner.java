package cs3240project;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class SpecScanner {
    private Scanner specReader;
    private SpecToken bufferTok;
    private String buffer;
    private int pos;
    private boolean oracling;
    private int calls;

    public SpecScanner(File specFile) throws FileNotFoundException {
        this.bufferTok = null;
        this.pos = -1;
        this.specReader = new Scanner(specFile);
        this.buffer = specReader.nextLine();
        this.oracling = false;
        this.calls = 0;
    }

    public SpecScanner(String specString) {
        this.bufferTok = null;
        this.pos = -1;
        this.specReader = new Scanner(specString);
        this.buffer = specReader.nextLine();
        this.oracling = false;
        this.calls = 0;
    }

    private String nextChar() {
        if (this.pos >= buffer.length() - 1) {
            return "\n";
        }

        this.pos += 1;
        return String.valueOf(this.buffer.charAt(pos));
    }
	
    private String lookAheadChar(int offset) {
        if (this.pos >= buffer.length() - 1) {
            return "\n";
        }

        return String.valueOf(buffer.charAt(pos + offset));
    }

    private boolean hasNextLineChar() {
        return this.specReader.hasNextLine();
    }
	
    private String nextLineChar() {
        this.pos = -1;
        this.buffer = specReader.nextLine();
        return this.buffer;
    }

    public boolean hasNext() {
        if ((this.calls++) == 0) {
            return true;
        }

        oracling = false;
        if (hasNextLineChar()) {
            nextLineChar();
            return true;
        }

        return false;
    }

    public SpecToken next() {
        if (oracling) {
            oracling = false;
            return bufferTok;
        }

        String n = nextChar();

        if (n.equals("%")) {
            if (this.pos == 0 && lookAheadChar(1).equals("%")) {
                if (hasNextLineChar()) {
                    nextLineChar();
                }
                return next();
            }
            return new SpecToken("literal", "%");
        }
        else if (n.equals("\n") || n.equals("\r")) {
            // Normalize newlines to unix newlines
            return new SpecToken("newline", "\n");
        }
        else if (n.equals("\t") || n.equals(" ")) {
            return next();
        }
        else if (n.equals("$")) {
            String definition = "";

            char v = lookAheadChar(1).charAt(0);
            while ((v >= 48 && v < 58) || (v >= 65 && v < 91) ||
                   (v >= 97 && v < 123) || v == 45) {
                definition += nextChar();
                v = lookAheadChar(1).charAt(0);
            }
            return new SpecToken("_" + definition, definition);
        }
        else if (n.equals("[")) {
            return new SpecToken("lbracket", "[");
        }
        else if (n.equals("]")) {
            return new SpecToken("rbracket", "]");
        }
        else if (n.equals("I")) {
            if (lookAheadChar(1).equals("N")) {
                nextChar();
                return new SpecToken("in", "IN");
            }
            else {
                return new SpecToken("literal", "I");
            }
        }
        else if (n.equals("\\")) {
            return new SpecToken("literal", "\\" + nextChar());
        }
        else if (n.equals("|")) {
            return new SpecToken("union", "|");
        }
        else if (n.equals("*")) {
            return new SpecToken("star", "*");
        }
        else if (n.equals("+")) {
            return new SpecToken("plus", "+");
        }
        else if (n.equals("^")) {
            return new SpecToken("caret", "^");
        }
        else if (n.equals("(")) {
            return new SpecToken("lparenthesis", "(");
        }
        else if (n.equals(")")) {
            return new SpecToken("rparenthesis", ")");
        }
        else if (n.equals(".")) {
            return new SpecToken("dot", ".");
        }
        return new SpecToken("literal", n);
    }
	
    public SpecToken lookAhead() {
        if (oracling) {
            return bufferTok;
        }

        bufferTok = this.next();
        oracling = true;
        return bufferTok;
    }
}
