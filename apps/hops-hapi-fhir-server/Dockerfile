FROM hapiproject/hapi:latest
COPY apps/hops-hapi-fhir-server/application.yaml /usr/local/etc/
ENV spring.config.location=/usr/local/etc/
CMD ["catalina.sh", "run"]