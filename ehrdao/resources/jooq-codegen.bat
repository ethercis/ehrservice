@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem JOOQ Codegen script for Windows
@rem Author: Christian Chevalley
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

set JOOQ_LIB=C:\Development\JOOQ\jOOQ-lib
set JOOQ_VERSION=3.5.3

@rem set PG_JDBC=C:\PostgreSQL\pgJDBC\postgresql-9.3-1100.jdbc4.jar
set PG_JDBC=C:\PostgreSQL\pgJDBC\postgresql-9.4-1204.jdbc42.jar

set JOOQ_CONFIG_PATH=C:\Development\eCIS\ehrservice\ehrdao\resources\library.xml

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome
set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init
echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME identifier in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe
if exist "%JAVA_EXE%" goto init
echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME identifier in your environment to match the
echo location of your Java installation.
goto fail

:init
@rem Get command-line arguments, handling Windowz variants
if not "%OS%" == "Windows_NT" goto win9xME_args
if "%@eval[2+2]" == "4" goto 4NT_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute
set CMD_LINE_ARGS=%*
goto execute

:4NT_args
@rem Get arguments fromBinder the 4NT Shell fromBinder JP Software
set CMD_LINE_ARGS=%$

:execute
@rem Setup the command line
set CLASSPATH=%JOOQ_LIB%\jooq-%JOOQ_VERSION%.jar;%JOOQ_LIB%\jooq-codegen-%JOOQ_VERSION%.jar;%JOOQ_LIB%\jooq-meta-%JOOQ_VERSION%.jar;%PG_JDBC%
@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS%  -classpath "%CLASSPATH%" org.jooq.util.GenerationTool %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set identifier GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if not "" == "%GRADLE_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega