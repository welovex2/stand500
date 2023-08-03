package egovframework.tst.service;

import java.time.LocalDateTime;

import javax.persistence.Column;

import org.springmodules.validation.bean.conf.loader.annotation.handler.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TestItemRej {

	@ApiModelProperty(value="TEST_ITEM_REJ_SEQ",  example = "", required = true)
	@Column
	@NotNull
	private int testItemRejSeq;
	
	@ApiModelProperty(value="TEST_ITEM_SEQ",  example = "", required = true)
	@Column
	@NotNull
	private int testItemSeq;


	@ApiModelProperty(value="반려/메모 선택", example = "")
	@Column
	private String topicCode;


	@ApiModelProperty(value="memo ", example = "")
	@Column
	private String memo;

	@ApiModelProperty(value="사용여부 ", example = "", hidden = true)
	@Column
	private int useYn;


	@ApiModelProperty(value="INS_MEM_ID ", example = "", hidden = true)
	@Column
	private String insMemId;


	@ApiModelProperty(value="INS_DT ", example = "", hidden = true)
	@Column
	private LocalDateTime insDt;


	@ApiModelProperty(value="UDT_MEM_ID ", example = "", hidden = true)
	@Column
	private String udtMemId;


	@ApiModelProperty(value="UDT_DT ", example = "", hidden = true)
	@Column
	private LocalDateTime udtDt;


	@ApiModelProperty(value="상태(I,U,D) ", example = "", hidden = true)
	@Column
	private String state;
	
}
