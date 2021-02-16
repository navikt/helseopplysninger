FROM gradle:6.8.0-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle test shadowJar --no-daemon

FROM navikt/java:11
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar