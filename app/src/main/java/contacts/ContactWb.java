package contacts;

/**
 * Created by rob on 2/14/16.
 */
public class ContactWb {

    private String name;
    private int status;
    private String id;

    public ContactWb(String id, String name) {
        this.name = name;
        this.status = ContactList.STATUS_DISCONNECTED;
        this.id = id;
    }

    public ContactWb(String id, String name, int contactStatus) {
        this.name = name;
        this.status = contactStatus;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getID() {
        return id;
    }

}
