FROM openjdk:8-jre-alpine

VOLUME /tmp
ARG JAR_FILE
COPY target/${JAR_FILE} app.jar

EXPOSE 11000

ENTRYPOINT ["java","-jar","app.jar"]