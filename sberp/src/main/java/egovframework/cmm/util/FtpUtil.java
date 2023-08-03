package egovframework.cmm.util;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class FtpUtil {
  
  private FTPClient ftpClient;
  
  public FtpUtil() {
      this.ftpClient = new FTPClient();
  }
  
  // FTP 연결 및 설정
  // ip : FTP IP, port : FTP port, id : FTP login Id, pw : FTP login pw, dir : FTP Upload Path
  public void connect(String ip, int port, String id, String pw, String dir) throws Exception{
      try {
          boolean result = false;
          ftpClient.connect(ip, port);            //FTP 연결
          ftpClient.setControlEncoding("UTF-8");  //FTP 인코딩 설정
          int reply = ftpClient.getReplyCode();   //응답코드 받기
          
          if (!FTPReply.isPositiveCompletion(reply)) {    //응답 False인 경우 연결 해제
              ftpClient.disconnect();
              throw new Exception("FTP서버 연결실패");
          }
          if(!ftpClient.login(id, pw)) {
              ftpClient.logout();
              throw new Exception("FTP서버 로그인실패");
          }
          
          ftpClient.setSoTimeout(1000 * 10);      //Timeout 설정
          ftpClient.login(id, pw);                //FTP 로그인
          ftpClient.setFileType(FTP.BINARY_FILE_TYPE);    //파일타입설정
          ftpClient.enterLocalPassiveMode();          //Active 모드 설정
          result = ftpClient.changeWorkingDirectory(dir); //저장파일경로

          if(!result){    // result = False 는 저장파일경로가 존재하지 않음
            // 경로가 없어서 makeDirectory를 호출할 때, 생성하려는 경로가 하위경로가 있다면 존재하지 않는 상위경로부터 만들어줘야한다.
            String[] directory = dir.split("/");
            
            String newdir = "/www/";
            for(int i=0, l=directory.length; i<l; i++) {
                newdir += (directory[i]);
                try {
                    result = ftpClient.changeWorkingDirectory(newdir);
                    if(!result) {
                        result = ftpClient.makeDirectory(newdir);
                        result = ftpClient.changeWorkingDirectory(newdir);
                    }
                } catch (IOException e) {
                    throw e;
                }
                newdir += "/";
            }
          }
      } catch (Exception e) {
          if(e.getMessage().indexOf("refused") != -1) {
              throw new Exception("FTP서버 연결실패");
          }
          throw e;
      }
  }

  // FTP 연결해제
  public void disconnect(){
      try {
          if(ftpClient.isConnected()){
              ftpClient.disconnect();
          }
      } catch (IOException e) {
          int a = 0; 
          int b = 0;
          a = b;
          b = a;
      }
  }

  // FTP 파일 업로드
  public void storeFile(String saveFileNm, InputStream inputStream) throws Exception{
      try {
          if(!ftpClient.storeFile(saveFileNm, inputStream)) {
              throw new Exception("FTP서버 업로드실패");
          }
      } catch (Exception e) {
          if(e.getMessage().indexOf("not open") != -1) {
              throw new Exception("FTP서버 연결실패");
          }
          throw e;
      }
  }
}
