-- this script enhance an existing EtherCIS DB to support FEEDER_AUDIT
-- see https://specifications.openehr.org/UML/#Architecture___18_1_83e026d_1433773264057_196291_6114
-- C.Chevalley Ded 2018
-- See LICENSE.txt for licensing details
-------------------------------------------------------------------------------------------------
-- this table is referred to by
-- originating_system_item_ids [0..n]
-- feeder_system_item_ids [0..n]
-- originating_system_audit [0..n]


CREATE TABLE ehr.feeder_audit_identifier (
  id UUID PRIMARY KEY DEFAULT ext.uuid_generate_v4() ,
  id_value TEXT, -- identifier value
  issuer TEXT, -- authority responsible for the identification (ex. France ASIP, LDAP server etc.)
  assigner TEXT, -- assigner of the identifier
  type_name TEXT, -- coding origin f.ex. INS-C, INS-A, NHS etc.
  feeder_audit_id UUID not null -- entity identified with this identifier
);

CREATE TABLE ehr.originating_system_item_ids (LIKE ehr.feeder_audit_identifier);
CREATE TABLE ehr.feeder_system_item_ids (LIKE ehr.feeder_audit_identifier);
CREATE TABLE ehr.originating_system_audit (LIKE ehr.feeder_audit_identifier);

CREATE TABLE ehr.feeder_audit (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4() ,
  composition_id UUID REFERENCES ehr.composition(id),
  originating_system_audit UUID REFERENCES ehr.feeder_audit_identifier(id),
  original_content BYTEA,
  feeder_system_audit UUID REFERENCES ehr.feeder_audit_identifier(id)
);

ALTER TABLE ehr.feeder_audit_identifier
ADD CONSTRAINT feeder_audit_fk
FOREIGN KEY (feeder_audit_id)
REFERENCES ehr.feeder_audit(id)
ON DELETE CASCADE;
