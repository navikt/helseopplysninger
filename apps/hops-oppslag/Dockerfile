FROM gradle:6.8.0-jdk11 AS build
WORKDIR /tmp/gradle

# Resolve dependencies in a intermediate docker layer that can be cached.
COPY build.gradle.* .
RUN gradle --no-daemon

COPY src .
RUN gradle shadowJar --no-daemon

FROM navikt/java:11
COPY --from=build /tmp/gradle/build/libs/*.jar app.jar