FROM openjdk:11.0.11-jre
COPY build/libs/keymanager-grpc-0.1-all.jar /app/app.jar
WORKDIR /app
EXPOSE 50051
CMD ["java", "-jar", "app.jar"]