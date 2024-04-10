FROM openjdk:11
ENV TZ=Asia/Seoul
RUN apt-get install -y tzdata
ARG JAR_PATH=./build/libs/*.jar
COPY ${JAR_PATH} app.jar
CMD ["java","-jar","app.jar"]
