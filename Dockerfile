# Stage 1: Build the application
# Pinned by digest (OSSF Scorecard Pinned-Dependencies). Refresh occasionally with
# `docker manifest inspect gradle:8.5-jdk21` and update the digest below.
FROM gradle:8.5-jdk21@sha256:873def2b8a73d00f5616043ac9ff65576b055942701391ad66499f1a1f53f1b2 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle build -x test --no-daemon

# Stage 2: Run the application
# Pinned by digest. Refresh with `docker manifest inspect eclipse-temurin:21-jre-jammy`.
FROM eclipse-temurin:21-jre-jammy@sha256:199aebeb3adcde4910695cdebfe782ada38dadb6cc8013159b58d3724451befd
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
