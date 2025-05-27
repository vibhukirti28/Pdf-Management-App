FROM maven:3.8.6-openjdk-17 AS build

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=build /app/target/pdf-management-system-1.0.0.jar app.jar

CMD ["java", "-jar", "app.jar"]
