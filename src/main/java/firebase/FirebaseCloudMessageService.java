package firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

public class FirebaseCloudMessageService {

    public static void sendMessage(String registrationToken, String messageData) throws Exception {
        // messageData 파싱하기

        // 알림 메세지
        // 어플리케이션이 백그라운드나 종료 상태에 있을때, 시스템이 알림을 자동으로 처리한다는 점이 장점!
        // 포그라운드에서는 직접 처리
        Message message = Message.builder() // 메시지 구성을 시작할 수 있는 Builder 객체가 반환
                .setNotification(Notification.builder()
                        .setTitle("메세지")
                        .setBody(messageData)
                        .build())
                .setToken(registrationToken) // 메서드는 메시지의 수신자를 지정
                .build(); // 최종 Message 객체를 생성

        // 메시지 보내기
        String response = FirebaseMessaging.getInstance().send(message);
        System.out.println("Successfully sent message: " + response);
    }
}

