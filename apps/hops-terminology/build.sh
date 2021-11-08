#!/bin/sh
die () {
    echo >&2 "$@"
    if [ -n "$container" ]; then
      docker stop "$container" && docker rm "$container"
    fi
    exit 1
}

if [ -z "$1" ]
  then
    echo "No argument supplied: you must supply the location you want the database to be built at"
fi
tempDir=$1
container=$(docker run -p 8080:8080 -d -v "$(pwd)"/application.yaml:/data/hapi/application.yaml -v "$tempDir":/usr/local/tomcat/target -e "--spring.config.location=file:///data/hapi/application.yaml" hapiproject/hapi:v5.5.1) || die "Could not start hapi container"

if [ ! -f icd10cm_tabular_2021.xml ]; then
  wget https://ftp.cdc.gov/pub/health_statistics/nchs/publications/ICD10CM/2021/icd10cm_tabular_2021.xml || die "Could not download terminologies"
fi
if [ ! -f hapi-fhir-cli ]; then
  (wget https://github.com/hapifhir/hapi-fhir/releases/download/v5.5.1/hapi-fhir-5.5.1-cli.tar.bz2 && tar -xf hapi-fhir-5.5.1-cli.tar.bz2) || die "Could not download hapi CLI"
fi


echo "Loading terminologies..."
./hapi-fhir-cli upload-terminology -d ./icd10cm_tabular_2021.xml -v r4 -t http://localhost:8080/fhir -u http://hl7.org/fhir/sid/icd-10-cm || die "Could not load terminologies"
echo "Waiting for deferred operation to complete..."
sleep 30
curl -X POST http://localhost:8080/fhir/ValueSet \
-H 'Content-Type: application/json' \
-d '{"resourceType":"ValueSet","url":"http://fhir.nav.no/ValueSet/icd-10-cm","name":"ICD 10 CM","title":"Full ICD 10 CM","compose":{"include":[{"system":"http://hl7.org/fhir/sid/icd-10-cm"}]}}'
echo "Waiting deferred pre-expand..."
sleep 120


echo "Verifying server is ready..."
expandCount=0
getExpandCount() {
  echo "Calling expand..."
  expand=$(curl --silent --request GET 'http://localhost:8080/fhir/ValueSet/$expand?url=http://fhir.nav.no/ValueSet/icd-10-cm')
  echo "$expand"
  expandCount=$(echo "$expand" | jq -r '.expansion.total')
}

getExpandCount
maxWaits=60
waitCount=0
while [ $((expandCount)) -eq 0 ] && [ $((waitCount)) -lt $((maxWaits)) ]; do
  echo "$waitCount: Received $expandCount codes. Waiting for expand to return codes..."
  waitCount=$((waitCount + 1))
  if [ $waitCount -eq $maxWaits ]; then
    die "Waited $(( waitCount * 5 )) seconds and did not get codes when expanding the value set"
  fi
  sleep 5
  getExpandCount
done

docker stop "$container" && docker rm "$container"
