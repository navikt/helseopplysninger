FROM navikt/java:16 AS app
COPY --from=build /apps/*/build/libs/*.jar app.jar
