FROM gradle:7.2.0-jdk16 AS gradle-files
COPY . .
RUN find . ! -name "*.kts" ! -name "gradle.properties" -delete; echo 0

FROM gradle:7.2.0-jdk16 AS build
COPY --from=gradle-files /home/gradle .
RUN gradle assemble --no-daemon
COPY . .
ARG project
RUN gradle apps:${project}:shadowJar --no-daemon

FROM navikt/java:16
COPY --from=build /home/gradle/apps/*/build/libs/*.jar app.jar
