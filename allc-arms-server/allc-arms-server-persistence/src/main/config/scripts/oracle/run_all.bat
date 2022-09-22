echo -------------------------------------------------------------------------
echo SERVER_NAME = $(serverName); PORT = $(dbPort); SERVICE_NAME = $(serviceName); USER_NAME = $(dbUser); DB_PASS = $(dbPass);
echo -------------------------------------------------------------------------

call sqlplus -S "$(dbUser)/$(dbPass)@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(Host=$(serverName))(Port=$(dbPort)))(CONNECT_DATA=(SID=$(serviceName))))" @exec_all_scripts.sql

pause