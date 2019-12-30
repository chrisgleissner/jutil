CREATE TABLE IF NOT EXISTS person (
    id int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nachname varchar(100),
    geburtstag date
);

INSERT INTO person VALUES (1, 'Miller', parsedatetime('2000-01-01', 'yyyy-MM-ss'));
INSERT INTO person VALUES (2, 'Farmer', parsedatetime('2000-01-02', 'yyyy-MM-ss'));
