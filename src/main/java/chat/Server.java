package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final ServerSocket serverSocket;
    private ChatRoom chatRoom;
    // 여러 사용자의 연결 관리를 담당하는 java.chat.server.ChatRoom 인스턴스는 일반적으로 서버 애플리케이션의 중심 부분에 선언되어야 함

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        chatRoom = new ChatRoom();
        // 서버가 최초로 시작될때 ChatRoom이 만들어짐
    }

    public void startServer() throws IOException {
        // 클라이언트가 처음 연결을 시도할 때 보낸 userId를 기반으로 각 클라이언트의 java.chat.server.ChatHandler 인스턴스를 생성하고 관리

        //장점
        //1. 사용자 식별 :  userId를 통해 연결된 각 클라이언트를 구별
        //2. 유연한 메세지 처리 : userId를 사용하여 특정 사용자에게 메시지를 보내거나, 브로드캐스트하기에 종흠
        try {
            while (!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String userIdString = bufferedReader.readLine();
                int userId = Integer.parseInt(userIdString.trim());
                ChatHandler chatHandler = new ChatHandler(socket, userId, chatRoom);
                Thread thread = new Thread(chatHandler);
                thread.start();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}