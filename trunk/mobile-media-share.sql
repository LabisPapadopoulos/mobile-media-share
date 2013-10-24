-- ------------------------------------------------------------------
-- Ta kanei o diaxeirisths xrhsths postgres (psql san postgres)
-- Xrhsths mobile-media-share
DROP USER "mobile-media-share";
CREATE USER "mobile-media-share" WITH PASSWORD 'medi@sh@re';

-- Bash mobile-media-share
DROP DATABASE IF EXISTS "mobile-media-share";
CREATE DATABASE "mobile-media-share" OWNER "mobile-media-share";
-- -------------------------------------------------------------------

-- psql -h localhost -U 'mobile-media-share' -W (ta kanei o xrhsths mobile-media-share)
-- Pinakes

-- Drop ton Media prwta epeidh exei foreign key ston Users
DROP TABLE IF EXISTS Media;

-- Pinakas Eggegramenwn xrhstwn
DROP TABLE IF EXISTS Users;

CREATE TABLE Users (
	email VARCHAR(32) NOT NULL PRIMARY KEY,
	password CHAR(32) NOT NULL,
	name VARCHAR(32),
	photo VARCHAR(32)
);

-- Pinakas Fotografiwn, videos...
CREATE TABLE Media (
	id VARCHAR(32) NOT NULL PRIMARY KEY,
	type VARCHAR(32) NOT NULL,
	title VARCHAR(32) NOT NULL,
	latitude DECIMAL(9, 6) NOT NULL,
	longitude DECIMAL(9, 6) NOT NULL,
	created TIMESTAMP NOT NULL, -- pote dhmiourghthike
	edited TIMESTAMP NOT NULL, -- pote allaxe
	"user" VARCHAR(32) NOT NULL,
	public BOOLEAN NOT NULL, -- 'h public 'h private
	size INT NOT NULL,
	duration INT NOT NULL, -- diarkeia gia video/hxo
	FOREIGN KEY ("user") REFERENCES Users(email) ON UPDATE CASCADE ON DELETE CASCADE
);

