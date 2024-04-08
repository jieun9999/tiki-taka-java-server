package firebase;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

public class FirebaseCloudMessageService {

    // 알림 메시지와 데이터 페이로드 포함
    public static void sendMessage(String registrationToken, String content, int messageId, int roomId, String name, String userProfile) throws Exception {

        // 데이터 메세지
        // 포그라운드, 백그라운드에서는 직접 일관되게 처리
        Message message = Message.builder() // 메시지 구성을 시작할 수 있는 Builder 객체가 반환
                .putData("flag", "chat_notification")
                .putData("title", name) // 알림 제목을 데이터 페이로드에 추가
                .putData("userProfile", userProfile)
                .putData("body", content) // 메시지 본문을 데이터 페이로드에 추가
                .putData("messageId", String.valueOf(messageId))// 메세지 id를 데이터 페이로드에 추가
                .putData("roomId", String.valueOf(roomId)) // room id를 데이터 페이로드에 추가
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH) // 우선 순위를 HIGH로 설정
                        .build())
                .setToken(registrationToken) // 메시지의 수신자를 지정
                .build(); // 최종 Message 객체를 생성

        // 메시지 보내기
        FirebaseMessaging.getInstance().send(message);
        System.out.println("Successfully sent message: " + message);
    }
}

