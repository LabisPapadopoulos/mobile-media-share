-- ------------------------------------------------------------------
-- Ta kanei o diaxeirisths xrhsths postgres (psql san postgres xwris na exei mpei se sygkekrimmenh vash)

DROP DATABASE IF EXISTS "mobile-media-share";
-- Xrhsths mobile-media-share
DROP USER "mobile-media-share";
CREATE USER "mobile-media-share" WITH PASSWORD 'medi@sh@re';
-- Bash mobile-media-share
CREATE DATABASE "mobile-media-share" OWNER "mobile-media-share";

-- Ta kanei o diaxeirisths xrhsths postgres (psql san postgres alla afou mpei se sygkekrimmenh vash)
DROP FUNCTION IF EXISTS match(TEXT, TEXT);
CREATE FUNCTION match(string TEXT, term TEXT) RETURNS BOOLEAN IMMUTABLE STRICT LANGUAGE PLPerlU AS $$
	use Unicode::Normalize;
	$text = NFD($_[0]);
	$text =~ s/\p{InCombiningDiacriticalMarks}+//g;
	$term = NFD($_[1]);
	$term =~ s/\p{InCombiningDiacriticalMarks}+//g;
	return ((index(uc($text), uc($term)) != -1)  || (index(lc($text), lc($term)) != -1)) ? 1 : 0;
$$;

-- -------------------------------------------------------------------

-- psql -h localhost -U 'mobile-media-share' -W (ta kanei o xrhsths mobile-media-share)

-- Pinakes
-- Drop ton Media prwta epeidh exei foreign key ston Users
DROP TABLE IF EXISTS Media CASCADE;

-- Pinakas Eggegramenwn xrhstwn
DROP TABLE IF EXISTS Users CASCADE;

CREATE TABLE Users (
	email VARCHAR(128) NOT NULL PRIMARY KEY,
	password CHAR(32) DEFAULT NULL,
	status INT NOT NULL,
	tokenTimestamp TIMESTAMP DEFAULT NULL,
	token CHAR(32) DEFAULT NULL,
	name VARCHAR(64) DEFAULT NULL,
	photo CHAR(128) DEFAULT NULL
);

-- Pinakas Fotografiwn, videos...
CREATE TABLE Media (
	id CHAR(36) NOT NULL PRIMARY KEY,
	type VARCHAR(128) NOT NULL,
	size BIGINT NOT NULL,
	duration INT NOT NULL, -- diarkeia gia video/hxo
	"user" VARCHAR(128) NOT NULL,
	created TIMESTAMP NOT NULL, -- pote dhmiourghthike
	edited TIMESTAMP NOT NULL, -- pote allaxe
	title VARCHAR(128) NOT NULL,
	latitude DECIMAL(9, 6) NOT NULL,
	longitude DECIMAL(9, 6) NOT NULL,
	public BOOLEAN NOT NULL, -- 'h public 'h private
	FOREIGN KEY ("user") REFERENCES Users (email) ON UPDATE CASCADE ON DELETE CASCADE
);

-- Prosthikh periorismou (foreign key me onoma Users_photo_fkey) apo ton Users ston Media (sto telos)
-- H fwtografia tou xrhsth tha prepei na einai fwtografia hdh anevasmenh
ALTER TABLE Users ADD CONSTRAINT Users_photo_fkey FOREIGN KEY (photo) REFERENCES Media (id) ON UPDATE CASCADE ON DELETE CASCADE;

