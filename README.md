# 🚑 helseopplysninger
Backend for forwarding and storing of health-related data

## 💊 tl;dr
Basically an event store exposing a RestAPI with two endpoins for external application. 
One for receiving a search result and one for adding a new message to the event store.
For internal (inside NAV) it exposes two Kafka topics, one for writing and one reading FHIR messages.

## 👽 Technologies used
* Kotlin
* Ktor
* Gradle
* Docker-compose
* Postgres
* Kafka

### 🏭 Building the application
Run `./gradlew build`

## 🏃 To run locally on Mac
To reach the mock oauth2 server you have to make a new mapping in the hosts file.
To modify the /etc/hosts file
* Launch Terminal
* Type sudo nano /etc/hosts and press Return
* Enter your admin password
* Add this new mapping: `127.0.0.1 mock-oauth2-service`

### 🐳 Docker
To run docker-compose from root, use the file flag: `-f .docker/docker-compose.yml` <br/>
Add the flag `-p hops` for naming the composed project.

Start the whole shebang:
```sh
docker-compose -f .docker/docker-compose.yml -p hops up -d
``` 

Stop the whole shebang:
```sh
docker-compose -f .docker/docker-compose.yml -p hops down
```

Start a single app:
```sh
docker-compose -f .docker/docker-compose.yml up eventstore 
```

NOTE: All apps are dependent on `mock-oauth2-service` and `postgres` see [docker-compose.yml](.docker/docker-compose.yml)

### 🐘 Gradle
Recipe for this monorepo:
```sh 
./gradlew apps:${project}:${task}
```

Start a single app:
```sh
./gradlew apps:hops-api:run
```

Build a libraries dependent apps:
```sh
./gradlew libs:hops-common-core:buildDependents
```
 
### 🚀 IntelliJ IDEA
For each app:
* Run -> Edit Configurations
* in the Configuration window click the + button and select Gradle
Set these values:
* Gradle project: `helseopplysninger:apps:hops-eventsinkkafka` (or one of the other apps)
* Tasks: `run`

### 🎨 Starting Kafka administration GUI Kafdrop, and Postgres administration GUI pgAdmin
```sh
docker-compose -f .docker/docker-compose.yml up -d pgadmin kafdrop
```

### 👓 View Kafdrop
After starting the Kafka with docker-compose, go to `localhost:9000`

### 👓 View pgAdmin
After starting the Postgres with docker-compose, go to `localhost:5050`

Log on to pgAdmin with user: `admin@admin.com admin`

Log on to postgres db with user: `Welcome01`
