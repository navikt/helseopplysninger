CREATE SEQUENCE TEST_TABLE_ID_SEQ;

CREATE TABLE TEST_TABLE (
  id                       CHAR(64)           PRIMARY KEY,
  fnr                      VARCHAR(11)        NOT NULL,
  behandler                VARCHAR(50),
  opprettet                TIMESTAMP NOT NULL,
  sist_endret              TIMESTAMP NOT NULL
);
