package com.umdcs4995.whiteboard.whiteboarddata;

/**
 * This class represents a physical user.
 * Created by Rob on 3/21/2016.
 */
public class User {
    //Static variables
    public static final int STATUS_CONNECTED = 1;
    public static final int STATUS_DISCONNECTED = 0;

    private String name;
    private int status;
    private String id;

    /**
     * Default constructor for user
     *
     * @param id      Sets the id to the passed in id string
     * @param name    Sets the name to the string passed in
     */
    public User(String id, String name) {
        this.name = name;
        this.status = this.STATUS_DISCONNECTED;
        this.id = id;
    }

    /**
     * Additional Constructor for user
     *
     * @param id            Sets the id to the passed in id string
     * @param name          Sets the name to the string passed in
     * @param contactStatus Sets the contact status
     */
    public User(String id, String name, int contactStatus) {
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
