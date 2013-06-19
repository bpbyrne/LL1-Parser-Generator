package cs3240project;

import java.io.File;
import java.util.Scanner;
import java.io.FileNotFoundException;

/**
 * Scanner that generates tokens from a grammar file
 */

public class GrammarScanner {

    private Scanner grammarReader;
    private Token bufferTok;
    private boolean oracling;
    private String buffer;
    private int pos;

    /**
     * Create scanner from a grammar file
     * @param grammarFile grammar file to use
     */

    public GrammarScanner(File grammarFile) throws FileNotFoundException {
        this.bufferTok = null;
        this.grammarReader = new Scanner(grammarFile);
        this.buffer = grammarReader.nextLine();
        this.oracling = false;
        this.pos = -1;
    }

    /**
     * Create scanner from a grammar file
     * @param grammarFile grammar file to use
     */
	
    public GrammarScanner(String grammarFile) throws FileNotFoundException {
        this.bufferTok = null;
        this.grammarReader = new Scanner(new File(grammarFile));
        this.buffer = grammarReader.nextLine();
        this.oracling = false;
        this.pos = -1;
    }

    /**
     * Grab the next token from the file source
     * @return next token
     */

    public Token next() {
        if (oracling) {
            oracling = false;
            return bufferTok;
        }

        String n = this.getNextChar();

        if (n.equals("\n")) {
            if (this.grammarReader.hasNextLine()) {
                this.buffer = grammarReader.nextLine();
                this.pos = -1;
            }
            return new Token("newline", "\n");
        }
        else if (n.equals("\t") || n.equals(" ")) {
            return next();
        }
        else if (n.equals("<")) {
            String nonTerminalName = "";

            while (!this.lookAheadChar().equals(">")) {
                nonTerminalName += this.getNextChar();
            }

            this.getNextChar();

            if (nonTerminalName.equals("epsilon")) {
                // special case for <epsilon> to generate terminal instead
                return new Token("terminal", "epsilon");
            }

            return new Token("nonterminal", "<" + nonTerminalName + ">");
        }
        else if (n.equals(".")) {
            if (this.pos == 0) {
                return new Token("nonetoken", "^D");
            }
        }
        else if (n.equals("|")) {
            return new Token("shortrule", "|");
        }
        else if (n.equals(":")) {
            if (this.lookAheadChar().equals(":")) {
                this.getNextChar();

                if (this.lookAheadChar().equals("=")) {
                    this.getNextChar();
                    return new Token("nonterminal", "::=");
                }
                return new Token("terminal", ":=");
            }
            return new Token("terminal", ":");
        }

        String terminal = n;
        while (!this.lookAheadChar().equals(" ") && !this.lookAheadChar().equals("\t") && !this.lookAheadChar().equals("\n")) {
            terminal += this.getNextChar();
        }

        return new Token("terminal", terminal);
    }

    /**
     * Look into future one token
     * @return the token that will be seen with the next next() call
     */

    public Token lookAhead() {
        if (oracling) {
            return bufferTok;
        }

        bufferTok = next();
        oracling = true;
        return bufferTok;
    }

    /**
     * Get the next character from the source file
     * @return next character from source
     */

    public String getNextChar() {
        if (this.pos >= buffer.length() - 1) {
            return "\n";
        }

        this.pos += 1;
        return String.valueOf(this.buffer.charAt(pos));
    }

    /**
     * Look into the future one character
     * @return the character that will be seen with the next getNextChar() call
     */

    public String lookAheadChar() {
        if (this.pos >= buffer.length() - 1) {
            return "\n";
        }

        return String.valueOf(buffer.charAt(pos + 1));
    }
}
