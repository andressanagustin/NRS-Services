echo -------------------------------------------------------------------------
echo Generando Servicio para Windows 64 bits.
echo -------------------------------------------------------------------------

set PATH=%PATH%;C:\ALLC\winrun4j\bin
call service64.exe --WinRun4J:RegisterService

pause
