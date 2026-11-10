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
    public static final String NO_SAMPLE = "반입된 시료가 없어 시험신청을 할 수 없습니다.";
    
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
    public static final String CHECK_REPORT_DT_FORMAT = "성적서 발급일 형식을 확인하세요. (yyyy-MM-dd)";
    public static final String CHECK_MSRMN_DT = "측정일은 성적서 발급일 이전으로만 입력할 수 있습니다.";

    // 로데이터 측정일·환경조건 (RAW_ENV_TB)
    public static final String CHECK_ENV_MET_KEY = "시험방법 구분값이 올바르지 않습니다.";
    public static final String CHECK_ENV_MAX = "측정일은 이 시험방법에서 최대 %d개까지 추가할 수 있습니다.";
    public static final String CHECK_ENV_NOT_ENDED = "시험종료가 기록되지 않은 측정일이 있어 추가할 수 없습니다.";
    public static final String CHECK_ENV_STATE = "이미 처리되었거나 현재 상태에서 할 수 없는 작업입니다. 새로고침 후 다시 시도하세요.";
    public static final String CHECK_ENV_REASON = "사유를 200자 이내로 입력하세요.";
    public static final String CHECK_ENV_DATE = "시작·종료 일시를 확인하세요. (종료는 시작 이후, 형식 yyyy-MM-dd HH:mm)";
    public static final String CHECK_ENV_AUTH = "기술책임자만 수정·삭제할 수 있습니다.";
    public static final String CHECK_ENV_REQUIRED = "시험종료 전에 온도·습도(·기압)와 시험자명, 서명을 입력하세요.";
    /** 같은 시험원의 다른 측정과 시간이 겹침. %s = 접수번호, 시험방법, 시작~종료 */
    public static final String CHECK_ENV_OVERLAP = "같은 시험원의 다른 측정과 시간이 겹칩니다. (%s %s %s)";
    public static final String CHECK_ENV_ACTION = "측정일 버튼 구분값이 올바르지 않습니다. (START, END 또는 빈 값)";
    
}
