package egovframework.wrp.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import springfox.documentation.annotations.ApiIgnore;

/** 예전 /wrp 화면 주소를 /page 로 넘긴다. */
@ApiIgnore
@Controller
@RequestMapping("/wrp")
public class WrpStuckViewController {

  @GetMapping("/pages.do")
  public String oldPages() {
    return "redirect:/page/list.do";
  }

  @GetMapping("/stuck/view.do")
  public String oldStuck(
      @RequestParam(value = "testTypeCode", defaultValue = "EM") String testTypeCode) {
    return "redirect:/page/stuck.do?testTypeCode=" + testTypeCode;
  }
}
