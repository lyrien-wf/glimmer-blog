# ===== 构建阶段 =====
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# ===== 运行阶段 =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 创建非 root 用户
RUN addgroup -S blog && adduser -S blog -G blog

# 复制 JAR
COPY --from=builder /build/target/*.jar app.jar

# 创建上传目录
RUN mkdir -p /app/uploads && chown -R blog:blog /app

USER blog

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Xms256m", "-Xmx512m", "app.jar"]
