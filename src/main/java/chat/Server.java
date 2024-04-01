package chat;

import firebase.FirebaseInitializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final ServerSocket serverSocket;
    private ChatRoom chatRoom;
    // 여러 사용자의 연결 관리를 담당하는 chat.server.ChatRoom 인스턴스는 일반적으로 서버 애플리케이션의 중심 부분에 선언되어야 함

    public static void main(String[] args) throws IOException {
        initializeFirebase();
        // firebase 초기화 후에서 각 서비스에 대한 참조를 직접 얻어와서 사용해야 함
        // ex. FirebaseMessaging.getInstance().send(message)
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();

    }

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public static void initializeFirebase(){
        FirebaseInitializer firebaseInitializer = new FirebaseInitializer();
        try {
            firebaseInitializer.initializeFirebase();
            System.out.println("Firebase has been initialized.");
        } catch (IOException e) {
            System.err.println("Failed to initialize Firebase");
            throw new RuntimeException(e);
        }
    }

    public void startServer() throws IOException {

        try {
            ChatService chatService = new ChatService(DatabaseUtil.getConnection());
            // 싱글톤 패턴
            // 특정 클래스의 인스턴스가 애플리케이션 내에서 단 하나만 존재하도록 보장하는 디자인 패턴 (자원의 효율적 사용)
            chatRoom = new ChatRoom();
            // ChatRoom 인스턴스가 단 한 번만 생성되고, 이후 서버가 실행되는 동안 들어오는 모든 클라이언트 연결에 대해 동일한 ChatRoom 인스턴스가 사용됨

            while (!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String userIdString = bufferedReader.readLine();
                int userId = Integer.parseInt(userIdString.trim());
                // 클라이언트가 처음 연결을 시도할 때 보낸 userId를 기반으로 각 클라이언트의 java.chat.server.ChatHandler 인스턴스를 생성하고 관리
                // 장점
                //1. 사용자 식별 :  userId를 통해 연결된 각 클라이언트를 구별
                //2. 유연한 메세지 처리 : userId를 사용하여 특정 사용자에게 메시지를 보내거나, 브로드캐스트하기에 좋음
                ChatHandler chatHandler = new ChatHandler(socket, userId, chatRoom, chatService);
                Thread thread = new Thread(chatHandler);
                thread.start();
            }
        }catch (Exception e){
            e.printStackTrace();
        } 
    }

}