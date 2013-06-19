package cs3240project;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Arrays;

/**
 * Parse a specification file
 */

public class SpecParser {
    private SpecScanner gScanner;
    private Stack<NFA> pda;
    private NFA derivedNFA;
    private List<NFA> definitionNFAs;
    private boolean isTokenDefinition;
    private int unmatchedParens;
    private boolean inExclusion;

    /* printable characters that can exist inside of a regular expression */
    private static final List<String> RE_CHAR = Arrays.asList(new String[] {
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
            "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<",
            "=", ">", "^", "_", "{", "}", "~", "!", "#", "$", "%", "&", ",",
            "-", "/", "@", "`",

            "\\ ", "\\\\", "\\*", "\\+", "\\?", "\\|", "\\[", "\\]", "\\(",
            "\\)", "\\.", "\\'", "\\\"", /* escape these for character usage */
        });

    /* characters that can make up character classes */
    private static final List<String> CLS_CHAR = Arrays.asList(new String[] {
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
            "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<",
            "=", ">", "_", "{", "}", "~", "!", "#", "$", "%", "&", ",", "/",
            "@", "`", " ", "*", "+", "?", "|", "(", ")", ".", "'", "\"",

            "\\ ", "\\\\", "\\^", "\\-", "\\[", "\\]" /* escape these for character usage */
        });

    /**
     * Create a new specification file parser
     * @param gScanner spec file scanner
     */

    public SpecParser(SpecScanner gScanner) {
        this.gScanner = gScanner;
        this.pda = new Stack<NFA>();
        this.definitionNFAs = null;
        this.derivedNFA = new NFA();

        /* put new NFA on our stack */
        pda.push(this.derivedNFA);

        this.isTokenDefinition = false;
        this.unmatchedParens = 0;
    }

    /**
     * Set the NFAs that we defined previously for use here (for like char classes and such)
     * @param definitionNFAs NFAs that we've already resolved names of -- we can use them within the current parsing
     */

    public void useDefs(List<NFA> definitionNFAs) {
        this.definitionNFAs = definitionNFAs;
    }

    /**
     * Execute the parsing of specification
     */

    public void parseSpec() {
        this.inExclusion = false;

        gRegex();

        /* pop original NFA from our stack */
        this.derivedNFA = pda.pop();
    }

    /**
     * Whether the definition being parsed is a token definition or a character class
     * @return true if the current definition is a token definition
     */

    public boolean isTokenDefinition() {
        return this.isTokenDefinition;
    }

    /**
     * Get the NFA created from this parsing
     * @return nfa created with the spec parser
     */
	
    public NFA getDerivation() {
        return this.derivedNFA;
    }

    /**
     * Start parsing a regular expression
     */
	
    private void gRegex() {
        gRexp();
    }
	
    private void gRexp() {
        gRexp1();
        gRexpPrime();
    }

    private void gRexpPrime() {
        String type = gScanner.lookAhead().getType();

        if (type.equals("union")) {
            gScanner.next();

            NFA nfa = pda.pop();
            nfa.union();
            pda.push(nfa);

            gRexp1();
            gRexpPrime();

            this.isTokenDefinition = true;			
        }
        return;
    }
	
    private void gRexp1() {
        gRexp2();
        gRexp1Prime();
    }
	
	
    private void gRexp1Prime() {
        String type = gScanner.lookAhead().getType();

        if (any(type.equals("lbracket"), type.equals("lparenthesis"), type.equals("dot"), type.startsWith("_"))) {
            gRexp2();
            gRexp1Prime();
        }
        else if (type.equals("literal")) {
            if (!RE_CHAR.contains(gScanner.lookAhead().getInstance())) {
                return;
            }
			
            gRexp2();
            gRexp1Prime();
        }
        return;
    }
	
    private void gRexp2() {
        String type = gScanner.lookAhead().getType();

        if (type.equals("lparenthesis")) {
            pda.push(new NFA());
            gScanner.next();

            isTokenDefinition = true;
            gRexp();
            gScanner.next();

            unmatchedParens += 1;
            gRexp2Tail();
        }

        else if (type.equals("literal")) {
            NFA popped = pda.pop();

            Token token = gScanner.next();
            if (token.getInstance().charAt(0) == '\\') {
                popped.concat(token.getInstance().substring(1));
            }
            else {
                popped.concat(token.getInstance());
            }
            pda.push(popped);

            isTokenDefinition = true;
            gRexp2Tail();
        }

        gRexp3();
    }
	
    private void gRexp2Tail() {
        String type = gScanner.lookAhead().getType();
        if (type.equals("star")) {
            isTokenDefinition = true;
            gScanner.next();

            if (unmatchedParens > 0) {
                unmatchedParens -= 1;
                NFA popped = pda.pop();
                popped.resetScope();
                popped.star();

                NFA prev = pda.pop();
                prev.link(popped);
                prev.linkEnd();
                pda.push(prev);
            }
            else {
                NFA popped = pda.pop();
				
                popped.star();
                pda.push(popped);
            }
        }

        else if (type.equals("plus")) {
            gScanner.next();

            if (unmatchedParens > 0) {
                NFA popped = pda.pop();
                popped.resetScope();
                popped.star();
                NFA prev = pda.pop();
                prev.link(popped);
                prev.linkEnd();
                pda.push(prev);

                unmatchedParens -= 1;
            }
            else {

                NFA popped = pda.pop();
                popped.star();
                pda.push(popped);
            }
        }
        
        if (unmatchedParens > 0) {
            NFA popped = pda.pop();
            NFA prev = pda.pop();
            prev.link(popped);
            prev.linkEnd();
            pda.push(prev);

            unmatchedParens -= 1;
        }

        isTokenDefinition = true;
			
        return;
    }
	
    private void gRexp3() {
        String type = gScanner.lookAhead().getType();
        if (any(type.equals("dot"), type.equals("lbracket"), type.startsWith("_"))) {
            gCharClass();
        }

        return;
    }
	
    private void gCharClass() {
        String type = gScanner.lookAhead().getType();
        String dot = "";
        this.inExclusion = false;
		
        if (type.equals("dot")) {
            gScanner.next();
			
            NFA popped = pda.pop();

            for (char i = 0; i < 128; i++) {
                if (i == '\n' || i == '\r') {
                    // dot character matches everything except \n and \r
                    continue;
                }
                dot += i;
            }

            popped.concat(dot);
            pda.push(popped);
        }
        else if (type.equals("lbracket")) {
            gScanner.next();
            gCharClass1();
        }
        else  {
            gDefined(gScanner.next());
        }
    }
	
    private void gCharClass1() {
        String type = gScanner.lookAhead().getType();
        String range;
        if (type.equals("caret")) {
            range = gExcludeSet("");
        }
        else {
            range = gCharSetList("");
        }
		
        NFA popped = pda.pop();
        popped.concat(range);
        pda.push(popped);

        return;
    }
	
    private String gCharSetList(String range) {
        String type = gScanner.lookAhead().getType();

        if (any(type.equals("dot"), type.equals("literal"))) {
            if (!CLS_CHAR.contains(gScanner.lookAhead().getInstance())) {
                return range;
            }

            return gCharSetList(gCharSet(range));
        }

        return range;
    }
	
    private String gCharSet(String charRange) {
        String lower = gScanner.next().getInstance();

        if (gScanner.lookAhead().getType().equals("literal") &&
            gScanner.lookAhead().getInstance().equals("-")) {
            gScanner.next();
            String upper = gScanner.next().getInstance();

            char i = lower.charAt(0) == '\\' ? lower.charAt(1) : lower.charAt(0);
            char j = upper.charAt(0) == '\\' ? upper.charAt(1) : upper.charAt(0);

            for (; i <= j; i++) {
                charRange += i;
            }

            return charRange;
        }
        
        if (lower.charAt(0) == '\\') {
            charRange += lower.charAt(1);
        }
        else {
            charRange += lower.charAt(0);
        }
        return charRange;
    }
	
    private String gExcludeSet(String range) {
        gScanner.next();
        String exclude = gCharSet("");

        gScanner.next();
        gScanner.next();
        String superset = gExcludeSetTail();
		
        for (int i = 0; i < superset.length(); i++) {
            if (!exclude.contains(String.valueOf(superset.charAt(i)))) {
                range += superset.charAt(i);
            }
        }
		
        return range;
    } 
	
    private String gExcludeSetTail() {
        String type = gScanner.lookAhead().getType();
        this.inExclusion = true;

        if (type.equals("lbracket")) {
            gScanner.next();
            String range = gCharSet("");
            gScanner.next();
            return range;
        }

        return gDefined(gScanner.next());
    }
	
    private String gDefined(SpecToken token) {
        NFA nfa = null;

        for (NFA defNFA : this.definitionNFAs) {
            if (defNFA.getName().equals(token.getInstance())) {
                nfa = defNFA;
                break;
            }
        }
		
        if (this.inExclusion) {
            String set = "";
            for (Transition nextT : nfa.getState(1).transitions()) {
                if (nextT.transChar() != '\0') {
                    set += nextT.transChar();
                }
            }
            return set;
        }

        NFA popped = pda.pop();
        popped.link(nfa);
        popped.linkEnd();
        pda.push(popped);

        return null;
    }

    /**
     * Helper method for checking tokens
     * @param choices list of choices
     * @return true if any choice is true
     */

    private boolean any(boolean... choices) {
        /* use general form since we need to work with startsWith and not just equals for comparing token types */
        for (boolean b : choices) {
            if (b) {
                return true;
            }
        }
        return false;
    }
	

}

