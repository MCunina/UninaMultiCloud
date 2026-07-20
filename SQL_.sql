DROP TABLE IF EXISTS accessoPlaylist;
DROP TABLE IF EXISTS composizionePlaylist;
DROP TABLE IF EXISTS ascolti;
DROP TABLE IF EXISTS playlist;
DROP TABLE IF EXISTS elementoMultimediale;
DROP TABLE IF EXISTS corsoLaurea;
DROP TABLE IF EXISTS utente;

CREATE TABLE utente (
    matricola           VARCHAR(20)   NOT NULL,
    nome                VARCHAR(50)   NOT NULL,
    cognome             VARCHAR(50)   NOT NULL,
    email               VARCHAR(100)  NOT NULL,
    password            VARCHAR(255)  NOT NULL, 
    dataRegistrazione   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pkUtente PRIMARY KEY (matricola),
    CONSTRAINT uqUtenteEmail UNIQUE (email),
    CONSTRAINT chkUtenteEmailDominio CHECK (
        email LIKE '%@unina.it' OR email LIKE '%@studenti.unina.it'
    ),
    CONSTRAINT chkUtentePasswordLunghezza CHECK (CHAR_LENGTH(password) >= 8)
);

CREATE TABLE corsoLaurea (
    matricola   VARCHAR(20)   NOT NULL,
    corsoLaurea VARCHAR(100)  NOT NULL,
    CONSTRAINT pkCorsoLaurea PRIMARY KEY (matricola, corsoLaurea),
    CONSTRAINT fkCorsoLaureaUtente FOREIGN KEY (matricola)
        REFERENCES utente (matricola)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE elementoMultimediale (
    idElementoMultimediale SERIAL       NOT NULL,
    titolo                 VARCHAR(150) NOT NULL,
    descrizione            VARCHAR(500) NULL,
    formato                VARCHAR(10)  NOT NULL,
    durata                 INT          NULL, 
    dataCreazione          DATE         NOT NULL,
    urlFile                VARCHAR(255) NOT NULL, 
    urlCopertina           VARCHAR(255) NULL,     
    matricolaAutore        VARCHAR(20)  NOT NULL,
    dataCaricamento        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pkElementoMultimediale PRIMARY KEY (idElementoMultimediale),
    CONSTRAINT fkElementoMultimedialeAutore FOREIGN KEY (matricolaAutore)
        REFERENCES utente (matricola)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chkElementoFormato CHECK (formato IN ('Audio', 'Video')),
    CONSTRAINT chkElementoDurataPositiva CHECK (durata IS NULL OR durata > 0),
    CONSTRAINT chkDataCreazioneValida CHECK (dataCreazione <= CURRENT_DATE) 
);

CREATE INDEX idxElementoTitolo   ON elementoMultimediale (titolo);
CREATE INDEX idxElementoFormato  ON elementoMultimediale (formato);
CREATE INDEX idxElementoAutore   ON elementoMultimediale (matricolaAutore);

CREATE TABLE playlist (
    idPlaylist            SERIAL       NOT NULL,
    titolo                VARCHAR(150) NOT NULL,
    visibilita            VARCHAR(20)  NOT NULL DEFAULT 'PRIVATA',
    dataCreazione         DATE         NOT NULL DEFAULT CURRENT_DATE,
    numeroElementi        INT          NOT NULL DEFAULT 0,
    matricolaProprietario VARCHAR(20)  NOT NULL,
    CONSTRAINT pkPlaylist PRIMARY KEY (idPlaylist),
    CONSTRAINT fkPlaylistProprietario FOREIGN KEY (matricolaProprietario)
        REFERENCES utente (matricola)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chkPlaylistVisibilita CHECK (visibilita IN ('PUBBLICA', 'PRIVATA', 'CONDIVISA'))
);

CREATE INDEX idxPlaylistVisibilita  ON playlist (visibilita);
CREATE INDEX idxPlaylistProprietario ON playlist (matricolaProprietario);

CREATE TABLE ascolti (
    matricola               VARCHAR(20)  NOT NULL,
    idElementoMultimediale  INT          NOT NULL,
    numeroRiproduzioni      INT          NOT NULL DEFAULT 1,
    dataUltimoAscolto       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pkAscolti PRIMARY KEY (matricola, idElementoMultimediale),
    CONSTRAINT fkAscoltiUtente FOREIGN KEY (matricola)
        REFERENCES utente (matricola)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fkAscoltiElemento FOREIGN KEY (idElementoMultimediale)
        REFERENCES elementoMultimediale (idElementoMultimediale)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chkAscoltiNumeroPositivo CHECK (numeroRiproduzioni > 0)
);

CREATE TABLE composizionePlaylist (
    idPlaylist              INT       NOT NULL,
    idElementoMultimediale  INT       NOT NULL,
    posizione               INT       NOT NULL, 
    dataAggiunta            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pkComposizionePlaylist PRIMARY KEY (idPlaylist, idElementoMultimediale),
    CONSTRAINT fkComposizionePlaylistPlaylist FOREIGN KEY (idPlaylist)
        REFERENCES playlist (idPlaylist)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fkComposizionePlaylistElemento FOREIGN KEY (idElementoMultimediale)
        REFERENCES elementoMultimediale (idElementoMultimediale)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE accessoPlaylist (
    idPlaylist       INT       NOT NULL,
    matricola        VARCHAR(20) NOT NULL,
    dataConcessione  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pkAccessoPlaylist PRIMARY KEY (idPlaylist, matricola),
    CONSTRAINT fkAccessoPlaylistPlaylist FOREIGN KEY (idPlaylist)
        REFERENCES playlist (idPlaylist)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fkAccessoPlaylistUtente FOREIGN KEY (matricola)
        REFERENCES utente (matricola)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- TRIGGER

-- Trigger per inserire o aggiornare un elemento negli ascolti

-- Vista necessaria per il trigger INSTEAD OF sottostante
-- (mancante nello script originale)
CREATE OR REPLACE VIEW Ascolti_v AS
SELECT matricola, idElementoMultimediale, numeroRiproduzioni, dataUltimoAscolto
FROM ascolti;

CREATE OR REPLACE FUNCTION fnControllaAscolti()
RETURNS TRIGGER AS $$
DECLARE
    v_conta INTEGER;
BEGIN
    SELECT COUNT(*)
    INTO v_conta
    FROM Ascolti
    WHERE matricola = NEW.matricola
      AND idElementoMultimediale = NEW.idElementoMultimediale;

    IF v_conta > 0 THEN
        UPDATE Ascolti
        SET numeroRiproduzioni = numeroRiproduzioni + 1,
            dataUltimoAscolto = CURRENT_TIMESTAMP
        WHERE matricola = NEW.matricola
          AND idElementoMultimediale = NEW.idElementoMultimediale;
    ELSE
        INSERT INTO Ascolti (matricola, idElementoMultimediale, numeroRiproduzioni, dataUltimoAscolto)
        VALUES (NEW.matricola, NEW.idElementoMultimediale, 1, CURRENT_TIMESTAMP);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER ControllaAscolti
INSTEAD OF INSERT ON Ascolti_v
FOR EACH ROW
EXECUTE FUNCTION fnControllaAscolti();

--Trigger per modificare l'accesso alla playlist

CREATE OR REPLACE FUNCTION fnPlaylistAfterUpdateVisibilita()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.visibilita = 'CONDIVISA' AND NEW.visibilita <> 'CONDIVISA' THEN
        DELETE FROM accessoPlaylist WHERE idPlaylist = NEW.idPlaylist;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trgPlaylistAfterUpdateVisibilita
AFTER UPDATE ON playlist
FOR EACH ROW
EXECUTE FUNCTION fnPlaylistAfterUpdateVisibilita();

--Trigger per la composizione di una playlist, aggiorna il numero di elementi in essa(+)
CREATE OR REPLACE FUNCTION fnComposizionePlaylistAfterInsert()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE playlist SET numeroElementi = numeroElementi + 1 WHERE idPlaylist = NEW.idPlaylist;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trgComposizionePlaylistAfterInsert
AFTER INSERT ON composizionePlaylist
FOR EACH ROW
EXECUTE FUNCTION fnComposizionePlaylistAfterInsert();

--Trigger per la composizione di una playlist, aggiorna il numero di elementi in essa(-)
CREATE OR REPLACE FUNCTION fnComposizionePlaylistAfterDelete()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE playlist SET numeroElementi = numeroElementi - 1 WHERE idPlaylist = OLD.idPlaylist;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trgComposizionePlaylistAfterDelete
AFTER DELETE ON composizionePlaylist
FOR EACH ROW
EXECUTE FUNCTION fnComposizionePlaylistAfterDelete();
