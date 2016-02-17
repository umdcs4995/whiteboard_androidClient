package contacts;

/**
 * Created by rob on 2/14/16.
 */
public class ContactWb {

    private String name;
    private int status;
    private String id;

    /**
     * Default constructor for ContactWb
     *
     * @param id      Sets the id to the passed in id string
     * @param name    Sets the name to the string passed in
     */
    public ContactWb(String id, String name) {
        this.name = name;
        this.status = ContactList.STATUS_DISCONNECTED;
        this.id = id;
    }

    /**
     * Additional Constructor for ContactWb
     *
     * @param id            Sets the id to the passed in id string
     * @param name          Sets the name to the string passed in
     * @param contactStatus Sets the contact status
     */
    public ContactWb(String id, String name, int contactStatus) {
        this.name = name;
        this.status = contactStatus;
        this.id = id;
    }

    /**
     * Simple getter method that returns the private string "name"
     * @return name
     */
    public String getName() {
        return name;
    }
    /**
     * Simple getter method that returns the status (int)
     * @return status
     */
    public int getStatus() {
        return status;
    }
    /**
     * Simple setter method that sets the name of the private string
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Simple setter method that sets the status
     * @param status
     */
    public void setStatus(int status) {
        this.status = status;
    }
    /**
     * Simple getter method that returns the id as a string
     * @return id
     */
    public String getID() {
        return id;
    }

}
