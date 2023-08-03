package egovframework.cmm.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import egovframework.chq.service.ChqDTO;
import egovframework.quo.dto.EngQuoDTO;
import egovframework.quo.service.QuoDTO;
import egovframework.raw.dto.RawDTO;
import egovframework.raw.dto.ReportDTO;
import egovframework.raw.service.FileRaw;
import egovframework.sam.dto.ImDTO;
import egovframework.sbk.service.SbkDTO;
import egovframework.sls.service.BillDTO;
import egovframework.sls.service.PayDTO;
import egovframework.sls.service.SlsDTO;
import egovframework.tst.dto.TestDTO;

@RestController
public class SwaggerModel {

  @GetMapping(value = "/QuoDTO.Res")
  public QuoDTO.Res quoList() throws Exception {
    QuoDTO.Res result = new QuoDTO.Res();
    return result;
  }

  @GetMapping(value = "/BillDTO.Res")
  public BillDTO.Res billList() throws Exception {
    BillDTO.Res result = new BillDTO.Res();
    return result;
  }

  @GetMapping(value = "/PayDTO.Res")
  public PayDTO.Res payList() throws Exception {
    PayDTO.Res result = new PayDTO.Res();
    return result;
  }

  @GetMapping(value = "/SlsDTO.Res")
  public SlsDTO.Res slsList() throws Exception {
    SlsDTO.Res result = new SlsDTO.Res();
    return result;
  }

  @GetMapping(value = "/SbkDTO.Res")
  public SbkDTO.Res sbkList() throws Exception {
    SbkDTO.Res result = new SbkDTO.Res();
    return result;
  }

  @GetMapping(value = "/TestDTO.Res")
  public TestDTO.Res tstList() throws Exception {
    TestDTO.Res result = new TestDTO.Res();
    return result;
  }

  @GetMapping(value = "/RawDTO")
  public RawDTO rawList() throws Exception {
    RawDTO result = new RawDTO();
    return result;
  }

  @GetMapping(value = "/ReportDTO")
  public ReportDTO report() throws Exception {
    ReportDTO result = new ReportDTO();
    return result;
  }

  @GetMapping(value = "/fileRaw")
  public FileRaw fileRaw() throws Exception {
    FileRaw result = new FileRaw();
    return result;
  }

  @GetMapping(value = "/ChqDTO.Res")
  public ChqDTO.Res chq() throws Exception {
    ChqDTO.Res result = new ChqDTO.Res();
    return result;
  }
  
  @GetMapping(value = "/ImDTO")
  public ImDTO sam() throws Exception {
    ImDTO result = new ImDTO();
    return result;
  }
  
  @GetMapping(value = "/EngQuoDTO")
  public EngQuoDTO engQuo() throws Exception {
    EngQuoDTO result = new EngQuoDTO();
    return result;
  }
}
