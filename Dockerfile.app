FROM navikt/java:16
COPY apps/*/build/libs/*.jar app.jar
