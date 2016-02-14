package contacts;

import java.util.LinkedList;

/**
 * Created by Rob on 2/12/2016.
 */
public class ContactList {
    //Static variables
    public static final int STATUS_CONNECTED = 1;
    public static final int STATUS_DISCONNECTED = 0;

    //ContactList data members
    LinkedList<ContactWb> contacts = new LinkedList<>();

    /**
     * Default constructor for contact list.
     */
    public ContactList() {

    }

    public void addContact(String id, String name, int connectionStatus) {
        contacts.add(new ContactWb(id, name,connectionStatus));
    }

    private ContactWb getContact(String id) {
        for(int i = 0; i < contacts.size(); i++) {
            if(contacts.get(i).getID().equals(id)) {
                return contacts.get(i);
            }
        }
        return null;
    }

    public ContactWb getContactOrdinal(int ordinal) {
        return contacts.get(ordinal);
    }

    public boolean changeStatus(String id, int newConnectionStatus) {
        try {
            getContact(id).setStatus(newConnectionStatus);
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean changeName(String id, String newName) {
        try {
            getContact(id).setName(newName);
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean removeContact(String id) {
        return contacts.remove(getContact(id));
    }

    public int getSize() {
        return contacts.size();
    }

    public String getName(String id) {
        return this.getContact(id).getName();
    }

    public String getName(int contactOrder) {
        return contacts.get(contactOrder).getName();
    }

    public int getConnectionStatus(String id) {
        return this.getContact(id).getStatus();
    }

    public String getConnectionStatus(int contactOrder) {
        return contacts.get(contactOrder).getName();
    }
}
