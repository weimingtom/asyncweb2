set SRC=F:\Lius.20121224\java_svn\bs4async\
set DST=F:\Lius.20121224\java_svn_gcode\asyncweb2\
set DST=F:\Work.Eclipse\asyncweb2\
set DIR=src\org\apache\mina\filter\codec\http

@REM	svn checkout https://asyncweb2.googlecode.com/svn/trunk/ asyncweb2 --username Nike.Lius@gmail.com
@REM	svn co https://asyncweb2.googlecode.com/svn/trunk/ asyncweb2 --username Nike.Lius@gmail.com
@REM	svn commit 

@	copy %SRC%\%DIR%\*.* %DST%\%DIR%\
@REM	cd asyncweb2 

@echo   "首次提交"
@REM	svn log
@REM	svn add	src
@REM	svn add	src\org\apache\
@REM	svn add	src\org\apache\mina
@REM	svn add	src\org\apache\mina\filter
@REM	svn add	src\org\apache\mina\filter\codec
@REM	svn add	src\org\apache\mina\filter\codec\http
@REM	svn add	%DIR%\*.java
@REM	svn commit --message "（1）src_asyncweb_apache_mina1_0.rar	反编译 asyncweb-common-0.9.0-SNAPSHOT.jar（仅支持mina2.0m2）"
@echo   "更新提交"
@REM	svn add uploader.bat
@REM	svn commit --message "bs3-svn-4"

@echo   "更新提交"
@REM	svn add src\org\apache\mina\filter\codec\http\DefaultHttpMessage.java
@REM	svn commit -m "bs3-svn-23" src\org\apache\mina\filter\codec\\http\DefaultHttpMessage.java
@pause
