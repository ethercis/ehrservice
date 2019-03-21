-- this script enhance an existing EtherCIS DB to support the cache summary extension it
-- is called by prepare_db shell script
-- PASS 1: prepare the cache summary configuration tables
-- C.Chevalley May 2017
-- See LICENSE.txt for licensing details
-------------------------------------------------------------------------------------------------
-- originally called: prepare_cache_summary_db_1.sql

CREATE TABLE ehr.heading (
  code VARCHAR(16) PRIMARY KEY ,
  name TEXT,
  description TEXT
);

CREATE TABLE ehr.template (
  uid UUID PRIMARY KEY,
  template_id TEXT UNIQUE,
  concept TEXT
);

CREATE TABLE ehr.template_heading_xref (
  heading_code VARCHAR(16) REFERENCES ehr.heading(code),
  template_id UUID REFERENCES ehr.template(uid)
);
-- fills in the headings
INSERT INTO ehr.heading
VALUES
  ('ORDERS', 'Orders', 'Orders'),
  ('RESULTS', 'Results', 'Results'),
  ('VITALS', 'Vitals', 'Vitals'),  ('DIAGNOSES', 'Diagnoses', 'Diagnoses');
