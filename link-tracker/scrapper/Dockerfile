FROM maven:3.9.12-eclipse-temurin-25 AS build

WORKDIR /workspace
COPY . .

RUN mvn -pl scrapper -am -Dmaven.test.skip=true package

RUN find /workspace/scrapper/target -maxdepth 1 -type f -name '*.jar' ! -name '*original*' \
    | head -n 1 | xargs -I{} cp {} /tmp/app.jar

FROM eclipse-temurin:25-jre

WORKDIR /app
COPY --from=build /tmp/app.jar /app/app.jar

EXPOSE 8081 8091
ENTRYPOINT ["java","-jar","/app/app.jar"]
