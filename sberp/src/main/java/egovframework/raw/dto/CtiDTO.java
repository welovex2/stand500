package egovframework.raw.dto;

import java.util.List;

import javax.persistence.Column;

import egovframework.raw.service.MethodCti;
import egovframework.raw.service.MethodCtiSub;
import egovframework.raw.service.RawMac;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString(callSuper = true)
public class CtiDTO extends MethodCti {
	
	@ApiModelProperty(value="시험고유번호", example = "15", hidden = true)
	@Column
	int testSeq;
	
	@ApiModelProperty(value="측정설비 종류(공통코드 : TM)", example = "CE")
	@Column
	String macType;
	
	@ApiModelProperty(value="측정설비 리스트", example = "")
	@Column
	List<RawMac> macList;
	
	@ApiModelProperty(value="시험결과 > 전원 System: DC XX V System ", example = "")
	@Column
	List<MethodCtiSub> subList;
}
