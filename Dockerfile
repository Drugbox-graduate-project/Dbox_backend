FROM openjdk:11
ARG JAR_PATH=./build/libs/*.jar
COPY ${JAR_PATH} app.jar
CMD ["java","-jar","app.jar"]
