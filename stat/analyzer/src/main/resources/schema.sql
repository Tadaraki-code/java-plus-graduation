CREATE TABLE IF NOT EXISTS event_similarity (
    eventA_id BIGINT NOT NULL,
    eventB_id BIGINT NOT NULL,
    score NUMERIC(5,2) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    PRIMARY KEY (eventA_id, eventB_id)
);

CREATE TABLE IF NOT EXISTS user_action (
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    weight NUMERIC(5,2) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, event_id)
);