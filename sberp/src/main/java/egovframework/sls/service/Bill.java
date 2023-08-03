package egovframework.sls.service;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.validation.constraints.Pattern;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotNull;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Bill {
	
	@ApiModelProperty(value="SLS_YM ", example = "")
	@Column
	@NotNull
	private String slsYm;


	@ApiModelProperty(value="SLS_SEQ ", example = "")
	@Column
	@NotNull
	private int slsSeq;


	@ApiModelProperty(value="BILL_SEQ ", example = "")
	@Column
	@NotNull
	private int billSeq;


	@ApiModelProperty(value="계산서발행여부 ", example = "")
	@Column
	private String billYn;


	@ApiModelProperty(value="계산서금액 ", example = "")
	@Pattern(regexp = "^[0-9]*$", message = "금액은 숫자만 입력 가능합니다.") 
	@Column
	private int bill;


	@ApiModelProperty(value="계산서발행날짜 ", example = "")
	@Column
	private LocalDateTime billDt;


	@ApiModelProperty(value="계산서 발행 아이디 ", example = "")
	@Column
	private String billId;


	@ApiModelProperty(value="납부상태(공통토드:MP) 01 납부완료(계좌이체) 02 납부완료(가상계좌) 03 납부완료(신용카드) 04 납부완료(기타입력)  ", example = "")
	@Column
	private String payCode;


	@ApiModelProperty(value="계산서발행날짜 ", example = "")
	@Column
	private LocalDateTime payDt;


	@ApiModelProperty(value="납부상태변경 아이디 ", example = "")
	@Column
	private String payId;


	@ApiModelProperty(value="OTHER_BILL ", example = "")
	@Column
	private int otherBill;


	@ApiModelProperty(value="OTHER_BILL_DT ", example = "")
	@Column
	private String otherBillDt;


	@ApiModelProperty(value="등록자 아이디 ", example = "", hidden = true)
	@Column
	private String insMemId;


	@ApiModelProperty(value="등록 날짜시간 ", example = "", hidden = true)
	@Column
	private LocalDateTime insDt;


	@ApiModelProperty(value="수정자 아이디 ", example = "", hidden = true)
	@Column
	private String udtMemId;


	@ApiModelProperty(value="수정 날짜시간 ", example = "", hidden = true)
	@Column
	private LocalDateTime udtDt;


	@ApiModelProperty(value="상태(I,U,D) ", example = "", hidden = true)
	@Column
	private String state;


	@ApiModelProperty(value="MEMO ", example = "")
	@Column
	private String memo;

}
