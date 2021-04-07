# Hapi fhir server
Backend for forwarding and storing of health-related data

## Technologies (to be) used
* Java
* Gradle
* Docker-compose
* Postgres

## To run locally on Mac 
To reach the mock oaut2 server you have to make a new mapping in the hosts file.
To modify the /etc/hosts file
* Launch Terminal
* Type sudo nano /etc/hosts and press Return
* Enter your admin password
* Add this new mapping: `127.0.0.1 mock-oauth2-service`

### Edit the configurations in IntelliJ:
(Run -> Edit Configurations)

Set these values:
* Gradle project: `helseopplysninger:apps:hops-hapi-fhir-server`
* Tasks: `bootRun`
* Environment variables: `SPRING_PROFILES_ACTIVE=local`

### To start up the server with docker-compose
In a terminal run: `docker-compose up hops-hapi-service`

You can start all the applications in this mono repo by running: 

`docker-compose up` (no argument)

### Check the open endpoint
http://localhost:8084/fhir/metadata

You should then get at `200 OK` and a (very long) response JSON

### Test the secured endpoint
You need Postman or something equivalent.

In Postman:
* Make a `get` request to `http://localhost:8084/fhir/Patient`
* Click the `Authorization` tab
* In the `Type` dropdown, select `OAuth 2.0`
* In the `Add authorization data to` dropdown, select `Request Headers`
* In the `Header Prefix` field, write `Bearer`
* In the `Token name` field, write `anything`
* In the `Grant type` dropdown, select `Client Credentials`
* In the `Access Token URL` field, write `http://mock-oauth2-service:8081/default/token`
* In the `Client ID` field, write `anything`
* In the `Client Secret` field, write `anything`
* In the `Client Authentication` dropdown, select `Send as Basic Auth header`
* Click the `Get New Access Token` button
* Click `Send`

You should then get at `200 OK` and a response JSON