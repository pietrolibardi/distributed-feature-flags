CREATE TABLE feature_flag (
    id UUID PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    enabled BOOLEAN NOT NULL,
    rollout_percentage INT NOT NULL
);
