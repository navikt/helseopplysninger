CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    bundle_id uuid NOT NULL UNIQUE,
    message_id uuid NOT NULL UNIQUE,
    event_type VARCHAR(200) NOT NULL,
    bundle_timestamp TIMESTAMP NOT NULL,
    recorded TIMESTAMP NOT NULL,
    "source" VARCHAR(200) NOT NULL,
    "data" TEXT NOT NULL,
    data_type VARCHAR(100) NOT NULL,
    data_bytes INT NOT NULL);

CREATE INDEX events_event_type ON events (event_type);
CREATE INDEX events_bundle_timestamp ON events (bundle_timestamp);
CREATE INDEX events_source ON events ("source");
CREATE INDEX events_data_type ON events (data_type);

CREATE TABLE destination (
    id SERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    endpoint VARCHAR(200) NOT NULL,
    CONSTRAINT fk_destination_event_id_id FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE RESTRICT ON UPDATE RESTRICT);

CREATE INDEX destination_endpoint ON destination (endpoint);
