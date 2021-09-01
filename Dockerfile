FROM gradle:7.2.0-jdk16 AS builder
WORKDIR /home
ENV GRADLE_USER_HOME /cache
ARG project

# cache dependencies
COPY build.gradle.kts gradle.properties settings.gradle.kts ./
COPY apps/${project}/build.gradle.kts ./apps/${project}/
COPY libs/hops-common-core/build.gradle.kts ./libs/hops-common-core/
COPY libs/hops-common-fhir/build.gradle.kts ./libs/hops-common-fhir/
COPY libs/hops-common-ktor/build.gradle.kts ./libs/hops-common-ktor/
COPY libs/hops-common-test/build.gradle.kts ./libs/hops-common-test/
RUN gradle --no-daemon dependencies --stacktrace

# cache libs
COPY libs/ libs/
RUN gradle --no-daemon libs:hops-common-core:assemble --stacktrace
RUN gradle --no-daemon libs:hops-common-fhir:assemble --stacktrace
RUN gradle --no-daemon libs:hops-common-ktor:assemble --stacktrace
RUN gradle --no-daemon libs:hops-common-test:assemble --stacktrace

COPY apps/${project} apps/${project}
RUN gradle --no-daemon apps:${project}:shadowJar --stacktrace

FROM navikt/java:16
COPY --from=builder /home/apps/*/build/libs/*.jar app.jar

# HOWTO RUN (local development):
# DOCKER_BUILDKIT=1 docker build --build-arg project=hops-api -t hops/api:local .
