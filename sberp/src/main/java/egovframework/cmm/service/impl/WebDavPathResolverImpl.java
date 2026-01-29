package egovframework.cmm.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import egovframework.cmm.service.NextcloudDavService;
import egovframework.cmm.service.WebDavPathResolver;
import lombok.extern.slf4j.Slf4j;

@Service("WebDavPathResolver")
@Slf4j
public class WebDavPathResolverImpl implements WebDavPathResolver {

    private final NextcloudDavService nextcloudDavService;
    private static final String ERP_ROOT = "/ERP";

    public WebDavPathResolverImpl(
            @Qualifier("NextcloudDavService") NextcloudDavService nextcloudDavService
    ) {
        this.nextcloudDavService = nextcloudDavService;
    }

    @Override
    public String resolveIfCode(String pathOrCode) throws Exception {
        log.info("resolveIfCode 호출됨. 입력값: [{}]", pathOrCode); // <-- 가장 먼저 확인해야 할 로그
        
        String v = normalize(pathOrCode);
        if (v.isEmpty()) return v;

        // 이미 경로가 완성되어 있다면 탐색 생략 (ERP/20xx 구조)
        if (v.contains("/ERP/20")) {
            return v;
        }

        // 앞뒤 슬래시를 모두 떼어낸 순수 코드값 생성
        String pureCode = v.replace(ERP_ROOT, "").replace("/", "").trim();
        
        // 정규식 완화: SB로 시작하고 뒤에 숫자가 오는 모든 경우 시도
        if (pureCode.startsWith("SB")) { 
            log.info("SB 코드 패턴 감지됨: [{}], 탐색 시작...", pureCode);
            try {
                return findFullPathByCode(pureCode);
            } catch (Exception e) {
                log.warn("탐색 실패: {}. 입력값 그대로 반환합니다.", e.getMessage());
                return v; 
            }
        }

        log.info("SB 코드가 아니라고 판단됨. 그대로 반환: {}", v);
        return v;
    }

    private String findFullPathByCode(String code) throws Exception {
        int year = parseYear(code); 
        String yearPath = ERP_ROOT + "/" + year;

        // 월 후보군 (Nextcloud 폴더 구조에 맞춰 01, 1 모두 시도)
        List<String> monthCandidates = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            monthCandidates.add(String.format("%02d", m)); 
            monthCandidates.add(String.valueOf(m));        
        }

        for (String mm : monthCandidates) {
            String candidate = yearPath + "/" + mm + "/" + code;
            
            // 디버깅을 위해 시도 중인 경로를 로그로 남깁니다.
            log.debug("Checking WebDAV path: {}", candidate);
            
            if (existsDirectory(candidate)) {
                log.info("탐색 성공! 최종 경로: {}", candidate);
                return candidate;
            }
        }

        throw new IllegalArgumentException("WebDAV 서버 내에 코드가 존재하지 않음: " + code);
    }

    private boolean existsDirectory(String fullPath) {
        try {
            // list 호출 시 경로 끝에 /가 없어도 WebDAV 라이브러리가 처리하지만, 
            // 확실하게 하기 위해 directory 존재 여부만 체크
            nextcloudDavService.list(fullPath, 1);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isSbCode(String s) {
        // 정규식: SB + 숫자2자리 + - + 나머지
        return s != null && s.matches("^SB\\d{2}-[A-Za-z0-9]+$");
    }

    private int parseYear(String code) {
        // "SB25-G1583" -> "25" -> 2025
        String yyStr = code.substring(2, 4);
        return 2000 + Integer.parseInt(yyStr);
    }

    private String normalize(String s) {
        if (s == null) return "";
        String v = s.trim();
        // 앞뒤 공백 제거 및 마지막 슬래시 제거
        if (v.endsWith("/") && v.length() > 1) {
            v = v.substring(0, v.length() - 1);
        }
        return v;
    }
}