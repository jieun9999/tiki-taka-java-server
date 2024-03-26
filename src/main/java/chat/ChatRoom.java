package chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatRoom {

    // 클라이언트 측 코드: 채팅방을 생성할때 필요하니 dto 측면에서 ChatRoom이 필요하고,
    // 서버측 코드: 모든 사용자핸들러를 관리하기 위함, 또한 브로드 캐스팅을 위해서 ChatRoom이 에도 필요하구나?
    private List<ChatHandler> chatHandlers = new ArrayList<>();
        // 채팅방의 모든 사용자 관리

       public synchronized void addChatHandler(ChatHandler chatHandler){
           chatHandlers.add(chatHandler);

       }


        public synchronized void broadcastMessage(String message, ChatHandler sender) throws IOException {
        for (ChatHandler chatHandler : chatHandlers){
            if(chatHandler != sender){
                chatHandler.sendMessage(message);
                System.out.println(message);
            }
        }
    }
}
