@rem *------------------------------------------------------------------------*
@rem * startup Rhigin id.
@rem *------------------------------------------------------------------------*
@set ARGS=%1 %2 %3 %4 %5 %6 %7 %8 %9
@if "%OS%" == "Windows_NT" setlocal
@echo off
@cls

@rem *------------------------------------------------------------------------*
@rem * Move to the folder at shell startup.
@rem *------------------------------------------------------------------------*
@set SCRIPT=%~0
@for /f "delims=\ tokens=*" %%z in ("%SCRIPT%") do (
@set SCRIPT_CURRENT_DIR=%%~dpz )
@cd %SCRIPT_CURRENT_DIR%

@rem *------------------------------------------------------------------------*
@rem * JDK installation destination.
@rem * Please do not specify anything if not set.
@rem *------------------------------------------------------------------------*
@set SET_JAVA_HOME=

@rem *------------------------------------------------------------------------*
@rem * Project directory settings.
@rem *------------------------------------------------------------------------*
@set PROJ_DIR=.\

@rem *------------------------------------------------------------------------*
@rem * Java option.
@rem *------------------------------------------------------------------------*
@set OPT=

@rem *------------------------------------------------------------------------*
@rem * Start program set.
@rem *------------------------------------------------------------------------*
@set EXEC_PACKAGE=rhigin.RhiginServerId

@rem *------------------------------------------------------------------------*
@rem * Start memory area.
@rem * Unit is MByte.
@rem *------------------------------------------------------------------------*
@set STM=64

@rem *------------------------------------------------------------------------*
@rem * Maximum memory area.
@rem * Unit is MByte.
@rem *------------------------------------------------------------------------*
@set EXM=64

@rem ##########################################################################
@rem # Please do not set below this.
@rem ##########################################################################
@rem * baseFolder. *
@set BASE_HOME=%RHIGIN_HOME%

@rem *------------------------------------------------------------------------*
@rem * Reflect setting conditions.
@rem *------------------------------------------------------------------------*
@if not "%SET_JAVA_HOME%" == "" @set JAVA_HOME=%SET_JAVA_HOME%

@rem *------------------------------------------------------------------------*
@rem * Startup batch directory.
@rem *------------------------------------------------------------------------*
@set BATCH_DIR=%BASE_HOME%

@rem *------------------------------------------------------------------------*
@rem * JAR storage directory.
@rem *------------------------------------------------------------------------*
@set LIB_DIR=%BASE_HOME%\lib

@rem *------------------------------------------------------------------------*
@rem * Error judgment.
@rem *------------------------------------------------------------------------*
@if "%JAVA_HOME%" == "" goto errJAVA_HOME
@if "%PROJ_DIR%" == "" goto errPROJ_DIR

@rem *------------------------------------------------------------------------*
@rem * execution java.
@rem *------------------------------------------------------------------------*

@call %BATCH_DIR%\sub\parselib
@set CLASSPATH=.;%INST_LIB%

@set OPT=%OPT% -Djava.awt.headless=true
@set OPT=%OPT% -Djava.net.preferIPv4Stack=true
@set BASE_OPT=-Xms%STM%m -Xmx%EXM%m

@%JAVA_HOME%\bin\java %BASE_OPT% %OPT% %EXEC_PACKAGE% %ARGS%
goto end

@rem *------------------------------------------------------------------------*
@rem * Error handling.
@rem *------------------------------------------------------------------------*

:errJAVA_HOME
@echo "Environment variable JAVA_HOME has not been set."
goto end

:errPROJ_DIR
@echo "The project directory is invalid."
goto end

:end
@if "%OS%" == "Windows_NT" endlocal
