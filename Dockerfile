# 1. Java 17 실행 환경 설정
FROM eclipse-temurin:17-jdk-jammy

# 2. 빌드된 jar 파일을 컨테이너 내부로 복사
# 빌드 결과물 이름이 다를 수 있으니 build/libs/*.jar 로 지정합니다.
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 3. .env 파일의 환경변수를 인식하며 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]