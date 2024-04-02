package chat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import firebase.FirebaseCloudMessageService;

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
    private ChatService service;
    // db에서 정보를 가져오는 요청

    public ChatHandler(Socket socket, int userId, ChatRoom chatRoom, ChatService service) throws IOException {
        this.socket = socket;
        this.userId = userId;
        this.chatRoom = chatRoom;
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.service = service;

        System.out.println("사용자[" + userId + "]가 서버 측 소켓에 연결되었습니다.");

    }

    //thread.start();될때 실행된다
    @Override
    public void run() {

        try {
                //해당 클라이언트를 채팅방의 참여자 목록에 한번만 추가
                chatRoom.addChatHandler(userId, this);

                // 클라이언트로부터 메시지를 지속적으로 수신하고 처리하는 로직
                String messageFromClient;
                while ((messageFromClient = bufferedReader.readLine())!= null){
                    // bufferedReader.readLine()은 입력 스트림으로부터 한줄의 텍스트를 읽어올때까지 블로킹(대기) 상태에 있습니다
                    // 클라이언트로부터 새로운 줄 바꿈 문자(예: \n 또는 \r\n)를 포함하는 문자열이 도착할 때까지 대기합니다
                    // 만약 클라이언트가 연결을 종료하거나 네트워크 문제 등으로 인해 연결이 끊어지면, readLine() 메서드는 null을 반환
                    handleSendingMessage(messageFromClient);
                }
                //반복문을 빠져나왔다면, 소켓이 종료되었음을 의미
                System.out.println("사용자[" + userId + "]가 클라이언트 측 소켓을 종료했습니다.");

                // 참여자 중에 나간 사용자를 삭제한다
                chatRoom.removeChatHandler(userId);
                //서버 소켓 연결도 종료
                closeConnection();
                System.out.println("사용자[" + userId + "]와 연결된 서버 측 소켓도 종료했습니다.");

            } catch (IOException e) {
                System.out.println("클라이언트 [" + userId + "]와의 연결에 문제가 발생했습니다.");

            }  catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handleSendingMessage(String data){
        try {
            JsonObject messageObject = JsonParser.parseString(data).getAsJsonObject();
            String type = messageObject.get("type").getAsString();
            System.out.println("클라이언트에서 받은 data" +data);
            String savedMessage = processDataBasedOnType(data);
            System.out.println("savedMessage" + savedMessage);

            // 메세지 type 2 : newMessage, readMessages
            // 채팅방에 참여자가 2명일 때에는 브로드캐스트, 1명만 있을 경우에는 FCM 전송
            int onlineUsers = chatRoom.getChatHandlersSize();

            if(type.equals("newMessage")){
                if(onlineUsers == 2){
                    broadcastMessage(savedMessage);

                } else if (onlineUsers == 1) {
                    System.out.println("메세지를 FCM 서버로 전달하는 로직을 추가");
                    String content = messageObject.get("message").getAsString();
                    int partnerId = messageObject.get("partnerId").getAsInt();

                    // 나간 사용자가 아니라 상대방의 userId로 fcmToken 가져오기
                    String fcmToken = service.selectFcmToken(partnerId);
                    // 메세지를 FCM 서버로 전달하는 로직을 추가
                    FirebaseCloudMessageService.sendMessage(fcmToken, content);
                }

            }else if(type.equals("readMessages")) {
                if(onlineUsers == 2){
                    broadcastMessage(savedMessage);

                } else if (onlineUsers == 1) {
                    // db 업데이트만 하면된다
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private String processDataBasedOnType(String data) throws SQLException {
        JsonObject messageObject = JsonParser.parseString(data).getAsJsonObject();
        String type = messageObject.get("type").getAsString();

        String savedMessage = null;
        if(type.equals("newMessage")){
            savedMessage = processAndSaveNewMessage(messageObject);

        }else if(type.equals("readMessages")){
            savedMessage = processAndSaveReadMessages(messageObject);
        }

        return savedMessage;
    }

    private String processAndSaveNewMessage(JsonObject messageObject) throws SQLException {
        int senderId = messageObject.get("senderId").getAsInt();
        String message = messageObject.get("message").getAsString();
        int chatRoomId = messageObject.get("chatRoomId").getAsInt();

        return service.saveMessageToDbAndReturn(senderId, message, chatRoomId, 0);
    }

    private String processAndSaveReadMessages(JsonObject messageObject) throws SQLException {
        int readerId = messageObject.get("readerId").getAsInt();
        String datetime = messageObject.get("dateTime").getAsString(); // String 타입으로 받음

        return service.updateMessageReadAndReturn(readerId, datetime, 1);
    }


    private void broadcastMessage(String savedMessage) throws IOException {

        if(savedMessage != null){
            chatRoom.broadcastMessage(savedMessage, userId);
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