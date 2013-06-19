package cs3240project;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Represent a DFA that has been converted from an NFA. Has convenience methods.
 */

public class DFATable {

    public static int ISERR = -1;
    private static int ERROR = -2;

    /**
     * Represent a single state in the DFATable. Has transitions.
     */

    public class State {
        private boolean isEndState;		
        private List<Integer> visibleStates;
        private Map<Character, Integer> transitions;
        private String id;

        /**
         * Create an empty state.
         */

        public State() {
            this.transitions = new HashMap<Character, Integer>();
            this.isEndState = false;
            this.id = null;
            this.visibleStates = new ArrayList<Integer>();
        }

        /**
         * Whether or not this state represents an accepting state
         * @return if this is an accepting state
         */
		
        public boolean isEndState() {
            return this.isEndState;
        }

        /**
         * States that are reachable from the current state
         * @return list of ids of states that are visible
         */
		
        public List<Integer> getVisibleStates() {
            return this.visibleStates;
        }

        /**
         * Attach a name to this state
         * @param id name based on specification
         */

        public void setID(String id) {
            this.id = id;
        }

        /**
         * Get the name of this state
         * @return name of this state
         */

        public String getID() {
            return this.id;
        }

        /**
         * Get the map of transitions from character to state id
         * @return map of transitions
         */
		
        public Map<Character, Integer> transitions() {
            return this.transitions;
        }

        /**
         * Turn on or off whether this state is accepting
         * @param isEndState whehter this state accepts or not
         */
		
        public void setEndState(boolean isEndState) {
            this.isEndState = isEndState;
        }

        /**
         * Make states visible to this state
         * @param visibleStates list of state ids to make visible
         */
		
        public void addVisibleStates(List<Integer> visibleStates) {
            this.visibleStates.addAll(visibleStates);
        }

        /**
         * Transition from this state to another one
         * @param transChar character to require for the transition
         * @param next next state id
         */
		
        public void transition(char transChar, int next) {
            this.transitions.put(transChar, next);
        }

        /**
         * Check whether a transition exists or not based on an input
         * @return whether a transition exists
         */
		
        public boolean hasTransition(char transChar) {
            return this.transitions.containsKey(transChar);
        }
    }

    private List<State> states;
    private NFA nfa;
    private Integer executionState;

    /**
     * Construct a new DFATable
     */

    public DFATable() {
        this.states = new ArrayList<State>();
        this.executionState = 0;
    }

    /**
     * Reset the execution state of the DFATable to 0. Lose track of position.
     */

    public void resetExecution() {
        gotoState(0);
    }

    /**
     * Jump to a specific state ID in the DFATable.
     * @param sInt state ID
     */

    private void gotoState(Integer sInt) {
        this.executionState = sInt;
    }

    /**
     * Perform epsilon closure to gather all states that are possible to be in
     * @param states ids of original states
     * @return ids of states after all epsilon moves are made
     */
	
    private List<Integer> eClosure(List<Integer> states) {
        Set<Integer> output = new HashSet<Integer>();
        List<Integer> outputList = new ArrayList<Integer>();
        Stack<Integer> queue = new Stack<Integer>();
        queue.addAll(states);

        while (!queue.isEmpty()) {
            int current = queue.pop();
            output.add(current);

            for (Transition transition : this.nfa.getStates().get(current).transitions()) {
                if (transition.transChar() == '\0') {
                    if (!output.contains(transition.next())) {
                        queue.push(transition.next());
                    }
                }
            }
        }

        outputList.addAll(output);
        return outputList;
    }

    /**
     * Convenience helper for taking an NFA and making an equivalent DFATable
     * @param nfa original NFA
     */
	
    public void importNFA(NFA nfa) {
        this.nfa = nfa;

        List<Integer> states = new ArrayList<Integer>();
        states.add(0);
        this.states.add(new State());
        this.states.get(0).addVisibleStates(eClosure(states));

        for (int z = 0; z < this.states.size(); z++) {
            State currState = this.states.get(z);

            for (Integer visibleState : currState.getVisibleStates()) {
                for (Transition transition : nfa.getState(visibleState).transitions()) {
                    boolean failLookup = false;
                    Integer transitionState = ERROR;

                    if (transition.transChar() == '\0' || currState.hasTransition(transition.transChar())) {
                        continue;
                    }

                    List<Integer> nextStates = new ArrayList<Integer>();
                    nextStates.add(transition.next());
                    for (Integer nextVisibleState : currState.getVisibleStates()) {
                        for (Transition nextTransition : nfa.getState(nextVisibleState).transitions()) {
                            if (transition.transChar() != nextTransition.transChar() || nextStates.contains(nextTransition.next())) {
                                continue;
                            }
                            nextStates.add(nextTransition.next());
                        }
                    }
                    nextStates = eClosure(nextStates);

                    for (State state : this.states) {
                        failLookup = false;
                        for (Integer nextState : nextStates) {
                            if (!state.getVisibleStates().contains(nextState)) {
                                failLookup = true;
                                break;
                            }
                        }
                        if (!failLookup) {
                            transitionState = this.states.indexOf(state);
                            break;
                        }
                    }
						
                    if (!failLookup) {
                        currState.transition(transition.transChar(), transitionState);
                        continue;
                    }

                    State newState = new State();
                    newState.addVisibleStates(nextStates);
                    this.states.add(newState);

                    currState.transition(transition.transChar(), this.states.size()-1);
                }
            }
        }
		
        for (State state : this.states) {
            for (Integer reference : state.getVisibleStates()) {
                state.setID(nfa.getState(reference).getID());

                if (nfa.getState(reference).isEndState()) {
                    state.setEndState(true);
                    break;
                }
            }
        }
    }

    /**
     * Read in some input character and execute action on the current DFATable state
     * @param transChar the transition character executed
     */

    public void execute(char transChar) {
        int nextState = ERROR;

        if ((this.executionState >= 0) &&
            (this.states.get(this.executionState).transitions().containsKey(transChar))) {
            nextState = this.states.get(this.executionState).transitions().get(transChar);
        }

        this.executionState = nextState;
    }

    /**
     * The name of the state that is active in our current execution.  If we are in an invalid state, the
     * name is "error"
     * @return state name
     */
	
    public String getCurrentID() {
        if (this.executionState < 0) {
            return "error";
        }

        return this.states.get(this.executionState).getID();
    }
}
