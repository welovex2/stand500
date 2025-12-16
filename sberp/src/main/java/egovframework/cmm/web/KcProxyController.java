package egovframework.cmm.web;

import java.net.URI;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/kc") // ✅ 프런트와 동일 경로로 변경 (/api/kc/cert, /api/kc/detail)
public class KcProxyController {

    // ✅ application.properties(yaml)에 설정하세요:
    // safetykorea.authkey=fdef06b1-e480-4e0f-9bed-6033b0c906df
    @Value("fdef06b1-e480-4e0f-9bed-6033b0c906df")
    private String authKey;

    private static final String LIST_API   = "http://www.safetykorea.kr/openapi/api/cert/certificationList.json";
    private static final String DETAIL_API = "http://www.safetykorea.kr/openapi/api/cert/certificationDetail.json";

    private final ObjectMapper om = new ObjectMapper();
    private final RestTemplate rest = new RestTemplate(); // 필요시 Timeout/Pooling 설정

    @GetMapping(value = "/cert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "all") String conditionKey,
            @RequestParam(defaultValue = "") String conditionValue
    ) {
        // ✅ build(true) 제거, .encode() 사용 (Spring 5.x 이상)
        URI uri = UriComponentsBuilder.fromHttpUrl(LIST_API)
                .queryParam("conditionKey", conditionKey)
                .queryParam("conditionValue", conditionValue)
                .build()
                .encode() // 기본 UTF-8
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("AuthKey", authKey);

        ResponseEntity<String> upstream =
                rest.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        String body = upstream.getBody();

        try {
            Object json = om.readValue(body, Object.class);
            return ResponseEntity
                    .status(upstream.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        } catch (Exception e) {
            return ResponseEntity
                    .status(upstream.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);
        }
    }

    @GetMapping(value = "/detail", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> detail(@RequestParam String certNum) {
        URI uri = UriComponentsBuilder.fromHttpUrl(DETAIL_API)
                .queryParam("certNum", certNum)
                .build()
                .encode() // 기본 UTF-8 인코딩
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("AuthKey", authKey);

        ResponseEntity<String> upstream =
                rest.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        String body = upstream.getBody();

        try {
            Object json = om.readValue(body, Object.class);
            return ResponseEntity
                    .status(upstream.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        } catch (Exception e) {
            return ResponseEntity
                    .status(upstream.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);
        }
    }
}
