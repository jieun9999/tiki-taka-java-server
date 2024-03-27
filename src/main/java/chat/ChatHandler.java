package chat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.Socket;
import java.sql.*;


public class ChatHandler implements Runnable {
    // 서버에서 개별 클라이언트의 연결을 처리하고, 메세지를 브로드 캐스트 합니다.

    //1. 서버가 각 클라이언트 연결을 독립적으로 처리하고,
    //2. 채팅방에 있는 다른 사용자들에게 메시지를 브로드캐스팅 (전파)

    private Socket socket;
    int userId;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private ChatRoom chatRoom;
    //서버가 생성될때 가져오는 chatRoom

    public ChatHandler(Socket socket, int userId, ChatRoom chatRoom) throws IOException {
        this.socket = socket;
        this.userId = userId;
        this.chatRoom = chatRoom;
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // 해당 클라이언트의 접속 확인
        System.out.println("A new client has connected! userId"+ userId);

    }

    //thread.start();될때 실행된다
    @Override
    public void run() {

            try {
                //해당 클라이언트를 채팅방의 참여자 목록에 한번만 추가
                chatRoom.addChatHandler(this);

                // 클라이언트로부터 메시지를 지속적으로 수신하고 처리하는 로직
                String messageFromClient;
                while ((messageFromClient = bufferedReader.readLine())!= null){
                    // bufferedReader.readLine()은 입력 스트림으로부터 한줄의 텍스트를 읽어올때까지 블로킹(대기) 상태에 있습니다
                    // 클라이언트로부터 새로운 줄 바꿈 문자(예: \n 또는 \r\n)를 포함하는 문자열이 도착할 때까지 대기합니다
                    // 만약 클라이언트가 연결을 종료하거나 네트워크 문제 등으로 인해 연결이 끊어지면, readLine() 메서드는 null을 반환
                    handleClientMessage(messageFromClient);
                }
                //반복문을 빠져나왔다면, 소켓이 종료되었음을 의미
                System.out.println("사용자[" + userId + "]가 클라이언트 측 소켓을 종료했습니다.");

                // 참여자 중에 나간 사용자를 삭제한다
                chatRoom.removeChatHandler(this);
                //서버 소켓 연결도 종료
                closeConnection();
                System.out.println("사용자[" + userId + "]와 연결된 서버 측 소켓도 종료했습니다.");

            } catch (IOException e) {
                System.out.println("클라이언트 [" + userId + "]와의 연결에 문제가 발생했습니다.");

            }

    }

    private void handleClientMessage(String data){
        try {

            String savedMessage = parsingData(data);
            System.out.println("savedMessage" + savedMessage);
            broadcastMessage(savedMessage);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private String parsingData(String data) throws SQLException {
        JsonObject messageObject = JsonParser.parseString(data).getAsJsonObject();

        int senderId = messageObject.get("senderId").getAsInt();
        String message = messageObject.get("message").getAsString();
        int chatRoomId = messageObject.get("chatRoomId").getAsInt();

        String savedMessage = saveMessageToDbAndReturn(senderId, message, chatRoomId);
        return savedMessage;
    }

    //데이터베이스에 저장 (JDBC)
    private String saveMessageToDbAndReturn(int senderId, String message, int chatRoomId) throws SQLException {

        String insertSQL = "INSERT INTO message (sender_id, content, room_id) VALUES (?, ?, ?)";
        String selectSQL = "SELECT created_at, content FROM message WHERE message_id = LAST_INSERT_ID()";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement selectStmt = conn.prepareStatement(selectSQL)
        ){
            // INSERT 실행
            insertStmt.setInt(1, senderId);
            insertStmt.setString(2, message);
            insertStmt.setInt(3, chatRoomId);

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

        return insertSQL;
    }

    private void broadcastMessage(String savedMessage) throws IOException {

        if(savedMessage != null){
            chatRoom.broadcastMessage(savedMessage, this);
        }
    }

    public void sendMessage(String message) throws IOException{
        if(bufferedWriter != null){
            bufferedWriter.write(message + "\n"); // 메시지 끝에 개행 문자 추가
            bufferedWriter.flush();
        }
    }

    //서버 소켓 연결 종료 및 리소스 해제
    public void closeConnection() throws IOException {
        try{
            if(socket != null){
                socket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }




}