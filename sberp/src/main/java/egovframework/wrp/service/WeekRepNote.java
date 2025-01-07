package egovframework.wrp.service;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "주간보고 피드백")
public class WeekRepNote {
  private int wrSeq;      // 보고서 SEQ
  private String memo;    // 내용
}
