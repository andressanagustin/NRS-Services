echo -------------------------------------------------------------------------
echo SERVER_NAME = $(serverName); PORT = $(dbPort); INSTANCE_NAME = $(instanceName); DB_NAME = $(dbName); USER_NAME = $(dbUser); DB_PASS = $(dbPass);
echo -------------------------------------------------------------------------

call sqlcmd -S $(serverName)\$(instanceName) -U $(dbUser) -P $(dbPass) -i exec_all_scripts.sql
pause