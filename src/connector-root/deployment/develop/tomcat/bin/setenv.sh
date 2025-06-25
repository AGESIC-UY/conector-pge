export CATALINA_HOME="$(dirname "$(pwd)")"
export CATALINA_OPTS="$CATALINA_OPTS -Dfile.encoding=UTF-8 -Dderby.system.home=$CATALINA_HOME/derby -DuploadFolder=$CATALINA_HOME/connector -Dconnector.integration.configLocation=$CATALINA_HOME/conf -Dconnector.web.configLocation=$CATALINA_HOME/conf -Dconnector.web.log4jLocation=$CATALINA_HOME/logs -Dconnector.integration.log4jLocation=$CATALINA_HOME/logs -Dconnector.web.log4jLevel=$LOG_LEVEL -Dconnector.integration.log4jLevel=$LOG_LEVEL"
export JRE_HOME=$CATALINA_HOME/../jre-13.0.2

