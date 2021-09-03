# This Dockerfile is used to create a docker-image containing the Gradle cache including Maven packages.
FROM gradle:7.2.0-jdk16 AS build
COPY . .
RUN find . ! -name "*.kts" ! -name "gradle.properties" -delete; echo 0

FROM gradle:7.2.0-jdk16
COPY --from=build /home/gradle .
RUN gradle assemble --no-daemon && rm -rf *
