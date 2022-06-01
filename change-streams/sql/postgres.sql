CREATE TABLE IF NOT EXISTS status_code_counts (
    status_code VARCHAR (255) NOT NULL,
    count bigint NOT NULL,
    PRIMARY KEY (status_code)
);
