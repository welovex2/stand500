#!/bin/sh

echo "로그파일 삭제 시작"

find /stand500admin/tomcat/logs/ -mtime +30 -name catalina\* -exec rm {} \; 
find /stand500admin/tomcat/logs/ -mtime +30 -name \* -exec rm {} \;

echo "로그파일 삭제 완료"
