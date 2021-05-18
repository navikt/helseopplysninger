FROM gradle:7.0.2-jdk11 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
ARG project
ARG task=shadowJar
RUN gradle apps:${project}:${task} --no-daemon

FROM navikt/java:11
ARG project
COPY --from=build /home/gradle/src/apps/${project}/build/libs/*.jar app.jar
