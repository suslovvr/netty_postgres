FROM openjdk:17-jdk-alpine
MAINTAINER vorlon.net
COPY ./target/spring_netty-0.0.1-SNAPSHOT-jar-with-dependencies.jar spring_netty.jar
ENTRYPOINT ["java","-jar","/spring_netty.jar"]
