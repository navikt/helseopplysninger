FROM gradle:7.2.0-jdk16 AS cache
WORKDIR /home
ENV GRADLE_USER_HOME /cache
COPY build.gradle.kts gradle.properties settings.gradle.kts ./

COPY libs/hops-common-core/*.kts ./libs/hops-common-core/
COPY libs/hops-common-fhir/*.kts ./libs/hops-common-fhir/
COPY libs/hops-common-ktor/*.kts ./libs/hops-common-ktor/
COPY libs/hops-common-test/*.kts ./libs/hops-common-test/

COPY apps/hops-api/*.kts ./apps/hops-api/
COPY apps/hops-eventreplaykafka/*.kts ./apps/hops-eventreplaykafka/
COPY apps/hops-eventsinkkafka/*.kts ./apps/hops-eventsinkkafka/
COPY apps/hops-eventstore/*.kts ./apps/hops-eventstore/
COPY apps/hops-fileshare/*.kts ./apps/hops-fileshare/
COPY apps/hops-test-external/*.kts ./apps/hops-test-external/

RUN gradle --no-daemon dependencies --stacktrace

# DOCKER_BUILDKIT=1 docker build --target cache -t ghcr.io/navikt/hops-dependency-cache:latest .

FROM gradle:7.2.0-jdk16 AS build
WORKDIR /home
ENV GRADLE_USER_HOME /cache
COPY --from=cache /cache /cache
COPY . .
ARG project
RUN gradle apps:${project}:shadowJar --no-daemon --stacktrace

FROM navikt/java:16 AS app
COPY --from=build /home/apps/*/build/libs/*.jar app.jar

# DOCKER_BUILDKIT=1 docker build --target app --build-arg project=hops-api --cache-from=ghcr.io/navikt/hops-dependency-cache:latest -t local/hops-api:latest .
