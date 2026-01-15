# 1. Dùng Maven để Build code (Sử dụng Java 21 cho khớp với máy bạn)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Dùng OpenJDK để Chạy code
FROM eclipse-temurin:21-jdk
WORKDIR /app
# Copy file .jar vừa build được sang đây
COPY --from=build /app/target/booking-system-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]