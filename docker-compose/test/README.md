# Publishing FHIR Messages on Kafka

While running the kafka-service from docker-compose FHIR messages can be published using 
the Python script: _kafka-producer-basic.py_

Running the script requires python3 and the python3-kafka module. This will install these
modules system wide.

`sudo apt-get install python3 python3-kafka`

On mac with homebrew:
`brew install python3 && pip3 install kafka-python`

Write the FHIR message to a file and run the script with the filename as an argument, e.g.:

`python3 kafka-producer-basic.py bestilling.json`

## Generate autotest-file
To be continued...
