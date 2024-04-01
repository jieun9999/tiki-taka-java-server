package chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoom {

    // 클라이언트 측 코드: 채팅방을 생성할때 필요하니 dto 측면에서 ChatRoom이 필요하고,
    // 서버측 코드: 모든 사용자핸들러를 관리하기 위함, 또한 브로드 캐스팅을 위해서 ChatRoom이 에도 필요함
    private ConcurrentHashMap<Integer, ChatHandler> chatHandlers = new ConcurrentHashMap<>();
        // 채팅방의 모든 사용자 관리
        // ConcurrentHashMap 자체가 동시성을 처리하기 위해 설계
        // 자동 동기화: ConcurrentHashMap은 내부적으로 동기화를 처리하기 때문에, 개발자가 명시적으로 synchronized 블록을 사용하여 동기화를 관리할 필요가 x
        // 동시 읽기 작업에 대해 락을 걸지 않으며, 쓰기 작업에 대해서는 최소한의 락만 사용
       public void addChatHandler(Integer userId, ChatHandler chatHandler){
           chatHandlers.put(userId, chatHandler);

       }

       public void removeChatHandler(Integer userId){
           chatHandlers.remove(userId);

       }

        public void broadcastMessage(String message, Integer senderUserId) throws IOException {
        for (Integer userId : chatHandlers.keySet()){
            if(!userId.equals(senderUserId)){
                ChatHandler chatHandler = chatHandlers.get(userId);
                chatHandler.sendMessage(message);
            }
        }
    }

        public int getChatHandlersSize(){
           return  chatHandlers.size();
        }
}
