package com.umdcs4995.whiteboard.whiteboarddata;

/**
 * Created by LauraKrebs on 3/29/16.
 */

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Defines an ApiCall that can be performed using Api.call().
 */
public class ApiCall {
    private static final String NO_QUERY = "";

    protected com.umdcs4995.whiteboard.whiteboarddata.ApiEndpoint mEndpoint;
    protected String mQuery;
    protected String mRequestBody;
    protected String mExtraInfo;
    protected String mFilePath;
    protected String mAuthToken;
    protected Site mSite;
    protected User mUser;
    protected int mActivityid = -1;

    public ApiCall(com.umdcs4995.whiteboard.whiteboarddata.ApiEndpoint endpoint, String query) {
        this(endpoint, query, null);
    }

    public ApiCall(com.umdcs4995.whiteboard.whiteboarddata.ApiEndpoint endpoint, String query, String requestBody) {
        this(endpoint, query, requestBody, null);
    }

    public ApiCall(com.umdcs4995.whiteboard.whiteboarddata.ApiEndpoint endpoint, String query, String requestBody,
                   String extraInfo) {
        this(endpoint, query, requestBody, extraInfo, null);
    }

    public ApiCall(com.umdcs4995.whiteboard.whiteboarddata.ApiEndpoint endpoint, String query, String requestBody,
                   String extraInfo, String filePath) {
        mEndpoint = endpoint;
        mQuery = query;
        mRequestBody = requestBody;
        mExtraInfo = extraInfo;
        mFilePath = filePath;
    }

    public void updateUser(User user) {
        mUser = user;

        if (mUser != null) {
            mAuthToken = mUser.getAuthToken();
        } else {
            mAuthToken = null;
        }
    }

    public String getQuery() {
        return mQuery;
    }

    /**
     * ApiCall Factory methods.
     */


    public static ApiCall userInfo(String authToken) {
        ApiCall apiCall = new ApiCall(com.umdcs4995.whiteboard.whiteboarddata.ApiEndpoint.USER_INFO, NO_QUERY);

        apiCall.mAuthToken = authToken;

        return apiCall;
    }

    public static ApiCall register(String email, String password, String username) {
        JSONObject requestBody = new JSONObject();

        try {
            requestBody.put("email", email);
            requestBody.put("password", password);
            requestBody.put("username", username);
        } catch (JSONException e) {
            return null;
        }

        return new ApiCall(com.umdcs4995.whiteboard.whiteboarddata.ApiEndpoint.REGISTER, NO_QUERY, requestBody.toString());
    }

    public static ApiCall login(String email, String password) {
        JSONObject requestBody = new JSONObject();

        try {
            requestBody.put("email", email);
            requestBody.put("password", password);
        } catch (JSONException e) {
            return null;
        }

        return new ApiCall(com.umdcs4995.whiteboard.whiteboarddata.ApiEndpoint.LOGIN, NO_QUERY, requestBody.toString());
    }

    public static ApiCall googleOauthLogin(String oauthCode) {
        JSONObject requestBody = new JSONObject();

        try {
            requestBody.put("google_oauth_code", oauthCode);
        } catch (JSONException e) {
            return null;
        }

        return new ApiCall(ApiEndpoint.LOGIN, NO_QUERY, requestBody.toString());
    }

    public static ApiCall logout(User user) {
        ApiCall apiCall = new ApiCall(ApiEndpoint.LOGOUT, NO_QUERY);

        // Override the authToken because the user won't be logged in by the time
        // the request is performed.
        apiCall.mAuthToken = user.getAuthToken();

        return apiCall;
    }


//    private static JSONObject guideBundleToRequestBody(Bundle bundle) {
//        JSONObject requestBody = new JSONObject();
//        App app = App.get();
//        try {
//
//            String subjectKey = GuideIntroWizardModel.HAS_SUBJECT_KEY + ":"
//                    + app.getString(R.string.guide_intro_wizard_guide_subject_title);
//            if (bundle.containsKey(subjectKey)) {
//                requestBody.put("subject", bundle.getBundle(subjectKey).getString(EditTextPage.TEXT_DATA_KEY));
//            }
//
//            String introductionKey = app.getString(R.string.guide_intro_wizard_guide_introduction_title);
//            if (bundle.containsKey(introductionKey)) {
//                requestBody.put("introduction", bundle.getBundle(introductionKey).getString(EditTextPage.TEXT_DATA_KEY));
//            }
//
//            String summaryKey = app.getString(R.string.guide_intro_wizard_guide_summary_title);
//            if (bundle.containsKey(summaryKey)) {
//                requestBody.put("summary", bundle.getBundle(summaryKey).getString(EditTextPage.TEXT_DATA_KEY));
//            }
//
//        } catch (JSONException e) {
//            return null;
//        }
//
//        return requestBody;
//    }


    public static ApiCall copyImage(String query) {
        return new ApiCall(ApiEndpoint.COPY_IMAGE, query);
    }

    public static ApiCall userImages(String query) {
        return new ApiCall(ApiEndpoint.USER_IMAGES, query);
    }

//    public static ApiCall userVideos(String query) {
//        return new ApiCall(ApiEndpoint.USER_VIDEOS, query);
//    }

    public static ApiCall uploadImage(String filePath, String extraInformation) {
        return new ApiCall(ApiEndpoint.UPLOAD_IMAGE, filePath, null, extraInformation,
                filePath);
    }

    public static ApiCall deleteImage(List<Integer> deleteList) {
        StringBuilder stringBuilder = new StringBuilder();
        String separator = "";

        stringBuilder.append("?imageids=");

        /**
         * Construct a string of imageids separated by comma's.
         */
        for (Integer imageid : deleteList) {
            stringBuilder.append(separator).append(imageid);
            separator = ",";
        }

        return new ApiCall(ApiEndpoint.DELETE_IMAGE, stringBuilder.toString());
    }

    public static ApiCall allTopics() {
        return new ApiCall(ApiEndpoint.ALL_TOPICS, NO_QUERY);
    }

//    public static ApiCall newComment(String comment, String context, int contextid) {
//        return newComment(comment, context, contextid, -1);
//    }

//    public static ApiCall newComment(String comment, String context, int contextid, int parentid) {
//        JSONObject requestBody = new JSONObject();
//        String query = "/" + context + "/" + contextid;
//
//        try {
//            requestBody.put("text", comment.trim());
//
//            if (parentid != -1) {
//                requestBody.put("parentid", parentid);
//            }
//        } catch (JSONException e) {
//            return null;
//        }
//
//        return new ApiCall(ApiEndpoint.ADD_COMMENT, query, requestBody.toString());
//    }

//    public static ApiCall editComment(String text, int commentid) {
//        JSONObject requestBody = new JSONObject();
//
//        try {
//            requestBody.put("text", text.trim());
//        } catch (JSONException e) {
//            return null;
//        }
//
//        return new ApiCall(ApiEndpoint.EDIT_COMMENT, "/" + commentid, requestBody.toString());
//    }
//
//    public static ApiCall deleteComment(int commentid) {
//        return new ApiCall(ApiEndpoint.DELETE_COMMENT, "/" + commentid, null, commentid + "");
//    }
}
