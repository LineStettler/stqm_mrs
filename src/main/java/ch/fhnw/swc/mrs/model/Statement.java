 package ch.fhnw.swc.mrs.model;

import java.util.List;

public abstract class Statement {

    private List<Rental> rentals;
    private String firstName;
    private String lastName;

    /**
     * Creates a statement object for a person (with the given name parameter) and a list of rentals.
     * @param name the family name.
     * @param firstName the first name.
     * @param rentals a list of rentals to be billed.
     */
    public Statement(String name, String firstName, List<Rental> rentals) {
        if (name.length() > 8 )
        {
            name = name.substring(0,5) + "..";
        }
        if(firstName.length() > 8 ){
            firstName = firstName.substring(0,5) + "..";
        }
        if( rentals == null) {
            throw new IllegalArgumentException();
        }
        this.firstName = firstName;
        this.lastName = name;
        this.rentals = rentals;
    }
    //add if the name is longer than 8, just show first 6 chars and add ..

    
    /** @return A list of rentals to be used to print a statement. */
    public List<Rental> getRentals() {
        return rentals;
    }
    
    /** @return The first name of the client for whom the statement is created. */
    public String getFirstName() {
        return firstName;
    }
    
    /** @return The family name of the client for whom the statement is created. */
    public String getLastName() {
        return lastName;
    }

    /**
     * Prints a statement for the given client and the list of rentals.
     * @return a multi-line string containing an aligned statement (when using fixed spaced font).
     */
    public abstract String print();
}
