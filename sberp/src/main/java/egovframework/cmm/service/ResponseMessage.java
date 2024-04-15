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
    
    public static final String INTERNAL_SERVER_ERROR = "서버 내부 에러";
    public static final String DB_ERROR = "데이터베이스 에러";
    public static final String RETRY = "다시시도하세요";
    
    public static final String NO_DATA = "데이터가 없습니다";
    public static final String ERROR_BILL = "신청금액이 미수금보다 큽니다";
    public static final String ERROR_ITEM_DELETE = "시험이 진행되어 시험항목을 삭제할 수 없습니다";
    public static final String ERROR_MNG = "업무담당자는 필수입니다";
    
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
    public static final String DIFFERENT_CONS = "동일한 컨설팅만 취합할 수 있습니다";
    public static final String INSERT_TWO = "2개이상 취합할수 있습니다";
    public static final String GO_CHQ = "취합견적서에서 진행하세요";
    
    public static final String MAX_REVISION = "이정도 했으면 신청서를 새로 받읍시다";
    public static final String CHECK_JOB_SEQ = "상태변경할 문서가 없습니다.";
    
    public static final String CHECK_RATE_SUM = "참여율 합은 100%가 되어야 합니다.";
}
