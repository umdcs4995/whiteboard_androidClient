package com.umdcs4995.whiteboard.whiteboarddata;

/**
 * Created by LauraKrebs on 3/29/16.
 */

import android.util.Log;

import com.umdcs4995.whiteboard.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class JSONHelper {
    private static final String TAG = "JSONHelper";
    private static final String INVALID_LOGIN_STRING = "Invalid login";

//    public static SearchResults parseSearchResults(String json) throws JSONException {
//
//        SearchResults search = new SearchResults();
//        JSONObject response = new JSONObject(json);
//
//        search.mLimit = response.getInt("limit");
//        search.mOffset = response.getInt("offset");
//        search.mTotalResults = response.getInt("totalResults");
//        search.mHasMoreResults = response.getBoolean("moreResults");
//        search.mQuery = response.getString("search");
//
//        if (response.has("results")) {
//            JSONArray resultsArr = response.getJSONArray("results");
//            int count = resultsArr.length();
//
//            for (int i = 0; i < count; i++) {
//                JSONObject result = resultsArr.getJSONObject(i);
//                String resultType = result.getString("dataType");
//                if (resultType.equals("guide")) {
//                    Type guidesType = new TypeToken<GuideInfo>() {}.getType();
//                    GuideInfo gi = new Gson().fromJson(result.toString(), guidesType);
//
//                    GuideSearchResult gsr = new GuideSearchResult(gi);
//                    search.mResults.add(gsr);
//                } else if (resultType.equals("wiki")) {
//                    TopicSearchResult tsr = new TopicSearchResult();
//                    tsr.mDisplayTitle = result.getString("display_title");
//                    tsr.mTitle = result.getString("title");
//                    tsr.mText = result.getString("text");
//                    tsr.mNamespace = result.getString("namespace");
//                    tsr.mSummary = result.getString("summary");
//                    tsr.mUrl = result.getString("url");
//                    tsr.mImage = parseImage(result, "image");
//
//                    search.mResults.add(tsr);
//                }
//            }
//        }
//
//        return search;
//    }

    public static ArrayList<Site> parseSites(String json) throws JSONException {
        ArrayList<Site> sites = new ArrayList<Site>();

        JSONArray jSites = new JSONArray(json);
        Site site;

        for (int i = 0; i < jSites.length(); i++) {
            site = parseSite(jSites.getJSONObject(i));

            if (site != null) {
                sites.add(site);
            }
        }

        return sites;
    }

    private static Site parseSite(JSONObject jSite) throws JSONException {
        Site site = new Site(jSite.getInt("siteid"));

        site.mName = jSite.getString("name");
        site.mDomain = jSite.getString("domain");
        site.mCustomDomain = jSite.optString("custom_domain", "");
        site.mTitle = jSite.getString("title");
        site.mTheme = jSite.getString("theme");
        site.mPublic = !jSite.getBoolean("private");
        site.mDescription = jSite.getString("description");
        site.mAnswers = jSite.getBoolean("answers");
        site.mStoreUrl = jSite.optString("store", "");

        setAuthentication(site, jSite.getJSONObject("authentication"));

        return site;
    }

    public static Site parseSiteInfo(String json) throws JSONException {
        JSONObject siteInfoObject = new JSONObject(json);
        Site site = parseSite(siteInfoObject);

        JSONObject types = (JSONObject) siteInfoObject.get("guide-types");

        site.mObjectNamePlural = siteInfoObject.getString("object-name-plural");
        site.mObjectNameSingular = siteInfoObject.getString("object-name-singular");
        site.setBarcodeScanner(siteInfoObject.getBoolean("feature-mobile-scanner"));
        site.mGoogleOAuth2Clientid = siteInfoObject.getString("google-oauth2-clientid");

        if (!siteInfoObject.isNull("logo")) {
            JSONObject logoImage = siteInfoObject.getJSONObject("logo").getJSONObject("image");
            site.mLogo = new Image(logoImage.getInt("id"), logoImage.getString("original"));
        }

        Iterator<?> keys = types.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (types.get(key) instanceof JSONObject) {
            }
        }

        return site;
    }

    private static void setAuthentication(Site site, JSONObject jAuth) throws JSONException {
        site.mStandardAuth = jAuth.has("standard") && jAuth.getBoolean("standard");

        site.mSsoUrl = jAuth.has("sso") ? jAuth.getString("sso") : null;

        site.mPublicRegistration = jAuth.getBoolean("public-registration");
    }

    public static ArrayList<String> parseAllTopics(String json) {
        ArrayList<String> topics = new ArrayList<String>();

        try {
            JSONArray topicsJson = new JSONArray(json);

            for (int i = 0; i < topicsJson.length(); i++) {
                topics.add(topicsJson.getString(i));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing all topics list: ", e);
        }

        return topics;
    }


//    private static ArrayList<Comment> parseComments(JSONArray comments) throws JSONException {
//        ArrayList<Comment> result = new ArrayList<Comment>();
//        for (int i = 0; i < comments.length(); i++) {
//            result.add(new Comment(comments.getJSONObject(i)));
//        }
//        return result;
//    }

//    private static Item parsePart(JSONObject jPart) throws JSONException {
//        return new Item(
//                Item.ItemType.PART,
//                jPart.getString("text"),
//                jPart.getString("quantity"),
//                jPart.getString("url"),
//                jPart.getString("thumbnail"),
//                jPart.getString("notes"));
//    }

//    private static Item parseTool(JSONObject jTool) throws JSONException {
//        return new Item(
//                Item.ItemType.TOOL,
//                jTool.getString("text"),
//                jTool.getString("quantity"),
//                jTool.getString("url"),
//                jTool.getString("thumbnail"),
//                jTool.getString("notes"));
//    }
//
//
//    private static Video parseVideo(JSONObject jVideo) throws JSONException {
//        Video video = new Video();
//
//        try {
//            JSONArray jEncodings = jVideo.getJSONArray("encodings");
//
//            int numEncodings = jEncodings.length();
//            for (int i = 0; i < numEncodings; i++) {
//                video.addEncoding(parseVideoEncoding(jEncodings.getJSONObject(i)));
//            }
//
//            video.setHeight(jVideo.getInt("width"));
//            video.setWidth(jVideo.getInt("height"));
//            video.setDuration(jVideo.getInt("duration"));
//            video.setFilename(jVideo.getString("filename"));
//            video.setThumbnail(parseVideoThumbnail(jVideo.getJSONObject("image")));
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Log.e("JSONHelper parseVideo", "Error parsing video API response");
//        }
//
//        return video;
//    }
//
//    private static VideoThumbnail parseVideoThumbnail(JSONObject jVideoThumb) throws JSONException {
//
//        Image image = parseImage(jVideoThumb.getJSONObject("image"), null);
//
//        String ratio = jVideoThumb.getString("ratio");
//        int width = jVideoThumb.getInt("width");
//        int height = jVideoThumb.getInt("height");
//
//        return new VideoThumbnail(image.getId(), image.getPath(), width, height);
//    }
//
//    private static VideoEncoding parseVideoEncoding(JSONObject jVideoEncoding) throws JSONException {
//        return new VideoEncoding(jVideoEncoding.getInt("width"), jVideoEncoding.getInt("height"),
//                jVideoEncoding.getString("url"), jVideoEncoding.getString("format"));
//    }

    /**
     * Parsing list of UserImageInfo.
     */
    public static ArrayList<UserImage> parseUserImages(String json) throws JSONException {
        JSONArray jImages = new JSONArray(json);

        ArrayList<UserImage> userImageList = new ArrayList<UserImage>();

        for (int i = 0; i < jImages.length(); i++) {
            userImageList.add(parseUserImage(jImages.getJSONObject(i)));
        }

        return userImageList;
    }

    private static UserImage parseUserImage(JSONObject jImage) throws JSONException {
        JSONObject img = jImage.getJSONObject("image");

        int id = img.getInt("id");
        int width = jImage.getInt("width");
        int height = jImage.getInt("height");
        String path = img.getString("original");
        String ratio = jImage.getString("ratio");
        String markup = jImage.isNull("markup") ? "" : jImage.getString("markup");
        // TODO: Add exif.
        String exif = "";

        return new UserImage(id, path, width, height, ratio, markup, exif);
    }

/*
   public static UserEmbedInfo parseUserEmbedInfo(JSONObject jEmbed)
    throws JSONException {
      UserEmbedInfo userEmbedInfo = new UserEmbedInfo();
//        userEmbedInfo.setItemId(jEmbed.getString("imageid"));
//        userEmbedInfo.setGuid(jEmbed.getString("guid"));
//        userEmbedInfo.setHeight(jEmbed.getString("height"));
//        userEmbedInfo.setWidth(jEmbed.getString("width"));
//        userEmbedInfo.setRatio(jEmbed.getString("ratio"));
      return userEmbedInfo;
   }
*/

    public static Image parseUploadedImage(String image) throws JSONException {
        return parseImage(new JSONObject(image), "image");
    }

    /**
     * Login parsing info
     */
    public static User parseLoginInfo(String json) throws JSONException {
        JSONObject jUser = new JSONObject(json);

        User user = new User();
        user.setUserid(jUser.getInt("userid"));
        user.setUsername(jUser.getString("username"));

//        if (!jUser.isNull("image"))
//            user.setAvatar(parseImage(jUser.getJSONObject("image"), null));

        user.setAboutRaw(jUser.getString("about_raw"));
        user.setAboutRendered(jUser.getString("about_rendered"));

        if (!jUser.isNull("summary"))
            user.setSummary(jUser.getString("summary"));
//        if (!jUser.isNull("location"))
//            user.setLocation(jUser.getString("location"));
        if (!jUser.isNull("join_date"))
            user.setJoinDate(jUser.getInt("join_date"));

        user.setReputation(jUser.getInt("reputation"));
        user.setCertificationCount(jUser.getInt("certification_count"));
        user.setAuthToken(jUser.getString("authToken"));

        return user;
    }

    public static User parseUserLight(JSONObject jUser) throws JSONException {
        User user = new User();
        user.setUserid(jUser.getInt("userid"));
        user.setUsername(jUser.getString("username"));
//        user.setAvatar(parseImage(jUser, "image"));

        if (!jUser.isNull("join_date"))
            user.setJoinDate(jUser.getInt("join_date"));

        user.setReputation(jUser.getInt("reputation"));
        return user;
    }


    /**
     * Returns the ApiError contained in the given JSON, or null if one
     * does not exist.
     * <p/>
     * e.g. Returns "Guide not found" for:
     * "{"error":true,"msg":"Guide not found"}"
     */
    public static ApiError parseError(String json, int code) {
        ApiError error = null;
        String message, title;
        ApiError.Type type;
        App app = App.get();

        try {
            JSONObject jError = new JSONObject(json);

            message = jError.getString("message");

            type = message.equals(INVALID_LOGIN_STRING) ?
                    ApiError.Type.INVALID_USER :
                    ApiError.getByStatusCode(code).mType;

            if (type == ApiError.Type.VALIDATION) {
                error = JSONHelper.parseValidationError(json);
            } else {
                title = app.getString(R.string.error); // Default error string
                error = new ApiError(title, message, type);
            }

        } catch (JSONException e) {
            Log.e("JSONHelper", "Unable to parse error message");
        }

        return error;
    }

    public static ApiError parseValidationError(String json) {
        String message;
        int index = -1;
        ApiError error = null;

        try {
            JSONObject jError = new JSONObject(json);
            JSONArray jErrors = jError.getJSONArray("errors");

            message = jError.getString("message") + ".";
            for (int i = 0; i < jErrors.length(); i++) {
                message += "  " + ((JSONObject)jErrors.get(i)).getString("message");
                index = ((JSONObject)jErrors.get(i)).optInt("index", -1);
            }
            error = new ApiError(
                    App.get().getString(R.string.validation_error_title),
                    message,
                    ApiError.Type.VALIDATION,
                    index);
        } catch (JSONException e) {
            Log.e("JSONHelper", "Unable to parse error message");
        }

        return error;
    }

    public static JSONArray createImageArray(ArrayList<Image> images) throws JSONException {

        JSONArray array = new JSONArray();

        for (Image image : images) {
            array.put(image.getId());
        }
        return array;
    }

    public static Image parseImage(JSONObject image, String imageFieldName) {
        try {
            if (imageFieldName != null) {
                image = image.optJSONObject(imageFieldName);
            }

            if (image == null) {
                return new Image();
            }

            return new Image(image.getInt("id"), image.getString("original"));
        } catch (JSONException e) {
            Log.w(TAG, "Image parsing", e);
            return new Image();
        }
    }

}
