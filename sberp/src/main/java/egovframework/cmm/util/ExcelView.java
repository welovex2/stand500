/*
 * Copyright 2014 MOSPA(Ministry of Security and Public Administration).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package egovframework.cmm.util;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.servlet.view.AbstractView;
import egovframework.rte.psl.dataaccess.util.EgovMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExcelView extends AbstractView {

  /** The content type for an Excel response */
   private static final String CONTENT_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

  /**
   * Default Constructor. Sets the content type of the view for excel files.
   */
  public ExcelView() {
  }

  @Override
  protected boolean generatesDownloadContent() {
      return true;
  }

  /**
   * Renders the Excel view, given the specified model.
   */
  @Override
  protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

      XSSFWorkbook workbook = new XSSFWorkbook();
      log.debug("Created Excel Workbook from scratch");

      setContentType(CONTENT_TYPE_XLSX);

      buildExcelDocument(model, workbook, request, response);
      
      // Set the filename
      String sFilename = "";
      if(model.get("filename") != null){
          sFilename = (String)model.get("filename");
      }else if(request.getAttribute("filename") != null){
          sFilename = (String)request.getAttribute("filename");
      }else{
          sFilename = getClass().getSimpleName();
       }

      response.setContentType(getContentType());
      
      String header = request.getHeader("User-Agent");
      sFilename = sFilename.replaceAll("\r","").replaceAll("\n","");
      if(header.contains("MSIE") || header.contains("Trident") || header.contains("Chrome")){
          sFilename = URLEncoder.encode(sFilename,"UTF-8").replaceAll("\\+","%20");
          response.setHeader("Content-Disposition","attachment;filename="+sFilename+".xlsx;");
      }else{
          sFilename = new String(sFilename.getBytes("UTF-8"),"ISO-8859-1");
          response.setHeader("Content-Disposition","attachment;filename=\""+sFilename + ".xlsx\"");
      }
      
      // Flush byte array to servlet output stream.
      ServletOutputStream out = response.getOutputStream();
      out.flush();
      workbook.write(out);
      out.flush();

      // Don't close the stream - we didn't open it, so let the container handle it.
      // http://stackoverflow.com/questions/1829784/should-i-close-the-servlet-outputstream
  }
  
  @SuppressWarnings("unchecked")
  protected void buildExcelDocument(Map model, XSSFWorkbook wb, HttpServletRequest req, HttpServletResponse resp) throws Exception {
      Map<String, Object> dataMap = (Map<String, Object>) model.get("dataMap");
      XSSFCell cell = null;

      String sheetNm = (String) dataMap.get("sheetNm"); // 엑셀 시트 이름
      
      String[] columnArr = (String[]) dataMap.get("columnArr"); // 각 컬럼 이름
      String[] columnVarArr = (String[]) dataMap.get("columnVarArr"); // 각 컬럼의 변수 이름
      int[] columnWidth = (int[]) dataMap.get("columnWidth"); // 각 컬럼의 길이
      
      List<EgovMap> dataList = (List<EgovMap>) dataMap.get("list"); // 데이터가 담긴 리스트 
      
      CellStyle cellStyle = wb.createCellStyle(); // 제목셀의 셀스타일
      cellStyle.setWrapText(true); // 줄 바꿈            
      cellStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index); // 셀 색상
      cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND); // 셀 색상 패턴
      cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 셀 가로 정렬
      cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER); // 셀 세로 정렬
      cellStyle.setDataFormat((short)0x31); // 셀 데이터 형식
      cellStyle.setBorderRight(HSSFCellStyle.BORDER_DOUBLE);
      cellStyle.setBorderLeft(HSSFCellStyle.BORDER_DOUBLE);
      cellStyle.setBorderTop(HSSFCellStyle.BORDER_DOUBLE);
      cellStyle.setBorderBottom(HSSFCellStyle.BORDER_DOUBLE);
      
      // 셀 폰트색상, bold처리
      Font font = wb.createFont();
      font.setColor(HSSFColor.WHITE.index);
      font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
      cellStyle.setFont(font);
      
      CellStyle cellStyle2 = wb.createCellStyle(); // 데이터셀의 셀스타일
      cellStyle2.setWrapText(true); // 줄 바꿈           
      cellStyle2.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 셀 가로 정렬
      cellStyle2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER); // 셀 세로 정렬
      cellStyle2.setDataFormat((short)0x31); // 셀 데이터 형식
      cellStyle2.setBorderRight(HSSFCellStyle.BORDER_THIN);
      cellStyle2.setBorderLeft(HSSFCellStyle.BORDER_THIN);
      cellStyle2.setBorderTop(HSSFCellStyle.BORDER_THIN);
      cellStyle2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
      
      XSSFSheet sheet = wb.createSheet(sheetNm);
      sheet.setDefaultColumnWidth(7);
      
      // 컬럼명 삽입
      for(int i=0; i<columnArr.length; i++){
          setText(getCell(sheet, 0, i), columnArr[i]);
          getCell(sheet, 0, i).setCellStyle(cellStyle);
//          sheet.autoSizeColumn(i);
//          int columnWidth = (sheet.getColumnWidth(i))*2;
          sheet.setColumnWidth(i, columnWidth[i]);
          
          if(dataList.size() < 1){
              cell = getCell(sheet, 1, i);
              if(i==0){
                  setText(cell, "등록된 정보가 없습니다.");
              }
              cell.setCellStyle(cellStyle2);
          }
      }
      
      if(dataList.size() > 0){ // 저장된 데이터가 있을때
          // 리스트 데이터 삽입
          for (int i = 0; i<dataList.size(); i++) {
              EgovMap dataEgovMap = dataList.get(i);
              
              // 맨 앞 컬럼인 "번호"는 idx라는 이름으로 여기서 생성하여 넣어준다.
              dataEgovMap.put("idx", (i+1)+""); 
              
              for(int j=0; j<columnVarArr.length; j++){
                  String data = String.valueOf(dataEgovMap.get(columnVarArr[j]));
                  cell = getCell(sheet, 1 + i, j);
                  setText(cell, data);
                  cell.setCellStyle(cellStyle2);
              }
          }
      }else{ // 저장된 데이터가 없으면 셀 병합
          // 셀 병합(시작열, 종료열, 시작행, 종료행)
          sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, columnArr.length-1));
      }
  }

  /**
   * Convenient method to obtain the cell in the given sheet, row and column.
   * 
   * <p>Creates the row and the cell if they still doesn't already exist.
   * Thus, the column can be passed as an int, the method making the needed downcasts.</p>
   * 
   * @param sheet a sheet object. The first sheet is usually obtained by workbook.getSheetAt(0)
   * @param row thr row number
   * @param col the column number
   * @return the XSSFCell
   */
  protected XSSFCell getCell(XSSFSheet sheet, int row, int col) {
      XSSFRow sheetRow = sheet.getRow(row);
      if (sheetRow == null) {
          sheetRow = sheet.createRow(row);
      }
      XSSFCell cell = sheetRow.getCell((short) col);
      if (cell == null) {
          cell = sheetRow.createCell((short) col);
      }
      return cell;
  }

  /**
   * Convenient method to set a String as text content in a cell.
   * 
   * @param cell the cell in which the text must be put
   * @param text the text to put in the cell
   */
  protected void setText(XSSFCell cell, String text) {
      cell.setCellType(XSSFCell.CELL_TYPE_STRING);
      cell.setCellValue(text);
  }            
}
