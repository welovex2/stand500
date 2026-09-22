/**
 * 성적서 PDF fetch 다운로드 — Content-Disposition / X-Download-Filename 파싱.
 *
 * 사용 (성적서 화면 report.js 등):
 *   downloadReportPdf('/api/raw/' + testSeq + '/report/pdf/download.do?draft=true&template=report_tel_3078');
 */
(function (global) {
  'use strict';

  function resolvePdfFileName(res) {
    var hdr = res.headers.get('X-Download-Filename');
    if (hdr) {
      try {
        return decodeURIComponent(hdr);
      } catch (e) {
        return hdr;
      }
    }
    var cd = res.headers.get('Content-Disposition');
    if (cd) {
      var star = /filename\*=UTF-8''([^;\n]+)/i.exec(cd);
      if (star && star[1]) {
        try {
          return decodeURIComponent(star[1].trim());
        } catch (e2) {
          return star[1].trim();
        }
      }
      var plain = /filename="([^"]+)"/i.exec(cd) || /filename=([^;\n]+)/i.exec(cd);
      if (plain && plain[1]) {
        return plain[1].trim();
      }
    }
    return 'report.pdf';
  }

  function downloadReportPdf(url, options) {
    var opts = options || {};
    return fetch(url, {
      method: opts.method || 'GET',
      credentials: opts.credentials || 'include',
      headers: opts.headers || {}
    }).then(function (res) {
      if (!res.ok) {
        return res.text().then(function (body) {
          throw new Error('PDF download failed (' + res.status + '): ' + body);
        });
      }
      var fileName = resolvePdfFileName(res);
      if (!/\.pdf$/i.test(fileName)) {
        fileName = fileName + '.pdf';
      }
      return res.blob().then(function (blob) {
        var objectUrl = URL.createObjectURL(blob);
        var a = document.createElement('a');
        a.href = objectUrl;
        a.download = fileName;
        a.style.display = 'none';
        document.body.appendChild(a);
        a.click();
        setTimeout(function () {
          URL.revokeObjectURL(objectUrl);
          a.remove();
        }, 0);
        return fileName;
      });
    });
  }

  global.downloadReportPdf = downloadReportPdf;
  global.resolveReportPdfFileName = resolvePdfFileName;
})(typeof window !== 'undefined' ? window : this);
