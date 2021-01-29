FROM maven:3.6.3-jdk-11-slim as build
WORKDIR /tmp/hapi-fhir-server

COPY pom.xml .
RUN mvn -ntp dependency:go-offline

COPY src/ /tmp/hapi-fhir-server/src/
RUN mvn package

FROM navikt/java:11
COPY --from=build /tmp/hapi-fhir-server/target/*.jar app.jar
