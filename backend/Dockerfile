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

# Chromium 및 ChromeDriver 설치 (크롤링용)
RUN apk add --no-cache \
    chromium \
    chromium-chromedriver \
    nss \
    freetype \
    harfbuzz \
    ca-certificates \
    ttf-freefont \
    font-noto-cjk

# Chromium 환경 변수 설정
ENV CHROME_BIN=/usr/bin/chromium-browser \
    CHROME_DRIVER=/usr/bin/chromedriver

# 빌드된 jar 복사
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

# 유료 플랜용 메모리 설정 (더 넉넉하게)
CMD ["java", "-Xmx1024m", "-Xms512m", "-jar", "app.jar"]