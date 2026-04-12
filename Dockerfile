# Bước 1: Build file jar bằng Maven
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
# Tải dependency trước để cache
RUN mvn dependency:go-offline
# Copy code và build (bỏ qua test để tiết kiệm RAM/thời gian)
COPY src ./src
RUN mvn clean package -DskipTests

# Bước 2: Chạy app với môi trường Java siêu nhẹ
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy file jar từ bước trên xuống
COPY --from=build /app/target/*.jar app.jar
# Mở port mặc định
EXPOSE 8080
# Lệnh chạy app
ENTRYPOINT ["java", "-jar", "app.jar"]