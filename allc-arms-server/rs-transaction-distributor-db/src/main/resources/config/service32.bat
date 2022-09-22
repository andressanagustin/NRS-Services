echo -------------------------------------------------------------------------
echo Generando Servicio para Windows 32 bits.
echo -------------------------------------------------------------------------

set PATH=%PATH%;C:\ALLC\winrun4j\bin
call service32.exe --WinRun4J:RegisterService

pause
