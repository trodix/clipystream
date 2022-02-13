FROM openjdk:17-jdk-alpine
WORKDIR /opt/clipystream
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","app.jar"]
