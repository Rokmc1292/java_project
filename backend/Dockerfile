# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Gradle Wrapper 복사
COPY gradlew .
COPY gradlew.bat .
COPY gradle gradle

# 프로젝트 설정 파일 복사
COPY build.gradle .
COPY settings.gradle .

# 소스 코드 복사
COPY src src

# 빌드
RUN chmod +x gradlew
RUN ./gradlew clean build -x test

# Run stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드된 jar 복사
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

# 메모리 제한 설정으로 OOM 방지
CMD ["java", "-Xmx400m", "-Xms256m", "-jar", "app.jar"]