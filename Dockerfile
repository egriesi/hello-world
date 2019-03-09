FROM tomcat:8.5-jre8
COPY /home/egriesi/hello-world/target/hello-world-war-1.0.0.war hello-world-war-1.0.0 /usr/local/tomcat/webapps/
CMD ["catalina.sh", "run"]