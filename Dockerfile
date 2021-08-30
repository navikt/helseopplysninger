# Used to create a source-tree containing only Gradle files.
# Needed because 'docker copy' does not support combining recursive copy and pattern matching.
FROM gradle:7.2.0-jdk16 AS gradle-files
COPY . .
RUN find . ! -name "*.kts" ! -name "gradle.properties" -delete; echo 0

# Pulls all maven dependencies.
# Is only rebuilt if a file from the 'gradle-files' stage is modified.
FROM gradle:7.2.0-jdk16 AS cache
COPY --from=gradle-files /home/gradle .
RUN gradle assemble --no-daemon

# Uses the maven packages from the 'cache' stage and builds the specified project.
FROM gradle:7.2.0-jdk16 AS build
COPY --from=cache /root/.gradle /root/.gradle
COPY . .
ARG project
RUN gradle apps:${project}:shadowJar --no-daemon

# Hardened navikt production image.
# Contains only the fat JAR from the 'build' stage, no other build artifact.
FROM navikt/java:16
COPY --from=build /home/gradle/apps/*/build/libs/*.jar app.jar
