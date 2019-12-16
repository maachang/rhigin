@rem *------------------------------------------------------------------------*
@rem * startup Rhigin librarys
@rem *------------------------------------------------------------------------*
@set ARGS=%1
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

cd %RHIGIN_HOME%
cd ..
cd components
cd %ARGS%

ant

:end
@if "%OS%" == "Windows_NT" endlocal
