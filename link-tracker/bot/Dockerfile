FROM maven:3.9.12-eclipse-temurin-25 AS build

WORKDIR /workspace
COPY . .

RUN mvn -pl bot -am -Dmaven.test.skip=true package

FROM eclipse-temurin:25-jre

WORKDIR /app
COPY --from=build /workspace/bot/target/bot-0.0.1.jar /app/app.jar

EXPOSE 8080 8011 8090
ENTRYPOINT ["java","-jar","/app/app.jar"]
