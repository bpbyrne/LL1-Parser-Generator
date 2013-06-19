package cs3240project;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Parses the tokens from the Grammar Scanner into useful first and follow sets, then a parse table
 */

public class GrammarParser {

    /* epsilon is a special character */
    private static final String EPS_SPECIAL_TOKEN = "epsilon";

    /* keep track of if we are finding new information (expanding size) */
    private Integer heldSize;

    /* valid literal terminals + epsilon */
    /*
      private static final List<String> terms = Arrays.asList(new String[] {"begin", "ID", "=", "end",
      ";", "with", "in", "replace", "recursivereplace",
      "union", "inters", "maxfreqstring", "REGEX",
      ">!", "print", "(", ")", ",", "#", "diff", 
      "ASCII-STR", "find", EPS_SPECIAL_TOKEN});
    */


    /**
     * Individual rule for the grammar
     */

    private class GrammarRule {
        private GrammarToken nonTerminal;
        private List<GrammarToken> tokens;
        private List<GrammarToken> first;

        /**
         * Create a new empty rule for a nonterminal
         * @param nonTerminal token that this rule is for
         */
    
        public GrammarRule(GrammarToken nonTerminal) {
            this.nonTerminal = nonTerminal;
            this.first = new ArrayList<GrammarToken>();
            tokens = new ArrayList<GrammarToken>();
        }

        /**
         * Get the token associated with the rule
         * @return nonterminal token for rule
         */

        public GrammarToken getToken() {
            return nonTerminal;
        }

        /**
         * Set the token associated with the rule
         * @param nonTerminal nonterminal token for rule
         */

        public void setToken(GrammarToken nonTerminal) {
            this.nonTerminal = nonTerminal;
        }

        /**
         * Get right side token associated with the rule
         * @return right side tokens for rule
         */

        public List<GrammarToken> getTokens() {
            return tokens;
        }

        /**
         * Set right side token associated with the rule
         * @return right side tokens for rule
         */

        public void setTokens(List<GrammarToken> tokens) {
            this.tokens = tokens;
        }

        /**
         * Add token on right side
         * @param token token to add
         */

        public void addToken(GrammarToken token) {
            tokens.add(token);       
        }

        /**
         * Get the first set for this rule
         * @return first set for rule
         */

        public List<GrammarToken> getFirst() {
            return first;
        }

        /**
         * Set the first set for this rule
         * @param first first set for rule
         */

        public void setFirst(List<GrammarToken> first) {
            this.first = first;
        }

        public String toString() {
            String repr = String.format("%s ::= ", nonTerminal.getInstance());

            for (GrammarToken token : tokens) {
                repr += String.format("%s ", token.getInstance());
            }

            return repr;
        }
    }

    /**
     * Parse table using first and follow sets
     */

    private class ParseTable {
	private GrammarRule[][] derivationTable;
	private ArrayList<GrammarRule> rules;
	private List<GrammarToken> terminals;
	private List<GrammarToken> nonterminals;

        /**
         * Create a new Parse table
         * @param terminals terminal tokens
         * @param nonterminals nonterminal tokens
         */

	public ParseTable(List<GrammarToken> terminals, List<GrammarToken> nonterminals) {

            this.terminals = terminals;
            this.nonterminals = nonterminals;
            this.derivationTable = new GrammarRule[this.nonterminals.size()][this.terminals.size()];

	}

        /**
         * Construct the parse table given a set of known rules
         * @param rules rules from the grammar
         * @return the parse table with the derivations filled out based on rules
         */

        public ParseTable constructWithRules(ArrayList<GrammarRule> rules) {
            this.rules = rules;

            for (GrammarRule rule : rules) {
                List<GrammarToken> first = rule.getFirst();
                GrammarToken nonTerminalName = rule.getToken();

                int nonterminal = this.nonterminals.indexOf(nonTerminalName);

                for (GrammarToken firstToken : first) {
                    if (firstToken.getInstance().equals(EPS_SPECIAL_TOKEN)) {
                        List<GrammarToken> follow = nonTerminalName.getFollow();
                        for (GrammarToken followToken : follow) {
                            for (int l = 0; l < this.terminals.size(); l++) {
                                if (this.terminals.get(l).equals(followToken)) {
                                    this.derivationTable[nonterminal][l] = rule;
                                }
                            }
                        }
                    }
                    else {
                        for (int k = 0; k < this.terminals.size(); k++) {
                            if (this.terminals.get(k).equals(firstToken)) {
                                this.derivationTable[nonterminal][k] = rule;
                            }
                        }
                    }
                }
            }
            
            return this;
        }

	public String toString() {
            String repr = "";

            for (int i = 0; i < this.derivationTable.length; i++) {
                repr += String.format(" When at nonterminal %s:\n", nonterminals.get(i));
                repr += "/--------------------------------------------------------------------------\n";
                repr += "|  LL(1) Next Token\t| Chosen Derivation\n";
                repr += "|--------------------------------------------------------------------------\n";
                for (int j = 0; j < this.derivationTable[i].length; j++) {
                    if (this.derivationTable[i][j] != null) {
                        String glue = null;
                        if (this.terminals.get(j).getInstance().length() < 5) {
                            glue = " \t\t\t| ";
                        }
                        else if (this.terminals.get(j).getInstance().length() < 13) {
                            glue = " \t\t| ";
                        }
                        else {
                            glue = " \t| ";
                        }
                        repr += String.format("| %s%s%s\n", this.terminals.get(j), glue, this.derivationTable[i][j]);
                    }
                }
                repr += "\\--------------------------------------------------------------------------\n\n";

            }
            return repr;
	}
    }

    private List<GrammarToken> terminals;
    private List<GrammarToken> nonTerminals;
    private GrammarToken init;
    private GrammarScanner gScanner;
    private GrammarToken doneToken;

    /**
     * Create a parser for the grammar file
     */
	
    public GrammarParser() {
        this.heldSize = 0;
        this.terminals = new ArrayList<GrammarToken>();
        this.nonTerminals = new ArrayList<GrammarToken>();
        this.doneToken = new GrammarToken("nonetoken", "^D", true);

        init = new GrammarToken("nonterminal", "MiniRE-program", false);
    }

    /**
     * Keep track of the length of a token list. Useful for determining whether to continue or if we've exhausted all productions.
     * @param s token list
     */

    private void holdSize(List<GrammarToken> s) {
        this.heldSize = s.size();
    }

    /**
     * Get the value of the last stored held size
     * @return frozen size of last stored list
     */

    private Integer heldSize() {
        return this.heldSize;
    }

    /**
     * Build a parse table
     * @param grammarFile the grammar file to use for building
     * @return the parse table
     */

    public ParseTable buildTable(File grammarFile) throws FileNotFoundException {
        ArrayList<GrammarRule> rules = new ArrayList<GrammarRule>();
        this.gScanner = new GrammarScanner(grammarFile);

        this.parseFile(gScanner, rules);
        this.computeFirstSet(rules);

        /* After first sets have been computed: table *must* always rebuild follow sets */
        GrammarToken epsilon = null;
        for (int q = 0; q < terminals.size(); q++) {
            if (terminals.get(q).getInstance().equals(EPS_SPECIAL_TOKEN)) {
                epsilon = terminals.get(q);
            }
        }

        terminals.add(doneToken);
        init.addToFollow(doneToken);

        while (true) {
            boolean finished = true;

            for (int f = 0; f < rules.size(); f++) {
                GrammarRule instGrammarRule = rules.get(f);
                List<GrammarToken> Tokens = instGrammarRule.getTokens();

                for (int d = 0; d < Tokens.size(); d++) {
                    GrammarToken thisTerminal = Tokens.get(d);
                    List<GrammarToken> follow = thisTerminal.getFollow();


                    GrammarToken thisTerm = epsilon;
                    if (d + 1 < Tokens.size()) {
                        thisTerm = Tokens.get(d + 1);
                    }

                    List<GrammarToken> thisFirst = thisTerm.getFirst();


                    holdSize(follow);
                    for (int p = 0; p < thisFirst.size(); p++) {
                        if (!thisFirst.get(p).getInstance().equals(EPS_SPECIAL_TOKEN) && !follow.contains(thisFirst.get(p))) {
                            follow.add(thisFirst.get(p));
                        }
                    }

                    if (follow.size() - heldSize() > 0) {
                        finished = false;
                    }

                    thisTerminal.setFollow(follow);
                    if (thisTerm.getFirst().contains(epsilon)) {
                        GrammarToken ruleTerm = instGrammarRule.getToken();
                        List<GrammarToken> ruleFollow = ruleTerm.getFollow();
                        List<GrammarToken> thisFollow = thisTerminal.getFollow();

                        holdSize(thisFollow);
                        for (int p = 0; p < ruleFollow.size(); p++) {
                            if (!thisFollow.contains(ruleFollow.get(p))) {
                                thisFollow.add(ruleFollow.get(p));
                            }
                        }
                        if (thisFollow.size() - heldSize() > 0) {
                            finished = true;
                        }

                        thisTerminal.setFollow(thisFollow);
                    }

                }
            }
            if (finished) { break; }
        }

        return new ParseTable(terminals, nonTerminals).constructWithRules(rules);
    }

    /**
     * Parse the grammar file based on a pre-defined grammar scanner
     * @param gScanner grammar scanner to get source data
     * @param rules list to store rules that we determine in
     */

    public void parseFile(GrammarScanner gScanner, ArrayList<GrammarRule> rules) throws FileNotFoundException {
        while (gScanner.lookAhead().getType() != "nonetoken") {
            while (gScanner.lookAhead().getType() != "nonterminal") {
                gScanner.next();
            }

            Token t = gScanner.next();
            
            GrammarToken first = new GrammarToken(t.getType(), t.getInstance(), false);
            GrammarRule instGrammarRule;

            if (!nonTerminals.contains(first)) {
                nonTerminals.add(first);
                instGrammarRule = new GrammarRule(first);
                rules.add(instGrammarRule);
            }

            else {
                int idx = nonTerminals.indexOf(first);
                first = nonTerminals.get(idx);
                instGrammarRule = new GrammarRule(first);
                rules.add(instGrammarRule);
            }

            while (!gScanner.lookAhead().getType().equals("newline") && !gScanner.lookAhead().getType().equals("nonetoken")) {

                if (gScanner.lookAhead().getType().equals("nonterminal")) {
                    Token t2 = gScanner.next();
                    GrammarToken instNonTerminal = new GrammarToken(t2.getType(), t2.getInstance(), false);

                    if (instNonTerminal.getInstance().equals("::=")) {
                        /* ignore assignments; continue to next token */
                        continue;
                    }

                    if (nonTerminals.contains(instNonTerminal)) {
                        int idx = nonTerminals.indexOf(instNonTerminal);
                        instGrammarRule.addToken(nonTerminals.get(idx));
                        continue;
                    }

                    nonTerminals.add(instNonTerminal);
                    instGrammarRule.addToken(instNonTerminal);
                }

                else if (gScanner.lookAhead().getType().equals("shortrule")) {
                    t = gScanner.next();
                    first = nonTerminals.get(nonTerminals.indexOf(first));
                    instGrammarRule = new GrammarRule(first);
                    rules.add(instGrammarRule);

                    continue;
                }

                else if (gScanner.lookAhead().getType().equals("terminal")) {
                    t = gScanner.next();

                    GrammarToken found = new GrammarToken(t.getType(), t.getInstance(), true);
                    int idx = terminals.indexOf(found);

                    if (idx < 0) {
                        terminals.add(found);
                    }
                    idx = terminals.indexOf(found);

                    instGrammarRule.addToken(terminals.get(idx));
                }
            }
            gScanner.next();
        }
    }

    /**
     * Compute the first sets of each of the rules in the grammar file
     * @param rules list of rules
     */

    public void computeFirstSet(ArrayList<GrammarRule> rules) {
        while (true) {
            boolean finished = true;

            for (GrammarRule rule : rules) {
                boolean completeOp = false;
                GrammarRule instGrammarRule = rule;
                GrammarToken thisTerminal = instGrammarRule.getToken();
                int i = 0;

                while (!completeOp) {
                    boolean hasEpsilon = false;
                    List<GrammarToken> derivation = instGrammarRule.getFirst();

                    if (i >= instGrammarRule.getTokens().size()) {
                        break;
                    }


                    List<GrammarToken> thisFirst = thisTerminal.getFirst();
                    holdSize(thisFirst);


                    List<GrammarToken> tokens = instGrammarRule.getTokens();
                    GrammarToken otherTok = tokens.get(i);
                    List<GrammarToken> otherFirst = otherTok.getFirst();


                    for (int j = 0; j < otherFirst.size(); j++) {
                        if (!otherFirst.get(j).getInstance().equals(EPS_SPECIAL_TOKEN) && !thisFirst.contains(otherFirst.get(j))) {
                            derivation.add(otherFirst.get(j));
                            thisFirst.add(otherFirst.get(j));
                        }
                    }

                    instGrammarRule.setFirst(derivation);
                    thisTerminal.setFirst(thisFirst);

                    if (thisFirst.size() - heldSize() > 0) {
                        finished = false;
                    }

                    for (int y = 0; y < otherFirst.size(); y++) {
                        if (otherFirst.get(y).getInstance().equals(EPS_SPECIAL_TOKEN)) {
                            hasEpsilon = true;
                        }
                    }
                    if (!hasEpsilon) {
                        completeOp = true;
                    }

                    i++;
                }

                if (!completeOp) {
                    int idx  = 0;
                    for (int p = 0; p < terminals.size(); p++) {
                        if (terminals.get(p).getInstance().equals(EPS_SPECIAL_TOKEN)) {
                            idx = p;
                        }
                    }

                    List<GrammarToken> thisFirsts = thisTerminal.getFirst();

                    if (thisFirsts.contains(terminals.get(idx))) {
                        continue;
                    }

                    List<GrammarToken> derivation = instGrammarRule.getFirst();
                    thisFirsts.add(terminals.get(idx));
                    thisTerminal.setFirst(thisFirsts);
						
                    derivation.add(terminals.get(idx));
                    instGrammarRule.setFirst(derivation);
						
                    finished = false;

                }

            }
            
            if (finished) { break; }
        }
    }
}

