# 배포 가이드

이 문서는 Opportunity Cost Calculation 애플리케이션을 다양한 플랫폼에 배포하는 방법을 설명합니다.

## 목차

1. [Docker를 사용한 배포](#docker를-사용한-배포)
2. [Heroku 배포](#heroku-배포)
3. [Railway 배포](#railway-배포)
4. [AWS Elastic Beanstalk 배포](#aws-elastic-beanstalk-배포)
5. [Google Cloud Run 배포](#google-cloud-run-배포)
6. [로컬 실행](#로컬-실행)

---

## Docker를 사용한 배포

### 사전 요구사항
- Docker 설치
- Docker Compose 설치 (선택사항)

### 빌드 및 실행

```bash
# 1. Docker 이미지 빌드
docker build -t opportunity-cost-calculation .

# 2. 컨테이너 실행
docker run -p 8080:8080 opportunity-cost-calculation

# 또는 Docker Compose 사용
docker-compose up -d
```

### 접속
- http://localhost:8080

---

## Heroku 배포

### 사전 요구사항
- Heroku 계정
- Heroku CLI 설치
- Git 설치

### 배포 단계

1. **Heroku 앱 생성**
```bash
heroku create your-app-name
```

2. **Procfile 생성** (프로젝트 루트에)
```
web: java -jar target/opportunity-cost-calculation-1.0.0.jar
```

3. **Maven 빌드 설정** (`pom.xml`에 추가)
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>3.1.2</version>
    <executions>
        <execution>
            <id>copy-dependencies</id>
            <phase>package</phase>
            <goals>
                <goal>copy-dependencies</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

4. **배포**
```bash
# Git 초기화 (이미 되어 있다면 생략)
git init
git add .
git commit -m "Initial commit"

# Heroku에 배포
heroku git:remote -a your-app-name
git push heroku main
```

5. **확인**
```bash
heroku open
```

---

## Railway 배포

### 사전 요구사항
- Railway 계정
- GitHub 저장소

### 배포 단계

1. **Railway 프로젝트 생성**
   - Railway 대시보드에서 "New Project" 클릭
   - GitHub 저장소 연결

2. **환경 변수 설정** (선택사항)
   - `PORT`: Railway가 자동으로 설정
   - `SPRING_PROFILES_ACTIVE`: `prod`

3. **배포 설정**
   - Build Command: `mvn clean package -DskipTests`
   - Start Command: `java -jar target/opportunity-cost-calculation-1.0.0.jar`

4. **배포**
   - GitHub에 푸시하면 자동 배포

---

## AWS Elastic Beanstalk 배포

### 사전 요구사항
- AWS 계정
- AWS CLI 설치
- EB CLI 설치

### 배포 단계

1. **EB CLI 초기화**
```bash
eb init -p java-17 opportunity-cost-calculation
```

2. **환경 생성**
```bash
eb create opportunity-cost-env
```

3. **애플리케이션 배포**
```bash
mvn clean package
eb deploy
```

4. **환경 확인**
```bash
eb status
eb open
```

---

## Google Cloud Run 배포

### 사전 요구사항
- Google Cloud 계정
- gcloud CLI 설치

### 배포 단계

1. **프로젝트 설정**
```bash
gcloud config set project YOUR_PROJECT_ID
```

2. **Docker 이미지 빌드 및 푸시**
```bash
# 이미지 빌드
docker build -t gcr.io/YOUR_PROJECT_ID/opportunity-cost-calculation .

# 이미지 푸시
docker push gcr.io/YOUR_PROJECT_ID/opportunity-cost-calculation
```

3. **Cloud Run에 배포**
```bash
gcloud run deploy opportunity-cost-calculation \
  --image gcr.io/YOUR_PROJECT_ID/opportunity-cost-calculation \
  --platform managed \
  --region asia-northeast3 \
  --allow-unauthenticated \
  --port 8080
```

---

## 로컬 실행

### 사전 요구사항
- Java 17 이상
- Maven 3.6 이상

### 실행 단계

```bash
# 1. 프로젝트 빌드
mvn clean package

# 2. JAR 파일 실행
java -jar target/opportunity-cost-calculation-1.0.0.jar

# 또는 Maven으로 직접 실행
mvn spring-boot:run
```

### 접속
- http://localhost:8080

---

## 환경 변수 설정

프로덕션 환경에서는 다음 환경 변수를 설정할 수 있습니다:

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `SERVER_PORT` | 서버 포트 | 8080 |
| `SPRING_PROFILES_ACTIVE` | 활성 프로파일 | default |
| `LOG_LEVEL` | 로그 레벨 | INFO |

---

## 프로덕션 설정 권장사항

1. **로깅**
   - 프로덕션에서는 로그 레벨을 `WARN` 또는 `ERROR`로 설정
   - 로그 파일을 외부 스토리지에 저장

2. **모니터링**
   - 애플리케이션 메트릭 수집 (Prometheus, CloudWatch 등)
   - 헬스 체크 엔드포인트 활용

3. **보안**
   - HTTPS 사용
   - CORS 설정 제한
   - 입력 검증 강화

4. **성능**
   - JVM 튜닝 (힙 메모리 설정 등)
   - 데이터베이스 연결 풀 설정 (필요 시)

---

## 문제 해결

### 포트 충돌
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

### 메모리 부족
```bash
# JVM 힙 메모리 증가
java -Xmx512m -jar target/opportunity-cost-calculation-1.0.0.jar
```

### 빌드 실패
```bash
# Maven 캐시 정리 후 재빌드
mvn clean
rm -rf ~/.m2/repository
mvn clean install
```

---

## 추가 리소스

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Docker 공식 문서](https://docs.docker.com/)
- [Heroku Java 가이드](https://devcenter.heroku.com/articles/getting-started-with-java)
- [Railway 문서](https://docs.railway.app/)
