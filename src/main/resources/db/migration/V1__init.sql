CREATE TABLE IF NOT EXISTS "channel" (
    "id" SERIAL,
    "name" VARCHAR(30) NOT NULL,
    UNIQUE ("name"),
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "group" (
    "id" SERIAL,
    "source_id" VARCHAR(50) NOT NULL,
    "channel_id" INTEGER REFERENCES "channel",
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "user" (
    "id" SERIAL,
    "nickname" VARCHAR(50) NOT NULL,
    "title" VARCHAR(100),
    "source_user_id" varchar(50) NOT NULL,
    "group_id" INTEGER REFERENCES "group",
    UNIQUE ("source_user_id", "group_id"),
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "discipline" (
    "id" SERIAL,
    "name" VARCHAR(30) NOT NULL,
    "team_members_count" SMALLINT NOT NULL,
    UNIQUE ("name"),
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "rating" (
    "id" SERIAL,
    "channel_id" INTEGER REFERENCES "channel",
    "group_id" INTEGER REFERENCES "group",
    "discipline_id" INTEGER REFERENCES "discipline",
    "user_id" INTEGER REFERENCES "user",
    "mmr" SMALLINT NOT NULL,
    UNIQUE ("channel_id", "group_id", "discipline_id", "user_id"),
    PRIMARY KEY ("id")
);

CREATE TYPE game_session_state AS ENUM('PREPARING', 'STARTED', 'FINISHED', 'ABORTED');

CREATE TABLE IF NOT EXISTS "game_session" (
    "id" SERIAL,
    "channel_id" INTEGER REFERENCES "channel",
    "group_id" INTEGER REFERENCES "group",
    "discipline_id" INTEGER REFERENCES "discipline",
    "state" game_session_state NOT NULL,
    "date" TIMESTAMPTZ NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "game_session_participant" (
    "game_session_id" INTEGER REFERENCES "game_session",
    "user_id" INTEGER REFERENCES "user"
);



