package cs3240project;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class NFA {
	
    public class State {
        //private Map<Character, List<Integer>> transitions;
        private boolean isEndState;
        private String identifier;
        private List<Transition> o_transitions; /* re-done test transitions */

        public State() {
            //this.transitions = new HashMap<Character, List<Integer>>();
            this.o_transitions = new ArrayList<Transition>();
            this.identifier = null;
            this.isEndState = false;
        }


        public boolean transition(char transChar, int next) {
            //if (!this.transitions.containsKey(transChar)) {
            //this.transitions.put(transChar, new ArrayList<Integer>());
            //}
            return this.o_transitions.add(new Transition(transChar, next));
            //            return this.transitions.getState(transChar).add(next);
        }

        public boolean isEndState() {
            return this.isEndState;
        }

        public void setEndState(boolean isEndState) {
            this.isEndState = isEndState;
        }

        public void setID(String id) {
            this.identifier = id;
        }

        public String getID() {
            return this.identifier;
        }

        public List<Transition> transitions() {
            return this.o_transitions;
            //            return this.transitions;
        }
    }

    private String name;
    private List<State> states;
    private NFA linkedNFA;
    private int curr;
    private int held;
    private int prev;
	
    public NFA() {
        this.states = new ArrayList<State>();
        this.states.add(new State());
        this.states.add(new State());
        this.transition(0, 1, '\0');
        this.curr = 1;
        this.linkedNFA = null;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        for (NFA.State s : this.getStates()) {
            s.setID(name);
        }
    }

    public int size() {
        return this.states.size();
    }

	
    public State getState(Integer sInt) {
        return this.states.get(sInt);
    }
	
    public List<State> getStates() {
        return this.states;
    }
	
    public void setEndState(int i, boolean isEndState) {
        this.states.get(i).setEndState(isEndState);
    }
	

    public void linkStart() {
        holdSize();
        linkedNFA.unfray();

        this.prev = this.curr;
        this.curr = linkedNFA.curr + heldSize();

        for (int i = 0; i < linkedNFA.size(); i++) {
            this.states.add(new State());
            for (Transition t : linkedNFA.getState(i).transitions()) {
                this.transition(this.last(), t.next() + heldSize(), t.transChar());
            }

            if (linkedNFA.getState(i).getID() == null) {
                this.getState(this.last()).setID(linkedNFA.getState(i-1).getID());
            } else {
                this.getState(this.last()).setID(linkedNFA.getState(i).getID());
            }

            if (linkedNFA.getState(i).isEndState()) {
                this.getState(this.last()).setEndState(true);
            }
        }
        this.transition(0, heldSize(), '\0');
    }
	
    public boolean transition(int start, int next, char transChar) {
        return this.states.get(start).transition(transChar, next);
    }
	
    public void linkEnd() {
        holdSize();
        linkedNFA.unfray();

        for (int i = 0; i < linkedNFA.size(); i++) {
            this.states.add(new State());
            for (Transition t : linkedNFA.getState(i).transitions()) {
                this.transition(this.last(), t.next()+heldSize(), t.transChar());
            }

        }

        this.transition(this.curr, heldSize(), '\0');
        this.prev = linkedNFA.prev;
        this.curr = linkedNFA.curr + heldSize();
    }

    public void link(NFA linkedNFA) {
        this.linkedNFA = linkedNFA;
    }

    public int last() {
        return this.size() - 1;
    }
    
    public void holdSize() {
        this.held = this.size();
    }

    public int heldSize() {
        return this.held;
    }
	
    public void concat(String transChars) {
        this.states.add(new State());
        for (Character ch : transChars.toCharArray()) {
            this.transition(this.curr, this.last(), ch);
        }
        this.prev = this.curr;
        this.curr = this.last();
    }
	
    public void union() {
        this.states.add(new State());
        this.transition(0, this.last(), '\0');
        this.setEndState(this.curr, true);
        this.curr = this.last();
        this.prev = 0;
    }
	
    public void star() {
        this.states.add(new State());
        this.transition(this.curr, this.prev, '\0');
        this.prev = this.curr;
        this.transition(this.prev, this.last(), '\0');
        this.curr = this.last();
    }

    public void resetScope() {
        this.unfray();
        this.prev = 0;
    }

    private void unfray() {
        this.states.add(new State());

        for (State k : getStates()) {
            if (k.isEndState()) {
                k.setEndState(false);
                k.transition('\0', this.last());
            }
        }

        this.setEndState(this.last(), true);
        this.transition(this.curr, this.last(), '\0');
        this.prev = this.curr;
        this.curr = this.last();
    }

}
