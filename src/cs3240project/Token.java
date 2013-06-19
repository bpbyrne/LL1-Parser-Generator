package cs3240project;

/**
 * Represents a token (grammar, spec, etc)
 */

public class Token {
    private String type;
    private String instance;

    /**
     * Create a new token
     * @param type class of token
     * @param instance actual value of the token in the source file
     */
	
    public Token(String type, String instance) {
        this.type = type;
        this.instance = instance;
    }

    /**
     * Get the type of the token
     * @return type of token
     */
	
    public String getType() {
        return this.type;
    }

    /**
     * Set the type of the token
     * @param type type of token
     */
	
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get the instance of a token
     * @return instance of token
     */
	
    public String getInstance() {
        return this.instance;
    }

    /**
     * Set the instance of a token
     * @param instance instance of token
     */
	
    public void setInstance(String instance) {
        this.instance = instance;
    }
}
