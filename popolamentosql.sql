
-- ---------------------------------------------------------
-- UTENTE
-- ---------------------------------------------------------
INSERT INTO utente (matricola, nome, cognome, email, password, dataRegistrazione) VALUES
('STU00001', 'Mario',  'Rossi',    'mario.rossi@studenti.unina.it',   'Passw0rd!23', '2025-09-01 09:00:00'),
('STU00002', 'Giulia', 'Bianchi',  'giulia.bianchi@studenti.unina.it','Passw0rd!23', '2025-09-02 10:15:00'),
('STU00003', 'Luca',   'Verdi',    'luca.verdi@studenti.unina.it',    'Passw0rd!23', '2025-09-03 11:30:00'),
('STU00004', 'Anna',   'Ferrari',  'anna.ferrari@studenti.unina.it',  'Passw0rd!23', '2025-09-05 08:45:00'),
('STU00005', 'Marco',  'Russo',    'marco.russo@studenti.unina.it',   'Passw0rd!23', '2025-10-10 14:20:00'),
('STU00006', 'Sara',   'Colombo',  'sara.colombo@studenti.unina.it',  'Passw0rd!23', '2025-10-12 16:00:00'),
('STU00007', 'Davide', 'Ricci',    'davide.ricci@studenti.unina.it',  'Passw0rd!23', '2025-11-01 09:10:00'),
('STU00008', 'Elena',  'Marino',   'elena.marino@studenti.unina.it',  'Passw0rd!23', '2025-11-20 12:00:00'),
('DOC00001', 'Paolo',  'Greco',    'paolo.greco@unina.it',            'Docente!2025', '2025-08-15 08:00:00'),
('DOC00002', 'Chiara', 'Bruno',    'chiara.bruno@unina.it',           'Docente!2025', '2025-08-16 08:30:00');

-- ---------------------------------------------------------
-- CORSOLAUREA
-- ---------------------------------------------------------
INSERT INTO corsoLaurea (matricola, corsoLaurea) VALUES
('STU00001', 'Informatica'),
('STU00002', 'Ingegneria Informatica'),
('STU00002', 'Informatica Musicale'),
('STU00003', 'Informatica'),
('STU00004', 'Ingegneria Gestionale'),
('STU00005', 'Informatica'),
('STU00006', 'Fisica'),
('STU00007', 'Matematica'),
('STU00008', 'Informatica Musicale');

-- ---------------------------------------------------------
-- ELEMENTOMULTIMEDIALE
-- ---------------------------------------------------------
INSERT INTO elementoMultimediale
    (idElementoMultimediale, titolo, descrizione, formato, durata, dataCreazione, urlFile, urlCopertina, matricolaAutore, dataCaricamento) VALUES
(1,  'Lezione 1 - Analisi Matematica',        'Introduzione ai limiti',                 'Video', 3600, '2025-09-10', '/files/video/analisi1.mp4', '/files/cover/analisi1.jpg', 'DOC00001', '2025-09-11 09:00:00'),
(2,  'Lezione 2 - Analisi Matematica',        'Derivate e applicazioni',                'Video', 4200, '2025-09-17', '/files/video/analisi2.mp4', '/files/cover/analisi2.jpg', 'DOC00001', '2025-09-18 09:00:00'),
(3,  'Podcast - Storia dell''Informatica',    'Le origini del calcolo automatico',      'Audio', 1800, '2025-09-20', '/files/audio/storia_inf.mp3', NULL, 'STU00002', '2025-09-21 15:30:00'),
(4,  'Tutorial Java - Le basi',               'Variabili, cicli e condizioni',          'Video', 2700, '2025-10-01', '/files/video/java_basi.mp4', '/files/cover/java_basi.jpg', 'STU00005', '2025-10-02 10:00:00'),
(5,  'Tutorial Java - Programmazione OOP',    'Classi, oggetti ed ereditarietà',        'Video', 3300, '2025-10-08', '/files/video/java_oop.mp4', '/files/cover/java_oop.jpg', 'STU00005', '2025-10-09 10:00:00'),
(6,  'Musica strumentale per studiare',       'Brano rilassante senza parole',          'Audio', 2400, '2025-10-15', '/files/audio/study_music1.mp3', '/files/cover/study1.jpg', 'STU00003', '2025-10-15 20:00:00'),
(7,  'Musica lofi per concentrazione',        'Beat lofi hip-hop',                      'Audio', 2100, '2025-10-16', '/files/audio/study_music2.mp3', '/files/cover/study2.jpg', 'STU00003', '2025-10-16 20:15:00'),
(8,  'Seminario - Basi di Dati',              'Modello relazionale e normalizzazione',  'Video', 5400, '2025-11-05', '/files/video/basidati.mp4', '/files/cover/basidati.jpg', 'DOC00002', '2025-11-06 08:30:00'),
(9,  'Podcast - Intelligenza Artificiale',    'Panoramica sul machine learning',        'Audio', 2700, '2025-11-10', '/files/audio/ia_podcast.mp3', NULL, 'STU00006', '2025-11-11 18:00:00'),
(10, 'Video progetto - Robotica',             'Presentazione del progetto di robotica', 'Video', 900,  '2025-11-25', '/files/video/robotica.mp4', '/files/cover/robotica.jpg', 'STU00007', '2025-11-26 17:00:00'),
(11, 'Registrazione lezione - Fisica I',      'Meccanica classica',                     'Audio', 3900, '2025-12-01', '/files/audio/fisica1.mp3', NULL, 'STU00006', '2025-12-02 09:00:00'),
(12, 'Tutorial SQL avanzato',                 'Query complesse e ottimizzazione',       'Video', 3000, '2026-01-10', '/files/video/sql_avanzato.mp4', '/files/cover/sql_avanzato.jpg', 'STU00005', '2026-01-11 11:00:00'),
(13, 'Podcast - Vita universitaria',          'Consigli per le matricole',              'Audio', 1500, '2026-02-14', '/files/audio/vita_universitaria.mp3', NULL, 'STU00008', '2026-02-15 13:00:00'),
(14, 'Cortometraggio studenti - Corso Cinema','Progetto finale corso di cinema',        'Video', 720,  '2026-03-05', '/files/video/corto_cinema.mp4', '/files/cover/corto_cinema.jpg', 'STU00004', '2026-03-06 19:00:00'),
(15, 'Musica jazz per relax',                 'Selezione di brani jazz strumentali',    'Audio', 2200, '2026-04-01', '/files/audio/jazz_relax.mp3', '/files/cover/jazz.jpg', 'STU00003', '2026-04-01 21:00:00');

SELECT setval(pg_get_serial_sequence('elementoMultimediale', 'idelementomultimediale'), 15, true);

-- ---------------------------------------------------------
-- PLAYLIST
-- (numeroElementi lasciato al valore di default: viene
--  allineato manualmente in fondo allo script)
-- ---------------------------------------------------------
INSERT INTO playlist (idPlaylist, titolo, visibilita, dataCreazione, matricolaProprietario) VALUES
(1, 'Lezioni di Analisi I',        'PUBBLICA',  '2025-09-12', 'STU00001'),
(2, 'I miei podcast preferiti',    'PRIVATA',   '2025-09-25', 'STU00002'),
(3, 'Musica per studiare',         'CONDIVISA', '2025-10-16', 'STU00003'),
(4, 'Tutorial Java',               'PUBBLICA',  '2025-10-10', 'STU00005'),
(5, 'Playlist personale di Sara',  'PRIVATA',   '2025-11-12', 'STU00006'),
(6, 'Materiale corso Basi di Dati','CONDIVISA', '2025-11-07', 'DOC00002');

SELECT setval(pg_get_serial_sequence('playlist', 'idplaylist'), 6, true);

-- ---------------------------------------------------------
-- COMPOSIZIONEPLAYLIST
-- ---------------------------------------------------------
INSERT INTO composizionePlaylist (idPlaylist, idElementoMultimediale, posizione) VALUES
(1, 1,  1),
(1, 2,  2),
(2, 3,  1),
(2, 9,  2),
(2, 13, 3),
(3, 6,  1),
(3, 7,  2),
(3, 15, 3),
(4, 4,  1),
(4, 5,  2),
(4, 12, 3),
(5, 11, 1),
(6, 8,  1),
(6, 12, 2);

-- ---------------------------------------------------------
-- ASCOLTI
-- ---------------------------------------------------------
INSERT INTO ascolti (matricola, idElementoMultimediale, numeroRiproduzioni, dataUltimoAscolto) VALUES
('STU00001', 1,  5, '2025-09-15 10:00:00'),
('STU00001', 2,  3, '2025-09-20 11:00:00'),
('STU00002', 1,  2, '2025-09-16 09:30:00'),
('STU00002', 3,  8, '2025-09-22 20:00:00'),
('STU00003', 6,  12,'2025-10-20 22:00:00'),
('STU00003', 7,  10,'2025-10-21 21:30:00'),
('STU00004', 8,  1, '2025-11-08 14:00:00'),
('STU00005', 4,  6, '2025-10-05 16:00:00'),
('STU00005', 5,  4, '2025-10-12 17:00:00'),
('STU00006', 9,  3, '2025-11-12 19:00:00'),
('STU00006', 11, 2, '2025-12-03 08:00:00'),
('STU00007', 10, 7, '2025-11-27 18:00:00'),
('STU00008', 13, 5, '2026-02-16 12:30:00'),
('DOC00001', 8,  1, '2025-11-06 09:00:00'),
('STU00002', 13, 4, '2026-02-20 13:00:00'),
('STU00003', 15, 6, '2026-04-02 21:15:00');

-- ---------------------------------------------------------
-- ACCESSOPLAYLIST (solo per playlist CONDIVISA: 3 e 6)
-- ---------------------------------------------------------
INSERT INTO accessoPlaylist (idPlaylist, matricola, dataConcessione) VALUES
(3, 'STU00001', '2025-10-17 08:00:00'),
(3, 'STU00004', '2025-10-18 09:00:00'),
(3, 'STU00008', '2025-10-19 10:00:00'),
(6, 'STU00003', '2025-11-08 08:00:00'),
(6, 'STU00005', '2025-11-09 09:00:00'),
(6, 'DOC00001', '2025-11-10 10:00:00');

-- ---------------------------------------------------------
-- Allineamento di numeroElementi in base al contenuto reale
-- delle playlist (necessario perché i trigger AFTER INSERT/
-- DELETE forniti nello script originale non sono scritti in
-- sintassi PL/pgSQL valida e quindi non si attivano su Postgres)
-- ---------------------------------------------------------
UPDATE playlist p
SET numeroElementi = sub.conteggio
FROM (
    SELECT idPlaylist, COUNT(*) AS conteggio
    FROM composizionePlaylist
    GROUP BY idPlaylist
) sub
WHERE p.idPlaylist = sub.idPlaylist;

COMMIT;