package firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.IOException;
import java.io.InputStream;

public class FirebaseInitializer {

    // Google 이외의 환경에서 SDK 초기화
    // 만약 JAR 파일이나 다른 방식으로 애플리케이션을 배포한다면, 파일 시스템 상의 절대 경로는 사용되지 않을 수 있습니다
    // 클래스 로더를 통해 리소스를 스트림으로 로드하는 방법이 더 적합
    public void initializeFirebase() throws IOException {
        InputStream serviceAccountStream = getClass().getClassLoader().getResourceAsStream("tiki-taka-22f76-firebase-adminsdk-umsvj-e1428d776e.json");
        FirebaseOptions options = FirebaseOptions
                .builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                .build();

        FirebaseApp.initializeApp(options);
    }
}
