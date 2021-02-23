FROM gradle:6.8.2-jdk11 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
RUN gradle bootJar --no-daemon

FROM navikt/java:11
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar