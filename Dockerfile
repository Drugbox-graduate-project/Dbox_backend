FROM openjdk:11
RUN apk add tzdata
ARG JAR_PATH=./build/libs/*.jar
COPY ${JAR_PATH} app.jar
CMD ["java","-jar","app.jar"]
