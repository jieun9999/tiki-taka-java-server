package chat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.sql.*;

public class ChatService {
    private Connection connection;

    public ChatService(Connection connection) {
        this.connection = connection;
    }

    // 데이터베이스에 메세지 저장 (JDBC)
    public String saveMessageToDbAndReturn(int senderId, String message, int chatRoomId, int isRead) throws SQLException {

        String insertSQL = "INSERT INTO message (sender_id, content, room_id, is_read) VALUES (?, ?, ?, ?)";
        String selectSQL = "SELECT m.*, u.profile_image, u.name FROM message AS m LEFT JOIN userProfile AS u ON m.sender_id = u.user_id WHERE message_id = LAST_INSERT_ID()";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement selectStmt = conn.prepareStatement(selectSQL)
        ) {
            // INSERT 실행
            insertStmt.setInt(1, senderId);
            insertStmt.setString(2, message);
            insertStmt.setInt(3, chatRoomId);
            insertStmt.setInt(4, isRead);

            int affectedRows = insertStmt.executeUpdate();
            //executeUpdate(): INSERT, UPDATE, DELETE와 같이 데이터베이스의 내용을 변경
            // 영향 받은 행(row)의 수를 정수로 반환

            if (affectedRows > 0) {
                System.out.println("메세지 저장 성공");

                // 삽입된 메세지에 대한 추가 정보 조회
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        //ResultSet 객체에서 첫번째 행으로 이동
                        int messageId = rs.getInt("message_id");
                        int roomId = rs.getInt("room_id");
                        String name = rs.getString("name");
                        String userProfile = rs.getString("profile_image");
                        String createdAt = rs.getString("created_at");
                        String content = rs.getString("content");

                        JsonObject messageObject = new JsonObject();
                        messageObject.addProperty("type", "newMessage");
                        messageObject.addProperty("messageId", messageId);
                        messageObject.addProperty("roomId", roomId);
                        messageObject.addProperty("senderId", senderId);
                        messageObject.addProperty("name", name);
                        messageObject.addProperty("userProfile", userProfile);
                        messageObject.addProperty("createdAt", createdAt);
                        messageObject.addProperty("content", content);

                        // Gson을 사용하여 JsonObject를 JSON 문자열로 변환
                        return new Gson().toJson(messageObject);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else {
                System.out.println("메세지 저장 실패 : 영향 받은 행 없음");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("데이터베이스 에러: " + e.getMessage());
        }

        return null;
    }

    public String selectFcmToken(int userId) throws SQLException {

        String selectSQL = "SELECT token FROM fcmToken WHERE user_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSQL)
        ) {
            selectStmt.setInt(1, userId);
            // 1번째 파라미터(?)에 userId 값을 설정
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {

                    // 첫 번째 결과의 token 컬럼 값을 반환
                    return rs.getString("token");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("데이터베이스 에러: " + e.getMessage());
        }
        return null;
    }

    public String updateMessageReadAndReturn(int readerId, String datetime, int isRead) {
        // 사용자가 메시지를 읽은 시점에서 본 가장 최근 메시지까지 is_read 를 1로 변경함
        String updateSql = "UPDATE message SET is_read = ? WHERE message.created_at <= ? AND (sender_id != ? OR sender_id IS NULL)";
        // 업데이트된 행의 수
        int updatedRows = 0;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            updateStmt.setInt(1, isRead);
            updateStmt.setString(2, datetime);
            updateStmt.setInt(3, readerId);
            updatedRows = updateStmt.executeUpdate();

            //  사용자가 보낸 메시지를 제외하고 읽음 처리된 메시지 중 가장 최신의 message_id를 찾아냄
            if (updatedRows > 0) {
                String selectSql = "SELECT MAX(message_id) AS maxMessageId FROM message WHERE sender_id != ? AND is_read = 1";
                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setInt(1, readerId);
                    ResultSet rs = selectStmt.executeQuery();

                    if (rs.next()) {
                        int lastReadMessageId = rs.getInt("maxMessageId");
                        System.out.println("maxMessageId" + lastReadMessageId);
                        JsonObject responseJson = new JsonObject();
                        responseJson.addProperty("type", "readMessages");
                        responseJson.addProperty("lastReadMessageId", lastReadMessageId);
                        return responseJson.toString();
                    }else {
                        System.out.println("rs가 안찍힘");
                    }
                }
            }else {
                System.out.println("no update row");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
