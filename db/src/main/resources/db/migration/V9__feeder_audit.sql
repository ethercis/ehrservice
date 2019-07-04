
-- To use for CR152
-- required for v1.3.0

CREATE TABLE ehr.originating_system_item_ids (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  id_value TEXT, -- identifier value
  issuer TEXT, -- authority responsible for the identification (ex. France ASIP, LDAP server etc.)
  assigner TEXT, -- assigner of the identifier
  type_name TEXT, -- coding origin f.ex. INS-C, INS-A, NHS etc.
  feeder_audit_id UUID not null -- entity identified with this identifier
);

CREATE TABLE ehr.feeder_system_item_ids (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  id_value TEXT, -- identifier value
  issuer TEXT, -- authority responsible for the identification (ex. France ASIP, LDAP server etc.)
  assigner TEXT, -- assigner of the identifier
  type_name TEXT, -- coding origin f.ex. INS-C, INS-A, NHS etc.
  feeder_audit_id UUID not null -- entity identified with this identifier
);

CREATE TABLE ehr.originating_system_audit (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  system_id TEXT,
  location UUID,
  subject UUID,
  provider UUID,
  time TIMESTAMP,
  time_tz TEXT,
  version_id TEXT
);

CREATE TABLE ehr.feeder_system_audit (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  system_id TEXT,
  location UUID,
  subject UUID,
  provider UUID,
  time TIMESTAMP,
  time_tz TEXT,
  version_id TEXT
);

CREATE TABLE ehr.feeder_audit (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4() ,
  composition_id UUID REFERENCES ehr.composition(id),
  originating_system_audit UUID REFERENCES ehr.originating_system_audit(id) ON DELETE CASCADE,
  original_content BYTEA,
  feeder_system_audit UUID REFERENCES ehr.feeder_system_audit(id)  ON DELETE CASCADE
);

ALTER TABLE ehr.originating_system_item_ids
ADD CONSTRAINT feeder_audit_fk
FOREIGN KEY (feeder_audit_id)
REFERENCES ehr.feeder_audit(id)
ON DELETE CASCADE;

ALTER TABLE ehr.feeder_system_item_ids
ADD CONSTRAINT feeder_audit_fk
FOREIGN KEY (feeder_audit_id)
REFERENCES ehr.feeder_audit(id)
ON DELETE CASCADE;