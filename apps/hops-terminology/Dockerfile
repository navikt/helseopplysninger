FROM hapiproject/hapi:v5.5.1
ARG dataFolder
COPY ${dataFolder} /usr/local/tomcat/target
COPY application.yaml /data/hapi/application.yaml
RUN chmod -R 777 /usr/local/tomcat/conf/Catalina/
