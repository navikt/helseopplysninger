FROM ghcr.io/navikt/hops-build:latest AS build
COPY . .
ARG project
RUN gradle apps:${project}:shadowJar --no-daemon

FROM navikt/java:16
COPY --from=build /home/gradle/apps/*/build/libs/*.jar app.jar
