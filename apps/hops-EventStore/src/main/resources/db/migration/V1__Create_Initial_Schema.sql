CREATE TABLE event (
    id BIGSERIAL PRIMARY KEY,
    bundle_id uuid NOT NULL UNIQUE,
    message_id uuid NOT NULL UNIQUE,
    event_type VARCHAR(200) NOT NULL,
    bundle_timestamp TIMESTAMP NOT NULL,
    recorded TIMESTAMP NOT NULL,
    source VARCHAR(200) NOT NULL,
    data TEXT NOT NULL,
    data_type VARCHAR(100) NOT NULL,
    data_bytes INT NOT NULL);

CREATE INDEX event_event_type ON event (event_type);
CREATE INDEX event_bundle_timestamp ON event (bundle_timestamp);
CREATE INDEX event_source ON event (source);
CREATE INDEX event_data_type ON event (data_type);

CREATE TABLE destination (
    id SERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    endpoint VARCHAR(200) NOT NULL,
    CONSTRAINT fk_destination_event_id_id FOREIGN KEY (event_id) REFERENCES event (id) ON DELETE RESTRICT ON UPDATE RESTRICT);

CREATE INDEX destination_endpoint ON destination (endpoint);
