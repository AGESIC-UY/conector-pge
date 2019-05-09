export CATALINA_OPTS="$CATALINA_OPTS \
-Dfile.encoding=UTF-8 \
-Dconnector.integration.configLocation=$CATALINA_HOME/conf \
-Dconnector.web.configLocation=$CATALINA_HOME/conf \
-Dconnector.integration.log4jLocation=$CATALINA_HOME/logs \
-Dconnector.web.log4jLocation=$CATALINA_HOME/logs \
-DuploadFolder=$CATALINA_HOME/connector \
-Dderby.system.home=$CATALINA_HOME/derby"
