set SRC=F:\Lius.20121224\java_svn\bs4async\
set DST=F:\Lius.20121224\java_svn_gcode\asyncweb2\
set DIR=src\org\apache\mina\filter\codec\http


@REM	svn checkout https://asyncweb2.googlecode.com/svn/trunk/ asyncweb2 --username Nike.Lius@gmail.com

@REM	svn co https://asyncweb2.googlecode.com/svn/trunk/ asyncweb2 --username Nike.Lius@gmail.com
@REM	svn commit 

@REM	copy %SRC%\%DIR%\*.java %DST%\%DIR%\
@REM	cd asyncweb2 

@echo   "首次提交"
	svn log
	svn add	src
	svn add	src\org\apache\
	svn add	src\org\apache\mina
	svn add	src\org\apache\mina\filter
	svn add	src\org\apache\mina\filter\codec
	svn add	%DIR%\*.java
	svn commit --message "（1）src_asyncweb_apache_mina1_0.rar	反编译 asyncweb-common-0.9.0-SNAPSHOT.jar（仅支持mina2.0m2）"
@echo   "更新提交"
	svn add uploader.bat
	svn commit --message "bs3-svn-4"
@pause
