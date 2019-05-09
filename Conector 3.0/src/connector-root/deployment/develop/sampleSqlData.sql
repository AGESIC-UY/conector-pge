-------------------------
--CONFIGURACION GLOBAL
INSERT INTO "USER"."CONFIGURATION" (DTYPE,ID,ALIAS_KEYSTORE,DIR_KEYSTORE,DIR_KEYSTORE_ORG,DIR_KEYSTORE_SSL,PASSWORD_KEYSTORE,PASSWORD_KEYSTORE_ORG,PASSWORD_KEYSTORE_SSL,TYPE) VALUES ('GLOBAL',1,'testing.hg.red.uy','/home/abrusco/Escritorio/conector_empaquetado/apache-tomcat-8.5.23/conf/agesic_v2.0.keystore','/home/abrusco/Escritorio/conector_empaquetado/apache-tomcat-8.5.23/conf/agesic_v2.0.keystore','/home/abrusco/Escritorio/conector_empaquetado/apache-tomcat-8.5.23/conf/agesic_v1.0.truststore','conector','conector','conector','Produccion');

-------------------------
--CONECTOR para ACCE
INSERT INTO "USER"."CONFIGURATION" (DTYPE,ID,ALIAS_KEYSTORE,DIR_KEYSTORE,DIR_KEYSTORE_ORG,DIR_KEYSTORE_SSL,PASSWORD_KEYSTORE,PASSWORD_KEYSTORE_ORG,PASSWORD_KEYSTORE_SSL,TYPE) VALUES ('LOCAL',2,'testing.hg.red.uy','/home/abrusco/Escritorio/conector_empaquetado/apache-tomcat-8.5.23/conf/agesic_v1.0.truststore','/home/abrusco/Escritorio/conector_empaquetado/apache-tomcat-8.5.23/conf/agesic_v2.0.keystore','/home/abrusco/Escritorio/conector_empaquetado/apache-tomcat-8.5.23/conf/agesic_v2.0.keystore','conector','conector','conector','Produccion');

INSERT INTO "USER"."ROLE_OPERATION" (ID,OPERATION_WSDL,ROLE,WSA_ACTION) VALUES (1,'consultar','ou=gerencia de proyectos,o=agesic','consultar');

INSERT INTO "USER"."USER_CREDENTIALS" (ID,UNT_NAME,UNT_PASSWORD) VALUES (1,'88888888','grp2013');

INSERT INTO "USER"."CONNECTOR" (ID,DESCRIPTION,CACHE_CONFIG,LOCAL_CONFIG,USER_CREDENTIALS,ISSUER,NAME,PATH,POLICY_NAME,TAG,TYPE,URL,USERNAME,WSA_TO,LOCALCONFIGURATION_ID,USERCREDENTIALS_ID,SSL_ENABLED,STS_LOCAL_ENABLED,STS_LOCAL_URL)
VALUES (1,'conectorACCE',false,false,true,'pge','conectorACCE','/service/acce','urn:tokensimple','tag','Produccion','http://testservicios.pge.red.uy:6129/sicews/compras','test-agesic','http://testservicios.pge.red.uy/agesic/sice/compras',2,1,false,true,'https://testservicios.pge.red.uy:6051/TrustServer/SecurityTokenServiceProtected');

INSERT INTO "USER"."CONNECTOR_ROLE_OPERATION" (CONNECTOR_ID,ROLEOPERATIONS_ID) VALUES (1,1);

-------------------------
--CONECTOR para timestamp
INSERT INTO "USER"."CONFIGURATION" (DTYPE,ID,ALIAS_KEYSTORE,DIR_KEYSTORE,DIR_KEYSTORE_ORG,DIR_KEYSTORE_SSL,PASSWORD_KEYSTORE,PASSWORD_KEYSTORE_ORG,PASSWORD_KEYSTORE_SSL,TYPE) VALUES ('LOCAL',3,'testing.hg.red.uy','/home/abrusco/Escritorio/conector_empaquetado/apache-tomcat-8.5.23/conf/agesic_v2.0.keystore','/home/abrusco/Escritorio/conector_empaquetado/apache-tomcat-8.5.23/conf/agesic_v2.0.keystore','/home/abrusco/Escritorio/conector_empaquetado/apache-tomcat-8.5.23/conf/agesic_v1.0.truststore','conector','conector','conector','Produccion');

INSERT INTO "USER"."ROLE_OPERATION" (ID,OPERATION_WSDL,ROLE,WSA_ACTION) VALUES (2,'GetTimestamp','ou=test,o=agesic','action');

INSERT INTO "USER"."USER_CREDENTIALS" (ID,UNT_NAME,UNT_PASSWORD) VALUES (2,'test-agesic','password');

INSERT INTO "USER"."CONNECTOR" (ID,DESCRIPTION,CACHE_CONFIG,LOCAL_CONFIG,USER_CREDENTIALS,ISSUER,NAME,PATH,POLICY_NAME,TAG,TYPE,URL,USERNAME,WSA_TO,LOCALCONFIGURATION_ID,USERCREDENTIALS_ID,SSL_ENABLED,STS_LOCAL_ENABLED,STS_LOCAL_URL)
VALUES (2,'conectorTimestamp',false,false,true,'pge','conectorTimestamp','/service/timeStamp','urn:tokensimple','tag','Produccion','https://testservicios.pge.red.uy:6055/timestamp/TimestampService','test-agesic','http://testservicios.pge.red.uy/timestamp',3,2,false,true,'https://testservicios.pge.red.uy:6051/TrustServer/SecurityTokenServiceProtected');

INSERT INTO "USER"."CONNECTOR_ROLE_OPERATION" (CONNECTOR_ID,ROLEOPERATIONS_ID) VALUES (2,2);
