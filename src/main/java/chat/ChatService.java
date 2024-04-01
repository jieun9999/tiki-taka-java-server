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
        String selectSQL = "SELECT created_at, content FROM message WHERE message_id = LAST_INSERT_ID()";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement selectStmt = conn.prepareStatement(selectSQL)
        ){
            // INSERT 실행
            insertStmt.setInt(1, senderId);
            insertStmt.setString(2, message);
            insertStmt.setInt(3, chatRoomId);
            insertStmt.setInt(4, isRead);

            int affectedRows = insertStmt.executeUpdate();
            //executeUpdate(): INSERT, UPDATE, DELETE와 같이 데이터베이스의 내용을 변경
            // 영향 받은 행(row)의 수를 정수로 반환

            if(affectedRows > 0){
                System.out.println("메세지 저장 성공");

                //삽입된 메세지에 대한 추가 정보 조회
                try(ResultSet rs = selectStmt.executeQuery()){
                    if(rs.next()){
                        //ResultSet 객체에서 첫번째 행으로 이동
                        String createdAt = rs.getString("created_at");
                        String content = rs.getString("content");

                        JsonObject messageObject = new JsonObject();
                        messageObject.addProperty("type", "newMessage");
                        messageObject.addProperty("createdAt", createdAt);
                        messageObject.addProperty("content", content);

                        // Gson을 사용하여 JsonObject를 JSON 문자열로 변환
                        return new Gson().toJson(messageObject);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }


            }else {
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
        ){
            selectStmt.setInt(1, userId);
            // 1번째 파라미터(?)에 userId 값을 설정
            try(ResultSet rs = selectStmt.executeQuery()){
                if(rs.next()){

                    // 첫 번째 결과의 token 컬럼 값을 반환
                    return rs.getString("token");
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("데이터베이스 에러: " + e.getMessage());
        }
        return null;
    }

    public String updateMessageReadAndReturn(int readerId, int lastReadMessageId, int isRead){
        String updateSql = "UPDATE message SET is_read = ? WHERE message_id <= ? AND (sender_id != ? OR sender_id IS NULL)";
        int updatedRows = 0; // 업데이트된 행의 수를 저장할 변수

        try(Connection conn = DatabaseUtil.getConnection();
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
        ){
            updateStmt.setInt(1, isRead);
            updateStmt.setInt(2, lastReadMessageId);
            updateStmt.setInt(3, readerId);

            updatedRows = updateStmt.executeUpdate();

        }catch (Exception e){
            e.printStackTrace();
        }

        if (updatedRows > 0) {
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("type", "readMessage");
            responseJson.addProperty("readerId", readerId);
            responseJson.addProperty("lastReadMessageId", lastReadMessageId);

            return responseJson.toString();

        } else {
            return null;
        }

    }
}
