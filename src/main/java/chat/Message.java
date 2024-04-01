package chat;

import com.google.gson.annotations.SerializedName;

public class Message {
    @SerializedName("message_id")
    private int messageId;

    @SerializedName("sender_id")
    private int senderId;

    @SerializedName("profile_image")
    private String profileImageUrl;
    // 클라이언트 상으로 는 Message 클래스에 프로필 이미지 필드를 가지고 있는것이 편리
    // (message 테이블에는 프로필 이미지 칼럼이 존재하지 않더라도)

    @SerializedName("room_id")
    private int chatRoomId;

    @SerializedName("content")
    private String content;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("date_marker")
    private int dateMarker;

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public int getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(int chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getDateMarker() {
        return dateMarker;
    }

    public void setDateMarker(int dateMarker) {
        this.dateMarker = dateMarker;
    }
}
