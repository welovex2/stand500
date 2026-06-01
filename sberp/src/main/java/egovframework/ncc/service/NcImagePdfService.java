package egovframework.ncc.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import egovframework.ncc.dto.NcImagePdfResolvedRequest;
import egovframework.ncc.dto.NcImagesToPdfRequest;
import egovframework.ncc.dto.NcImagesToPdfResult;

public interface NcImagePdfService {

  NcImagePdfResolvedRequest resolveRequest(NcImagesToPdfRequest req) throws Exception;

  byte[] generatePdf(NcImagePdfResolvedRequest resolved) throws Exception;

  void download(NcImagesToPdfRequest req, HttpServletRequest request, HttpServletResponse response)
      throws Exception;

  NcImagesToPdfResult save(NcImagesToPdfRequest req, HttpServletRequest request) throws Exception;

}
