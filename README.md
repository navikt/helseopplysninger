# 🚑 Helseopplysninger
Backend for forwarding and storing of health-related data

## 🔗 Associated repositories
- [navikt/helseopplysninger-frontend](https://github.com/navikt/helseopplysninger-frontend): Monorepo for React Front-end applications.
- [navikt/fhir](https://github.com/navikt/fhir): Monorepo for FHIR Implementation Guides (IG).
- [navikt/fhir-questionnaires](https://github.com/navikt/fhir-questionnaires): Monorepo for FHIR Questionnaire resources.
- [navikt/fhir-validator-junit-engine](https://github.com/navikt/fhir-validator-junit-engine): Test framework for testing FHIR Conformance Resources (profiles).

## 📁 Folder structure
```yaml
helseopplysninger
├── .docker
│   ├── builder             # Dockerfile used to create a cache-image for multistage builds
│   ├── config              # Config files used by services in docker-compose
│   ├── test                # Python script to publish fhir-messages on kafka, used for for local testing
│   └── docker-compose.yml  # Setup a complete environment for local testing
├── .github
│   ├── ISSUE_TEMPLATE      # Templates used when creating issues on GitHub
│   ├── workflows           # GitHub-action workflows used for CI/CD
│   └── dependabot.yml      # Instructions regarding auto-updating for dependencies
├── .scripts 
│   ├── licence-to-sarif.js # Node-script to convert qodana-license-audit-report to sarif format
│   └── run-e2e             # Bash script to run end-2-end tests
├── apps                    # The various applications\microservices (gradle-projects)
├── docs
│   ├── adrs                # Architecture Decision Records
│   ├── images              # Images used in documentation
│   ├── pipeline            # CI/CD pipeline documentation
│   ├── test                # Test strategy documentation
│   └── _config.yml         # Github-Pages config
├── kafka                   # NAIS templates for kafka topics
├── libs                    # Common libraries (gradle-projects) used by the applications
├── .dockerignore 
├── .editorconfig           # IntelliJ IDEA code style settings
├── .gitattributes
├── .gitignore 
├── CODEOWNERS 
├── Dockerfile              # Multistage dockerfile used to create deployable docker-image. Used by all apps
├── LICENSE.md              # MIT license
├── README.md
├── SECURITY.md             # Instructions regarding security practices
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle.kts
```

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
docker-compose -f .docker/docker-compose.yml up hops-eventstore 
```

When using docker-compose, the services are configured to use a proxy, and will therefore be available on `http://<app name>.local.gl:8080` e.g:
> http://hops-api.local.gl:8080 <br>
> http://hops-eventstore.local.gl:8080 <br>
> http://hops-fileshare.local.gl:8080 <br>

You can set up a proxy for any service in docker-compose with the following config:
```yaml
services:
  some-service:
    expose: [8080]
    environment:
      VIRTUAL_HOST: some-service.local.gl
      VIRTUAL_PORT: 8080
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
