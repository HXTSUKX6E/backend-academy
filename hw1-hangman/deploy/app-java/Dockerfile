ARG CI_IMAGE=maven:3.9.11-eclipse-temurin-24-noble
ARG RUNTIME_IMAGE="$CI_IMAGE"

FROM $CI_IMAGE AS builder
ARG SRC_DIR=./app-java
WORKDIR /app
COPY $SRC_DIR/src ./src
COPY $SRC_DIR/config ./config
COPY $SRC_DIR/pom.xml .
RUN mvn -f pom.xml clean package -DskipTests=true -Djacoco.skip=true

FROM $RUNTIME_IMAGE AS runner
USER 2000
ARG BUILDER_APP_DIR=/app
ARG APP_HOME=/app
WORKDIR $APP_HOME
COPY --from=builder $BUILDER_APP_DIR/target/hangman-*.jar ./app.jar
ENV LOG_DIR=/var/tmp/app-logs
RUN mkdir -p "$LOG_DIR"
ENTRYPOINT ["java", "-jar", "app.jar"]
