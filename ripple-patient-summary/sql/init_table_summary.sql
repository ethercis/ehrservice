-- Tables used for patient summary (HEADING relationship)
-- C.Chevalley May 2017
-- See LICENSE.txt for licensing details
-------------------------------------------------------------------------------------------------
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