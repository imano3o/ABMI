-- Exactly one hospital. The name is a placeholder.
INSERT INTO hospital (plant, name) VALUES
    ('1710', 'Klinikum Musterstadt');

INSERT INTO station (code, name, cost_center) VALUES
    ('ST01', 'Station 1 – Chirurgie',      '17101101'),
    ('ST02', 'Station 2 – Kardiologie',    '17101201'),
    ('ST03', 'Station 3 – Innere Medizin', '17101301');

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

-- Display names; SAP only knows the material number.
INSERT INTO medication (material, name) VALUES
    ('MED-AMOXI1000', 'Amoxicillin 1000 mg Tabletten'),
    ('MED-ENOX40',    'Enoxaparin 40 mg Fertigspritze'),
    ('MED-FUROS40',   'Furosemid 40 mg Tabletten'),
    ('MED-HEP5000',   'Heparin 5.000 I.E. Ampullen'),
    ('MED-IBU400',    'Ibuprofen 400 mg Tabletten'),
    ('MED-INSACT',    'Insulin Actrapid 10 ml Durchstechflasche'),
    ('MED-METAM500',  'Metamizol 500 mg Tabletten'),
    ('MED-MORPH10',   'Morphin 10 mg Ampullen'),
    ('MED-NACL500',   'NaCl 0,9 % 500 ml Infusion'),
    ('MED-ONDAN4',    'Ondansetron 4 mg Ampullen'),
    ('MED-PANTO40',   'Pantoprazol 40 mg Tabletten'),
    ('MED-PCM500',    'Paracetamol 500 mg Tabletten');
