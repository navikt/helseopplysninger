# helseopplysninger
Backend for forwarding and storing of health-related data

## tl;dr
Basically an event store exposing a RestAPI with two endpoins for external application. 
One for receiving a search result and one for adding a new message to the event store.
For internal (inside NAV) it exposes two Kafka topics, one for writing and one reading FHIR messages.

## Technologies used
* Kotlin
* Ktor
* Gradle
* Docker-compose
* Postgres
* Kafka

### Building the application
Run `./gradlew build`

## To run locally on Mac
To reach the mock oaut2 server you have to make a new mapping in the hosts file.
To modify the /etc/hosts file
* Launch Terminal
* Type sudo nano /etc/hosts and press Return
* Enter your admin password
* Add this new mapping: `127.0.0.1 mock-oauth2-service`

### To start up with docker-compose
In a terminal, go to the docker-compose catalog.

You can start all the applications in this mono repo by running:

`docker-compose up -d` 

To run one of the applications in the mono repo, in this example hops-eventstore, 
in a terminal run: `docker-compose up eventstore`

NOTE: All apps needs to have `mock-oauth2-service` and `postgres` 
running (`docker-compose up mock-oauth2-service`), see [docker-compose.yml](./docker-compose/docker-compose.yml)


You can see all your containers running in Docker Desktop 
(has to be installed https://www.docker.com/products/docker-desktop )
 
### To run applications in IntelliJ, edit the configurations:
For each app:
* Run -> Edit Configurations
* in the Configuration window click the + button and select Gradle
Set these values:
* Gradle project: `helseopplysninger:apps:hops-eventsinkkafka` (or one of the other apps)
* Tasks: `run`
* Environment variables: `SPRING_PROFILES_ACTIVE=local`

### Test the endpoints in API
If API is started from IntelliJ: Go to localhost:8080

If you started from Docker-Compose: Go to localhost:8085 (or, from Docker Desktop click the "View in browser" button)

Try the unsecured /isAlive or /isReady.
You should then get at `200 OK`

###To try the secured endpoints 
For trying the /fhir/4.0/Bundle {GET} or /fhir/4.0/$prosess-message {POST}
you will need to use Postman or something equivalent.

In Postman: See [screen dump](./docs/PostmanDump.png)

* Make a `get` request to `http://localhost:8085/fhir/4.0/Bundle`
* Click the `Authorization` tab
* In the `Type` dropdown, select `OAuth 2.0`
* In the `Add authorization data to` dropdown, select `Request Headers`
* In the `Header Prefix` field, write `Bearer`
* In the `Token name` field, write `anything`
* In the `Grant type` dropdown, select `Client Credentials`
* In the `Access Token URL` field, write `http://mock-oauth2-service:8081/maskinporten/token`
* In the `Client ID` field, write `anything`
* In the `Client Secret` field, write `anything`
* In the `Client Scope` field, write `hops`
* In the `Client Authentication` dropdown, select `Send as Basic Auth header`
* Click the `Get New Access Token` button
* Click `Send`

You should then get at `200 OK` and a response JSON

Note: You can limit the access by setting `Scope= hops:sub` to only be able to use the `get` endpoint
and `Scope= hops:pub` to only use the `post` endpoint

If you are testing the EventStore directly 
(or other apps that are not reachable externally) you have to use the "internal token":
`http://mock-oauth2-service:8081/default/token` with `Scope = eventstore`

### Starting Kafka administration GUI Kafdrop, and Postgres administration GUI pgAdmin
`docker-compose up -d pgadmin kafdrop`

### View Kafdrop
After starting the Kafka with docker-compose, go to `localhost:9000`

### View pgAdmin
After starting the Postgres with docker-compose, go to `localhost:5050`

Log on to pgAdmin with user: `admin@admin.com admin`

Log on to postgres db with user: `Welcome01`

