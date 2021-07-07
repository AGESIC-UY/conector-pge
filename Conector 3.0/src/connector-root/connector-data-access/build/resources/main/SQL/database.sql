call SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.connection.requireAuthentication','true');
call SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.authentication.provider','BUILTIN');
call SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.user.user', 'pass');
call SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.database.fullAccessusers', 'user');

#Para conectarse desde la consola del derby.
#connect 'jdbc:derby://localhost:1527/connectorpge;create=true;user=user;password=pass';
connect 'jdbc:derby:connectorpge;user=user;password=pass';
