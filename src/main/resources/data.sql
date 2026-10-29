-- Krankenhaus: plant = SAP-Werk (genau ein Eintrag)
-- Name ist ein Platzhalter, bitte durch den echten Namen ersetzen
INSERT INTO hospital (plant, name) VALUES
    ('1710', 'Klinikum Musterstadt');

-- Stationen: code = SAP-Lagerort (StorageLocation), cost_center = SAP-Kostenstelle
INSERT INTO station (code, name, cost_center) VALUES
    ('ST01', 'Station 1 – Chirurgie',      '17101101'),
    ('ST02', 'Station 2 – Kardiologie',    '17101201'),
    ('ST03', 'Station 3 – Innere Medizin', '17101301');

-- Mindestbestände pro Station
INSERT INTO minimum_stock (station, material, minimum_quantity) VALUES
    ('ST01', 'MED-PCM500', 100),
    ('ST01', 'MED-MORPH10', 20),
    ('ST01', 'MED-AMOXI1000', 40),
    ('ST01', 'MED-NACL500', 30),
    ('ST02', 'MED-HEP5000', 40),
    ('ST02', 'MED-FUROS40', 50),
    ('ST02', 'MED-ENOX40', 15),
    ('ST03', 'MED-ENOX40', 10),
    ('ST03', 'MED-ONDAN4', 10),
    ('ST03', 'MED-METAM500', 30),
    ('ST03', 'MED-NACL500', 20);
