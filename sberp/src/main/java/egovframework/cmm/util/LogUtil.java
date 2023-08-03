package egovframework.cmm.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogUtil {

  public static void setWarn(Exception exception) {

    StackTraceElement[] ste = exception.getStackTrace();
    StringBuffer str = new StringBuffer();
    int lastIndex = ste.length - 1;
    int count = 1;
    for (int i = lastIndex; i > lastIndex - 3; i--) {
      String className = ste[i].getClassName();
      String methodName = ste[i].getMethodName();
      int lineNumber = ste[i].getLineNumber();
      String fileName = ste[i].getFileName();

      str.append("\n").append("[" + count++ + "]").append("className :").append(className)
          .append("\n").append("methodName :").append(methodName).append("\n").append("fileName :")
          .append(fileName).append("\n").append("lineNumber :").append(lineNumber).append("\n")
          .append("message :").append(exception.getMessage()).append("\n").append("cause :")
          .append(exception.getCause()).append("\n");
    }
    
    log.warn(str.toString());
    System.out.println(str.toString());
  }

}
