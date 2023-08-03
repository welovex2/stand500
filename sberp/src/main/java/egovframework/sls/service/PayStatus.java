package egovframework.sls.service;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.validation.constraints.Pattern;

import org.springmodules.validation.bean.conf.loader.annotation.handler.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@ApiModel(value="PayStatus", description = "납부상태")
public class PayStatus {

	@ApiModelProperty(value="PAY_STATUS_SEQ ", example = "", required = true)
	@Column
	@NotNull
	private int payStatusSeq;


	@ApiModelProperty(value="SLS_SEQ ", example = "", hidden = true)
	@Column
	@NotNull
	private int slsSeq;


	@ApiModelProperty(value="SLS_YM ", example = "", hidden = true)
	@Column
	@NotNull
	private String slsYm;


	@ApiModelProperty(value="납부상태(공통토드:MP) 01 납부완료(계좌이체) 02 납부완료(가상계좌) 03 납부완료(신용카드) 04 납부완료(기타입력)  ", example = "", required = true)
	@Column
	private String stateCode;


	@ApiModelProperty(value="금액 ", example = "", required = true)
	@Column
	@Pattern(regexp = "^[0-9]*$", message = "금액은 숫자만 입력 가능합니다.")
	private int pay;


	@ApiModelProperty(value="사유 ", example = "", required = true)
	@Column
	private String memo;


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
	
}
