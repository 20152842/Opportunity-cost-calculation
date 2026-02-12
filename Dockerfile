# Multi-stage build를 사용하여 최적화된 이미지 생성
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 의존성 복사 및 다운로드 (레이어 캐싱 최적화)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 소스 코드 복사 및 빌드
COPY src ./src
RUN mvn clean package -DskipTests

# 실행 단계
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/target/opportunity-cost-calculation-*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
