FROM centos:centos8.3.2011
LABEL Author="Ivan Viamonte"

#Actualizar servidor
USER root
RUN dnf update -y && dnf upgrade -y && dnf clean all

#Java JDK-13
RUN cd /tmp
RUN curl -O https://download.java.net/java/GA/jdk13/5b8a42f3905b406298b72d750b6919f6/33/GPL/openjdk-13_linux-x64_bin.tar.gz
RUN tar xf openjdk-13_linux-x64_bin.tar.gz
RUN mv jdk-13 /opt/

#Variables de entorno
ENV JAVA_HOME=/opt/jdk-13
ENV WILDFLY_VERSION=24.0.1.Final
ENV JBOSS_HOME=/opt/wildfly

#WildFly instalacion y permisos
RUN curl -O https://download.jboss.org/wildfly/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz 
RUN tar xf wildfly-$WILDFLY_VERSION.tar.gz 
RUN mv wildfly-$WILDFLY_VERSION $JBOSS_HOME 
RUN rm -f wildfly-$WILDFLY_VERSION.tar.gz 
RUN chmod -R g+rw ${JBOSS_HOME}

#Exponer puertos
EXPOSE 8080 9800 9700

#Copia de archivos de configuracion
RUN cd $HOME
COPY confWildfly/bin/* $JBOSS_HOME/bin/
COPY confWildfly/conf/* $JBOSS_HOME/standalone/configuration/
COPY confWildfly/connector $JBOSS_HOME/standalone/connector
COPY confWildfly/deployments/* $JBOSS_HOME/standalone/deployments/
COPY confWildfly/derby $JBOSS_HOME/derby
COPY confWildfly/modules/org $JBOSS_HOME/modules/org

#Ejecucion
CMD cd /opt/wildfly/bin/ && sh standalone.sh -b 0.0.0.0
