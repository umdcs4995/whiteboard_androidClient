package com.umdcs4995.whiteboard.whiteboarddata;

/**
 * Created by LauraKrebs on 3/29/16.
 */
import java.util.ArrayList;

/**
 * Base class for API events that are posted to the otto bus.
 */
public abstract class ApiEvent<T> {
    /**
     * Proxy for APIEvents. Api posts these to the bus, BaseActivity
     * listens for them and posts the underlying ApiEvent to the bus if the
     * activityids match.
     */
    public static class ActivityProxy {
        protected ApiEvent<?> mApiEvent;

        public ActivityProxy(ApiEvent<?> apiEvent) {
            mApiEvent = apiEvent;
        }

        public ApiEvent<?> getApiEvent() {
            return mApiEvent;
        }

        public int getActivityid() {
            return mApiEvent.mApiCall.mActivityid;
        }
    }

    public static class Unauthorized extends ApiEvent<String> {}
    public static class TopicList extends ApiEvent<ArrayList<String>> {}

    public static class Login extends ApiEvent<User> {}
    public static class Logout extends ApiEvent<String> {}
    public static class Register extends ApiEvent<User> {}

    public static class UserImages extends ApiEvent<ArrayList<UserImage>> {}
    public static class UserInfo extends ApiEvent<User> {}
    public static class DeleteImage extends ApiEvent<String> {}

    public static class CopyImage extends ApiEvent<Image> {}
    public static class UploadImage extends ApiEvent<Image> {}
    public static class UploadStepImage extends ApiEvent<Image> {}



    public String mResponse;
    public T mResult;
    public ApiCall mApiCall;
    public com.umdcs4995.whiteboard.whiteboarddata.ApiError mError;
    public int mCode;

    /**
     * True iff response came from the offline storage.
     */
    public boolean mStoredResponse;

    public ApiEvent<T> setResult(T result) {
        mResult = result;
        return this;
    }

    public T getResult() {
        return mResult;
    }

    public String getExtraInfo() {
        return mApiCall.mExtraInfo;
    }

    public boolean hasError() {
        return mError != null;
    }

    public ApiEvent<T> setResponse(String response) {
        mResponse = response;
        return this;
    }

    public String getResponse() {
        return mResponse;
    }

    public ApiEvent<T> setCode(int code) {
        mCode = code;
        return this;
    }

    public com.umdcs4995.whiteboard.whiteboarddata.ApiError getError() {
        return mError;
    }

    public ApiEvent<T> setApiCall(ApiCall apiCall) {
        mApiCall = apiCall;
        return this;
    }

    public ApiEvent<T> setError(com.umdcs4995.whiteboard.whiteboarddata.ApiError error) {
        mError = error;
        return this;
    }

    public ApiEvent<T> setStoredResponse(boolean stored) {
        mStoredResponse = stored;
        return this;
    }
}
