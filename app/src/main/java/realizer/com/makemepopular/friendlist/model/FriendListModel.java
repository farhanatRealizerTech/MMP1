package realizer.com.makemepopular.friendlist.model;

/**
 * Created by Win on 12/01/2017.
 */
public class FriendListModel
{
    String friendName="";
    String UserId="";
    boolean istracking=false;
    boolean ismessaging=false;
    String TrackingDate="";
    String CreateTS="";
    String TrackingStatusChangeDt="";
    boolean isEmergency=false;
    String friendId="";
    String thumbnailUrl="";
    int frndPositio=0;
    String status="";
    boolean isSentRequest=false;


    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public boolean istracking() {
        return istracking;
    }

    public void setIstracking(boolean istracking) {
        this.istracking = istracking;
    }

    public boolean ismessaging() {
        return ismessaging;
    }

    public void setIsmessaging(boolean ismessaging) {
        this.ismessaging = ismessaging;
    }

    public String getTrackingDate() {
        return TrackingDate;
    }

    public void setTrackingDate(String trackingDate) {
        TrackingDate = trackingDate;
    }

    public String getCreateTS() {
        return CreateTS;
    }

    public void setCreateTS(String createTS) {
        CreateTS = createTS;
    }

    public String getTrackingStatusChangeDt() {
        return TrackingStatusChangeDt;
    }

    public void setTrackingStatusChangeDt(String trackingStatusChangeDt) {
        TrackingStatusChangeDt = trackingStatusChangeDt;
    }

    public boolean isEmergency() {
        return isEmergency;
    }

    public void setIsEmergency(boolean isEmergency) {
        this.isEmergency = isEmergency;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public int getFrndPositio() {
        return frndPositio;
    }

    public void setFrndPositio(int frndPositio) {
        this.frndPositio = frndPositio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSentRequest() {
        return isSentRequest;
    }

    public void setSentRequest(boolean isSentRequest) {
        this.isSentRequest = isSentRequest;
    }
}
