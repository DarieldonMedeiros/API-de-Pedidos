FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

RUN apk add --no-cache bash curl unzip

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src ./src

RUN chmod +x mvnw && ./mvnw -B package -DskipTests


FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/pedidos-*-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]