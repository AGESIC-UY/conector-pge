/* timeout error handling test data */
INSERT INTO "USER"."CONFIGURATION" (DTYPE,ID,ALIAS_KEYSTORE,DIR_KEYSTORE,DIR_KEYSTORE_ORG,DIR_KEYSTORE_SSL,PASSWORD_KEYSTORE,PASSWORD_KEYSTORE_ORG,PASSWORD_KEYSTORE_SSL,TYPE) 
VALUES ('GLOBAL',5,'testing.hg.red.uy','C:/temp/conector-2.5/agesic_v2.0.keystore','C:/temp/conector-2.5/agesic_v2.0.keystore','C:/temp/conector-2.5/keystores/agesic_v1.0.truststore',
'conector','conector','conector','Produccion');
INSERT INTO "USER"."CONNECTOR" (ID,DESCRIPTION,CACHE_CONFIG,ISSUER,NAME,PATH,POLICY_NAME,TAG,TYPE,URL,USERNAME,WSA_TO,WSDL_DIR,LOCALCONFIGURATION_ID,USERCREDENTIALS_ID) 
VALUES (4,'conectorAle',1,'pge','conectorAle','/service/getConnector/4','urn:tokensimple','tag','Produccion','http://localhost:28080/connector-runtime/service/getConnector/4',
'test-agesic','http://testservicios.pge.red.uy/timestamp',null,null,1);
INSERT INTO "USER"."ROLE_OPERATION" (ID,OPERATION_WSDL,ROLE,WSA_ACTION) VALUES (4,'GetTimestamp','ou=test,o=agesic','action');
INSERT INTO "USER"."CONNECTOR_ROLE_OPERATION" (CONNECTOR_ID,ROLEOPERATIONS_ID) VALUES (4,4);
INSERT INTO "USER"."USER_CREDENTIALS" (ID,UNT_NAME,UNT_PASSWORD) VALUES (4,'test-agesic','password');

/* connection refused */
INSERT INTO "USER"."CONFIGURATION" (DTYPE,ID,ALIAS_KEYSTORE,DIR_KEYSTORE,DIR_KEYSTORE_ORG,DIR_KEYSTORE_SSL,PASSWORD_KEYSTORE,PASSWORD_KEYSTORE_ORG,PASSWORD_KEYSTORE_SSL,TYPE) 
VALUES ('GLOBAL',6,'testing.hg.red.uy','C:/temp/conector-2.5/agesic_v2.0.keystore','C:/temp/conector-2.5/agesic_v2.0.keystore','C:/temp/conector-2.5/keystores/agesic_v1.0.truststore',
'conector','conector','conector','Produccion');
INSERT INTO "USER"."CONNECTOR" (ID,DESCRIPTION,CACHE_CONFIG,ISSUER,NAME,PATH,POLICY_NAME,TAG,TYPE,URL,USERNAME,WSA_TO,WSDL_DIR,LOCALCONFIGURATION_ID,USERCREDENTIALS_ID) 
VALUES (6,'conectorAle',1,'pge','conectorAle','/service/getConnector/6','urn:tokensimple','tag','Produccion','http://localhost:18088/mockTimestampServiceBinding',
'test-agesic','http://testservicios.pge.red.uy/timestamp',null,null,1);
INSERT INTO "USER"."ROLE_OPERATION" (ID,OPERATION_WSDL,ROLE,WSA_ACTION) VALUES (6,'GetTimestamp','ou=test,o=agesic','action');
INSERT INTO "USER"."CONNECTOR_ROLE_OPERATION" (CONNECTOR_ID,ROLEOPERATIONS_ID) VALUES (6,6);
INSERT INTO "USER"."USER_CREDENTIALS" (ID,UNT_NAME,UNT_PASSWORD) VALUES (6,'test-agesic','password');

/* Unknown host */
INSERT INTO "USER"."CONFIGURATION" (DTYPE,ID,ALIAS_KEYSTORE,DIR_KEYSTORE,DIR_KEYSTORE_ORG,DIR_KEYSTORE_SSL,PASSWORD_KEYSTORE,PASSWORD_KEYSTORE_ORG,PASSWORD_KEYSTORE_SSL,TYPE) 
VALUES ('GLOBAL',7,'testing.hg.red.uy','C:/temp/conector-2.5/agesic_v2.0.keystore','C:/temp/conector-2.5/agesic_v2.0.keystore','C:/temp/conector-2.5/keystores/agesic_v1.0.truststore',
'conector','conector','conector','Produccion');
INSERT INTO "USER"."CONNECTOR" (ID,DESCRIPTION,CACHE_CONFIG,ISSUER,NAME,PATH,POLICY_NAME,TAG,TYPE,URL,USERNAME,WSA_TO,WSDL_DIR,LOCALCONFIGURATION_ID,USERCREDENTIALS_ID) 
VALUES (7,'conectorAle',1,'pge','conectorAle','/service/getConnector/7','urn:tokensimple','tag','Produccion','http://unknownhost:18088/mockTimestampServiceBinding',
'test-agesic','http://testservicios.pge.red.uy/timestamp',null,null,1);
INSERT INTO "USER"."ROLE_OPERATION" (ID,OPERATION_WSDL,ROLE,WSA_ACTION) VALUES (7,'GetTimestamp','ou=test,o=agesic','action');
INSERT INTO "USER"."CONNECTOR_ROLE_OPERATION" (CONNECTOR_ID,ROLEOPERATIONS_ID) VALUES (7,7);
INSERT INTO "USER"."USER_CREDENTIALS" (ID,UNT_NAME,UNT_PASSWORD) VALUES (7,'test-agesic','password');