package egovframework.cmm.service;

public class ResponseMessage {
    public static final String LOGIN_SUCCESS = "로그인 성공";
    public static final String LOGIN_FAIL = "아이디나 비밀번호가 맞지 않습니다. 다시 확인해주세요."; 
    public static final String LOGIN_LOCK = "계정잠김";
    public static final String NO_LOGIN = "로그인정보가 없습니다";
    public static final String UNAUTHORIZED = "권한이 없습니다";
    
    public static final String READ_USER = "회원 정보 조회 성공";
    public static final String NOT_FOUND_USER = "회원을 찾을 수 없습니다.";
    public static final String NOT_FOUND_PASS = "비밀번호가 일치하지 않습니다.";
    public static final String CREATED_USER = "회원 가입 성공";
    public static final String UPDATE_USER = "회원 정보 수정 성공";
    public static final String DELETE_USER = "회원 탈퇴 성공";
    public static final String DUPLICATE_ID = "중복된 아이디가 있습니다.";
    public static final String DUPLICATE_CMPY = "이미등록된 회사명이 있습니다";
    
    public static final String INTERNAL_SERVER_ERROR = "서버 내부 에러";
    public static final String DB_ERROR = "데이터베이스 에러";
    public static final String RETRY = "다시시도하세요";
    
    public static final String NO_DATA = "데이터가 없습니다";
    public static final String ERROR_BILL = "신청금액이 미수금보다 큽니다";
    public static final String ERROR_ITEM_DELETE = "시험이 진행되어 시험항목을 삭제할 수 없습니다";
    public static final String ERROR_MNG = "업무담당자는 필수입니다";
    public static final String ERROR_APP_DATE = "신청일은 오늘 날짜만 입력 가능합니다.";
    public static final String ERROR_WHY = "사유를 입력하세요";
    public static final String ERROR_PAYER = "입금자명을 입력해주세요.";
    public static final String ERROR_PRTN = "컨설팅선택은 필수입니다.";
    
    public static final String CHECK_DATA = "필수입력값을 확인하세요";
    public static final String DUPLICATE_SLS = "이미등록된 매출데이터가 있습니다";
    public static final String DUPLICATE_QUO = "이미등록된 견적서가 있습니다";
    public static final String DUPLICATE_SBK = "이미등록된 신청서가 있습니다";
    public static final String DUPLICATE_CNS = "이미등록된 상담서가 있습니다";
    public static final String DUPLICATE_TEST = "이미등록된 시험이 있습니다";
    public static final String DUPLICATE_RAW = "이미등록된 로데이터가 있습니다";
    public static final String DUPLICATE_RAW_BASIC = "이미등록된 로데이터 기본정보가 있습니다";
    public static final String DUPLICATE_CHQ = "이미등록된 취합견적서가 있습니다";
    
    public static final String DUPLICATE_CNFRMED = "매출확정이된 견적서가 포함되어 있습니다";
    public static final String DIFFERENT_CONS = "동일한 업체로만 취합할 수 있습니다";
    public static final String INSERT_TWO = "2개이상 취합할수 있습니다";
    public static final String GO_CHQ = "취합견적서에서 진행하세요";
    public static final String SECRET_CHQ = "보안문서는 취합할 수 없습니다";
    
    public static final String MAX_REVISION = "이정도 했으면 신청서를 새로 받읍시다";
    public static final String CHECK_JOB_SEQ = "상태변경할 문서가 없습니다.";
    
    public static final String CHECK_RATE_SUM = "참여율 합은 100%가 되어야 합니다.";
    public static final String CHECK_TEST_STATE = "시험취소가 확정되어 상태변경이 불가합니다.";
    public static final String CHECK_TEST_STATE_END = "프로젝트가 완료되어 상태변경이 불가합니다.";
    public static final String CHECK_RAW_EDIT = "프로젝트가 완료되어 로데이터를 수정할 수 없습니다.";
    public static final String CHECK_MAC = "장비데이터가 없습니다. 1초 뒤 다시 시도하세요.";
    public static final String CHECK_MAC_DATE = "시험 장비의 차기 교정일을 확인해 주세요.";
    public static final String CHECK_REPORT_DT = "성적서 발급일은 시험종료일 이후로만 입력할 수 있습니다.";
    public static final String CHECK_MSRMN_DT = "측정일은 성적서 발급일 이전으로만 입력할 수 있습니다.";

    public static final String CHECK_STORE_CODE = "위치 QR 코드 형식이 아닙니다. 예: MF-A-01, 3F-C";
    public static final String DUPLICATE_STORE = "이미 등록된 위치 코드입니다.";
    public static final String SAMPLE_CARRY_OUT = "이미 반출된 시료입니다.";

    /* 창고관리 */
    public static final String ERROR_WH_MNG = "창고 관리자는 필수입니다";
    public static final String CHECK_WH_TYPE = "창고 형태를 확인하세요.";
    public static final String CHECK_ZONE_NAME =
            "구역명을 확인하세요. 비울 수 없고, 따옴표는 쓸 수 없습니다.";
    public static final String CHECK_ZONE_DESC = "구역설명은 200자까지입니다.";
    public static final String CHECK_ZONE_USE = "구역 용도를 확인하세요. 일반, 인수요청, 장기 중 하나입니다.";
    public static final String DUPLICATE_ZONE = "이 창고에 이미 있는 구역명입니다.";
    public static final String CHECK_ZONE_RANGE =
            "접두어와 시작·끝 번호를 확인하세요. 시작은 1 이상이고, 끝은 시작보다 작을 수 없습니다.";
    public static final String ZONE_RANGE_LIMIT = "한 번에 50개까지만 만들 수 있습니다.";
    public static final String ZONE_RANGE_NONE = "새로 만든 구역이 없습니다. 모두 이미 쓰고 있는 이름입니다.";
    public static final String DUPLICATE_WH_NAME = "같은 이름의 창고가 있습니다.";
    public static final String WH_READONLY = "사용 중지된 창고는 볼 수만 있습니다.";
    public static final String WH_ALREADY_STOPPED = "이미 사용 중지된 창고입니다.";
    public static final String ZONE_READONLY = "사용 중지된 구역은 고칠 수 없습니다.";
    public static final String ZONE_ALREADY_STOPPED = "이미 사용 중지된 구역입니다.";
    public static final String ZONE_LAST_ONE =
            "이 창고에 남은 마지막 사용 구역이라 중지할 수 없습니다. 창고를 더 쓰지 않는다면 창고를 사용 중지하세요.";
    public static final String ZONE_WH_STOPPED = "사용 중지된 창고입니다. 구역을 따로 중지할 필요가 없습니다.";
    public static final String ZONE_LABEL_OFF = "사용 중지된 구역입니다. 라벨을 떼 주세요.";
    public static final String WH_LABEL_OFF = "사용 중지된 창고입니다. 라벨을 떼 주세요.";

}
