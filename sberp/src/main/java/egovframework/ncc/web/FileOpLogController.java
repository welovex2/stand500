package egovframework.ncc.web;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.PagingVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.ncc.dto.FileOpLogListItemDTO;
import egovframework.ncc.dto.FileOpLogSummaryDTO;
import egovframework.ncc.service.FileOpLogService;
import egovframework.rte.fdl.property.EgovPropertyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = {"파일서버"})
@RestController
@RequestMapping("/nc/file-op-log")
public class FileOpLogController {

  @Resource(name = "FileOpLogService")
  private FileOpLogService fileOpLogService;

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @ApiOperation(value = "파일작업이력 목록",
      notes = "FILE_OP_LOG_TB 조회. 검색박스는 공통코드 CS, searchVO 사용\n"
          + " 50 신청서번호\n"
          + " 53 이용자(ID)\n"
         // + " 87 작업일시(startDate/endDate)\n"
          + " 88 파일명\n"
          + " 89 작업유형(UPLOAD/MKDIR/DOWNLOAD 등)\n"
          + " 90 형식(FILE|FOLDER)\n"
          + " 91 구분(E=ERP, A=연동)\n"
          + " 92 결과코드(SUCCESS/FAIL)\n"
          + " 93 부서(Code말고 풀네임으로 보내기)\n"
          + "요약: summary(총용량/파일수/폴더수)")
  @GetMapping("/list")
  public BasicResponse list(@ModelAttribute ComParam param) throws Exception {

    boolean result = true;
    String msg = "";

    if (!EgovUserDetailsHelper.isAuthenticated()) {
      return BasicResponse.builder().result(false).message(ResponseMessage.UNAUTHORIZED).build();
    }

    param.setPageUnit(param.getPageUnit());
    param.setPageSize(propertyService.getInt("pageSize"));

    PagingVO pagingVO = new PagingVO();
    pagingVO.setCurrentPageNo(param.getPageIndex());
    pagingVO.setDisplayRow(param.getPageUnit());
    pagingVO.setDisplayPage(param.getPageSize());
    param.setFirstIndex(pagingVO.getFirstRecordIndex());

    int cnt = fileOpLogService.selectListCnt(param);
    pagingVO.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));

    List<FileOpLogListItemDTO> list = cnt > 0 ? fileOpLogService.selectList(param)
        : new ArrayList<FileOpLogListItemDTO>();

    FileOpLogSummaryDTO summary = fileOpLogService.selectSummary(param);

    if (cnt == 0) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    return BasicResponse.builder().result(result).message(msg).data(list).summary(summary)
        .paging(pagingVO).build();
  }
}
