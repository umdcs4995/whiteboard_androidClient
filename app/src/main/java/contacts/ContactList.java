package contacts;

import java.util.LinkedList;

/**
 * ContactList stores a list of whiteboard-contacts.
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

    /**
     * Adds the contact with the specified id, name, and connection status to the list.
     * @param id - the unique String identification for each contact
     * @param name - the name of the contact
     * @param connectionStatus - an int representing the connection status with connected (1)
     *                         or disconnected (2)
     */
    public void addContact(String id, String name, int connectionStatus) {
        contacts.add(new ContactWb(id, name, connectionStatus));
    }


    /**
     * Returns the contact associated with the specified id.
     * @param id - the unique String identification for each contact
     * @return the contact associated with the given id, or null if none could be found.
     */
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

    /**
     * Changes the connection status for a whiteboard contact.
     * @param id - the unique id associated with the contact
     * @param newConnectionStatus - an integer representing the new connection status
     * @return a boolean representing the method's success
     */
    public boolean changeStatus(String id, int newConnectionStatus) {
        try {
            getContact(id).setStatus(newConnectionStatus);
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * Changes the name for a whiteboard contact.
     * @param id - the unique id representing the contact
     * @param newName - the new name for the contact
     * @return a boolean representing the method's success.
     */
    public boolean changeName(String id, String newName) {
        try {
            getContact(id).setName(newName);
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * Removes the contact associated with the given id from the contact list.
     * @param id - the unique id associated with the contact
     * @return a boolean representing the method's success
     */
    public boolean removeContact(String id) {
        return contacts.remove(getContact(id));
    }

    /**
     * Returns the size of the contact list.
     * @return the size of the list
     */
    public int getSize() {
        return contacts.size();
    }

    /**
     * Returns the name of the contact associated with the given id.
     * @param id - the unique id associated with a contact
     * @return the name of the contact
     */
    public String getName(String id) {
        return this.getContact(id).getName();
    }

    /**
     *
     * @param contactOrder
     * @return
     */
    public String getName(int contactOrder) {
        return contacts.get(contactOrder).getName();
    }

    /**
     * Returns the connection status for the contact associated with the given id.
     * @param id - the unique id associated with the contact
     * @return - the connection status
     */
    public int getConnectionStatus(String id) {
        return this.getContact(id).getStatus();
    }

    /**
     * 
     * @param contactOrder
     * @return
     */
    public String getConnectionStatus(int contactOrder) {
        return contacts.get(contactOrder).getName();
    }
}
