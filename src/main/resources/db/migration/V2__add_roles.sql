ALTER TABLE "game_session"
    ADD "initiator_id" INTEGER REFERENCES "_user";

CREATE TYPE "user_role" AS ENUM('ADMIN', 'MODERATOR');

ALTER TABLE "_user"
    ADD "role" user_role;