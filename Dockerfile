FROM maven:3.8.5-openjdk-17 AS build

COPY . .

RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim

COPY --from=build /app/target/pdf-management-system-1.0.0.jar app.jar
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
