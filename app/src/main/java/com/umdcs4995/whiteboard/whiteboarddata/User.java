package com.umdcs4995.whiteboard.whiteboarddata;

import android.media.Image;

import com.google.gson.annotations.SerializedName;


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

    private static final long serialVersionUID = 6209686573278334361L;

    @SerializedName("userid") private int mUserid;
    @SerializedName("username") private String mUsername;
    @SerializedName("image") private Image mAvatar;
    @SerializedName("reputation") private int mReputation;
    @SerializedName("join_date") private int mDate;
    @SerializedName("certification_count") private int mCertificationCount;
    @SerializedName("summary") private String mSummary;
    @SerializedName("about_raw") private String mAboutRaw;
    @SerializedName("about_rendered") private String mAboutRendered;
    @SerializedName("authToken") private String mAuthToken;

    /**
     * Used for reauthentication. It isn't returned by the API but it's used internally.
     */
    public String mEmail;

    /**
     * Used for using the correct site for syncing the user's data. It isn't returned by
     * the API but it's used internally.
     */
    public String mSiteName;

    public User() {

    }

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

        public int getUserid() {
            return mUserid;
        }

        public void setUserid(int userid) {
            mUserid = userid;
        }

        public String getUsername() {
            return mUsername;
        }

        public void setUsername(String mUsername) {
            this.mUsername = mUsername;
        }

        public Image getAvatar() {
            return mAvatar;
        }

        public void setAvatar(Image mAvatar) {
            this.mAvatar = mAvatar;
        }

        public int getReputation() {
            return mReputation;
        }

        public void setReputation(int mReputation) {
            this.mReputation = mReputation;
        }

        public int getJoinDate() {
            return mDate;
        }

        public void setJoinDate(int mDate) {
            this.mDate = mDate;
        }

        public int getCertificationCount() {
            return mCertificationCount;
        }

        public void setCertificationCount(int mCertificationCount) {
            this.mCertificationCount = mCertificationCount;
        }

        public String getSummary() {
            return mSummary;
        }

        public void setSummary(String mSummary) {
            this.mSummary = mSummary;
        }

        public String getAboutRaw() {
            return mAboutRaw;
        }

        public void setAboutRaw(String mAboutRaw) {
            this.mAboutRaw = mAboutRaw;
        }

        public String getAboutRendered() {
            return mAboutRendered;
        }

        public void setAboutRendered(String mAboutRendered) {
            this.mAboutRendered = mAboutRendered;
        }

        public String getAuthToken() {
            return mAuthToken;
        }

        public void setAuthToken(String mAuthToken) {
            this.mAuthToken = mAuthToken;
        }
    }


