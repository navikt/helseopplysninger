from kafka import KafkaProducer
from json import dumps
import json
import sys

for fileName in sys.argv[1:]:
    print("Reading file: " + fileName)
    file = open(fileName, "r")
    data = json.load(file)

    producer = KafkaProducer(
        value_serializer=lambda m: dumps(m).encode('utf-8'),
        bootstrap_servers=['localhost:9092'])

    future = producer.send("helseopplysninger.bestilling", value=data)
    result = future.get(timeout=60)
    print(result)
