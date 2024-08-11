CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

ALTER TABLE "game_session"
    ADD "session_uuid" UUID NOT NULL DEFAULT (uuid_generate_v4());

ALTER TABLE "game_session_participant"
    ADD "team_number" SMALLINT;