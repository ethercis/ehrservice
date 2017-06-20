-- the ethercis database schema

-- differences:
--   extensions live in the ext schema to make flyways work easier

CREATE SCHEMA IF NOT EXISTS ehr;

-- identification of a system
CREATE TABLE ehr.system (
  id           UUID PRIMARY KEY,             -- the ID
	description  TEXT NOT NULL,
  settings     TEXT NOT NULL UNIQUE          -- unique external ID (as set in the config)
);

--
-- TERMINOLOGY
--

-- ISO 3166-1 countries
CREATE TABLE ehr.territory (
	code         INT PRIMARY KEY, -- numeric code
	twoLetter    CHAR(2),
	threeLetter  CHAR(3),
	text         TEXT NOT NULL
);

-- ISO 639-1 languages
CREATE TABLE ehr.language (
	code          VARCHAR(5) PRIMARY KEY,
	description   TEXT NOT NULL
);

-- terminology provider
CREATE TABLE ehr.terminology_provider (
	code       VARCHAR(20) PRIMARY KEY,
	source     TEXT NOT NULL,
	authority  TEXT
);

-- concepts
CREATE TABLE ehr.concept (
  id          UUID PRIMARY KEY DEFAULT ext.uuid_generate_v4(),
	conceptID   INT,
	language    VARCHAR(5) REFERENCES ehr.language(code),
	description TEXT
);


--
-- RM
--

--
-- PARTY_IDENTIFIED
--
-- http://www.openehr.org/releases/RM/latest/docs/common/common.html#_party_identified_class
CREATE TABLE ehr.party_identified (
	id                  UUID PRIMARY KEY,   -- ?? (can be refactored into a bigserial?)
  name                TEXT,               -- PARTY_IDENTIFIED.name

  party_ref_value     TEXT,               -- PARTY_PROXY.external_ref.id.value  (OBJECT_ID)
  party_ref_scheme    TEXT,               -- PARTY_PROXY.external_ref.id.scheme (GENERIC_ID)
  party_ref_namespace TEXT,               -- PARTY_PROXY.external_ref.namespace (OBJECT_REF)
  party_ref_type      TEXT                -- PARTY_PROXY.external_ref.type      (OBJECT_REF)
);

-- PARTY_IDENTIFIED.identifiers
CREATE TABLE ehr.identifier (
	id_value  TEXT, -- DV_IDENTIFIER.id
	issuer    TEXT, -- DV_IDENTIFIER.issuer
  assigner  TEXT, -- DV_IDENTIFIER.assigner
	type_name TEXT, -- DV_IDENTIFIER.type
	party     UUID NOT NULL REFERENCES ehr.party_identified(id)
);


--
-- EHR_ACCESS
--
CREATE TABLE ehr.access (
	id       UUID PRIMARY KEY DEFAULT ext.uuid_generate_v4(),
	settings TEXT,
	scheme   TEXT
);

--
-- EHR
-- http://www.openehr.org/releases/RM/latest/docs/ehr/ehr.html#_ehr_class
--
CREATE TABLE ehr.ehr (
    id                UUID PRIMARY KEY,               -- EHR.ehr_id
    date_created      TIMESTAMP,                      -- EHR.time_created
    date_created_tzid TEXT,                           --   timezone id
    access            UUID REFERENCES ehr.access(id), -- EHR.ehr_access
    system_id         UUID REFERENCES ehr.system(id), -- EHR.system_id
    directory         UUID                            -- EHR.directory
);

--
-- EHR_STATUS
-- http://www.openehr.org/releases/RM/latest/docs/ehr/ehr.html#_ehr_status_class
--
CREATE TABLE ehr.status (
  id              UUID PRIMARY KEY,     -- EHR_STATUS.uid
  is_queryable    BOOLEAN,              -- EHR_STATUS.is_queryable
  is_modifiable   BOOLEAN,              -- EHR_STATUS.is_modifiable
  party           UUID NOT NULL REFERENCES ehr.party_identified(id),  -- EHR.subject
  other_details   JSONB,                -- EHR_STATUS.other_details

  ehr_id          UUID REFERENCES ehr.ehr(id) ON DELETE CASCADE,
  sys_transaction TIMESTAMP NOT NULL,
  sys_period      TSTZRANGE NOT NULL
);

-- EHR_STATUS change history table
CREATE TABLE ehr.status_history (LIKE ehr.status);
CREATE INDEX ehr_status_history ON ehr.status_history USING BTREE (id);
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON ehr.status
FOR EACH ROW EXECUTE PROCEDURE ext.versioning('sys_period', 'ehr.status_history', true);


create type ehr.contribution_data_type as enum('composition', 'folder', 'ehr', 'system', 'other');
create type ehr.contribution_state as enum('complete', 'incomplete', 'deleted');

-- openehr terminology group 'audit change type'
CREATE TYPE ehr.contribution_change_type AS ENUM (
  'creation',      -- 249
  'amendment',     -- 250
  'modification',  -- 251
  'synthesis',     -- 252
  'deleted',       -- 523
  -- 'attestation',   -- 666  ???
  'unknown'        -- 253
);


CREATE TABLE ehr.contribution (
	id                  UUID PRIMARY KEY,                           -- CONTRIBUTION.uid
	system_id           UUID REFERENCES ehr.system(id),             -- AUDIT_DETAILS.system_id
	committer           UUID REFERENCES ehr.party_identified(id),   -- AUDIT_DETAILS.committer
	time_committed      TIMESTAMP,                                  -- AUDIT_DETAILS.time_committed
  time_committed_tzid TEXT,                                       --   timezone id
	change_type         ehr.contribution_change_type,               -- AUDIT_DETAILS.change_type
	description         TEXT,                                       -- AUDIT_DETAILS.description

  ehr_id              UUID REFERENCES ehr.ehr(id) ON DELETE CASCADE,
  sys_transaction     TIMESTAMP NOT NULL,
  sys_period          TSTZRANGE NOT NULL,

  -- RVE: unused?
  contribution_type ehr.contribution_data_type, -- specifies the type of data it contains
  state ehr.contribution_state, -- current state in lifeCycleState
  signature TEXT

);

-- change history table
CREATE TABLE ehr.contribution_history (LIKE ehr.contribution);
CREATE INDEX ehr_contribution_history ON ehr.contribution_history USING BTREE (id);
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON ehr.contribution
FOR EACH ROW EXECUTE PROCEDURE ext.versioning('sys_period', 'ehr.contribution_history', true);




create table ehr.attestation (
  id UUID PRIMARY KEY DEFAULT ext.uuid_generate_v4(),
  contribution_id UUID REFERENCES ehr.contribution(id) ON DELETE CASCADE ,
  proof TEXT,
  reason TEXT,
  is_pending BOOLEAN
);

CREATE TABLE ehr.attested_view (
  id UUID PRIMARY KEY DEFAULT ext.uuid_generate_v4(),
  attestation_id UUID REFERENCES ehr.attestation(id) ON DELETE CASCADE,
  --  DvMultimedia
  alternate_text TEXT,
  compression_algorithm TEXT,
  media_type TEXT,
  data BYTEA,
  integrity_check BYTEA,
  integrity_check_algorithm TEXT,
  thumbnail UUID, -- another multimedia holding the thumbnail
  uri TEXT
);


create table ehr.composition (
    id UUID PRIMARY KEY DEFAULT ext.uuid_generate_v4(),
    ehr_id UUID references ehr.ehr(id) ON DELETE CASCADE,
--    version UUID references ehr.versioned(id),
    in_contribution UUID references ehr.contribution(id) ON DELETE CASCADE , -- in contribution version
    active boolean default true, -- true if this composition is still valid (e.g. not replaced yet)
    is_persistent boolean default true,
    language varchar(5) references ehr.language(code), -- pointer to the language codeset. Indicates what broad category this Composition is belogs to, e.g. �persistent� - of longitudinal validity, �event�, �process� etc.
    territory int references ehr.territory(code), -- Name of territory in which this Composition was written. Coded fromBinder openEHR �countries� code set, which is an expression of the ISO 3166 standard.
    composer UUID not null references ehr.party_identified(id), -- points to the PARTY_PROXY who has created the composition
    sys_transaction TIMESTAMP NOT NULL,
    sys_period tstzrange NOT NULL -- temporal table
    -- item UUID not null, -- point to the first section in composition
);

-- change history table
CREATE TABLE ehr.composition_history (like ehr.composition);
CREATE INDEX ehr_composition_history ON ehr.composition_history USING BTREE (id);

COMMENT ON TABLE ehr.composition IS 'Composition table';

CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON ehr.composition
FOR EACH ROW EXECUTE PROCEDURE ext.versioning('sys_period', 'ehr.composition_history', true);

create table ehr.event_context (
  id UUID primary key DEFAULT ext.uuid_generate_v4(),
  composition_id UUID references ehr.composition(id) ON DELETE CASCADE , -- belong to composition
  start_time TIMESTAMP not null,
  start_time_tzid TEXT, -- time zone id: format GMT +/- hh:mm
  end_time TIMESTAMP null,
  end_time_tzid TEXT, -- time zone id: format GMT +/- hh:mm
  facility UUID references ehr.party_identified(id), -- points to a party identified
  location TEXT,
  other_context JSONB, -- supports a cluster for other context definition
  setting UUID references ehr.concept(id), -- codeset setting, see ehr_im section 5
--	program UUID references ehr.program(id), -- the program defined for this context (only in full ddl version)
  sys_transaction TIMESTAMP NOT NULL,
  sys_period tstzrange NOT NULL -- temporal table
);

-- change history table
create table ehr.event_context_history (like ehr.event_context);
CREATE INDEX ehr_event_context_history ON ehr.event_context_history USING BTREE (id);

CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON ehr.event_context
FOR EACH ROW EXECUTE PROCEDURE ext.versioning('sys_period', 'ehr.event_context_history', true);

COMMENT ON TABLE ehr.event_context IS 'defines the context of an event (time, who, where... see openEHR IM 5.2';

create table ehr.participation (
  id UUID primary key DEFAULT ext.uuid_generate_v4(),
  event_context UUID NOT NULL REFERENCES ehr.event_context(id) ON DELETE CASCADE,
  performer UUID references ehr.party_identified(id),
  function TEXT,
  mode TEXT,
  start_time timestamp,
  start_time_tzid TEXT, -- timezone id
  sys_transaction TIMESTAMP NOT NULL,
  sys_period tstzrange NOT NULL -- temporal table
);

-- change history table
create table ehr.participation_history (like ehr.participation);
CREATE INDEX ehr_participation_history ON ehr.participation_history USING BTREE (id);

CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON ehr.participation
FOR EACH ROW EXECUTE PROCEDURE ext.versioning('sys_period', 'ehr.participation_history', true);

COMMENT ON TABLE ehr.participation IS 'define a participating party for an event f.ex.';

create type ehr.entry_type as enum('section','care_entry', 'admin', 'proxy');

create table ehr.entry (
	id UUID primary key DEFAULT ext.uuid_generate_v4(),
	composition_id UUID references ehr.composition(id) ON DELETE CASCADE , -- belong to composition
	sequence int, -- ordering sequence number
	item_type ehr.entry_type,
  template_id TEXT, -- operational template to rebuild the structure entry
  template_uuid UUID, -- optional, used with operational template for consistency
  archetype_id TEXT, -- ROOT archetype id (not sure still in use...)
  category UUID null references ehr.concept(id), -- used to specify the type of content: Evaluation, Instruction, Observation, Action with different languages
  entry JSONB,            -- actual content version dependent (9.3: json, 9.4: jsonb). entry is either CARE_ENTRY or ADMIN_ENTRY
  sys_transaction TIMESTAMP NOT NULL,
  sys_period tstzrange NOT NULL -- temporal table
);

-- change history table
CREATE TABLE ehr.entry_history (like ehr.entry);
CREATE INDEX ehr_entry_history ON ehr.entry_history USING BTREE (id);

COMMENT ON TABLE ehr.entry IS 'this table hold the actual archetyped data values (fromBinder a template)';

CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON ehr.entry
FOR EACH ROW EXECUTE PROCEDURE ext.versioning('sys_period', 'ehr.entry_history', true);

-- CONTAINMENT "pseudo" index for CONTAINS clause resolution
create TABLE ehr.containment (
  comp_id UUID,
  label ltree,
  path text
);

-- meta data
CREATE TABLE ehr.template_meta (
  template_id TEXT,
  array_path TEXT[] -- list of paths containing an item list with list size > 1
);

CREATE INDEX template_meta_idx ON ehr.template_meta(template_id);

-- simple cross reference table to link INSTRUCTIONS with ACTIONS or other COMPOSITION
CREATE TABLE ehr.compo_xref (
  master_uuid UUID REFERENCES ehr.composition(id),
  child_uuid UUID REFERENCES ehr.composition(id),
  sys_transaction TIMESTAMP NOT NULL
);
CREATE INDEX ehr_compo_xref ON ehr.compo_xref USING BTREE (master_uuid);

-- log user sessions with logon id, session id and other parameters
CREATE TABLE ehr.session_log (
  id UUID primary key DEFAULT uuid_generate_v4(),
  subject_id TEXT NOT NULL,
  node_id TEXT,
  session_id TEXT,
  session_name TEXT,
  session_time TIMESTAMP,
  ip_address TEXT
);

-- views to abstract querying
-- EHR STATUS
CREATE VIEW ehr.ehr_status AS
  SELECT ehr.id, party.name AS name,
                 party.party_ref_value AS ref,
                 party.party_ref_scheme AS scheme,
                 party.party_ref_namespace AS namespace,
                 party.party_ref_type AS type,
                 identifier.*
      FROM ehr.ehr ehr
        INNER JOIN ehr.status status ON status.ehr_id = ehr.id
        INNER JOIN ehr.party_identified party ON status.party = party.id
        LEFT JOIN ehr.identifier identifier ON identifier.party = party.id;

-- Composition expanded view (include context and other meta_data
CREATE OR REPLACE VIEW ehr.comp_expand AS
  SELECT
    ehr.id                            AS ehr_id,
    party.party_ref_value             AS subject_externalref_id_value,
    party.party_ref_namespace         AS subject_externalref_id_namespace,
    entry.composition_id,
    entry.template_id,
    entry.archetype_id,
    entry.entry,
    trim(LEADING '''' FROM (trim(TRAILING ''']' FROM
                                 (regexp_split_to_array(json_object_keys(entry.entry :: JSON), 'and name/value=')) [2
                                 ]))) AS composition_name,
    compo.language,
    compo.territory,
    ctx.start_time,
    ctx.start_time_tzid,
    ctx.end_time,
    ctx.end_time_tzid,
    ctx.other_context,
    ctx.location                      AS ctx_location,
    fclty.name                        AS facility_name,
    fclty.party_ref_value             AS facility_ref,
    fclty.party_ref_scheme            AS facility_scheme,
    fclty.party_ref_namespace         AS facility_namespace,
    fclty.party_ref_type              AS facility_type,
    compr.name                        AS composer_name,
    compr.party_ref_value             AS composer_ref,
    compr.party_ref_scheme            AS composer_scheme,
    compr.party_ref_namespace         AS composer_namespace,
    compr.party_ref_type              AS composer_type
  FROM ehr.entry
    INNER JOIN ehr.composition compo ON compo.id = ehr.entry.composition_id
    INNER JOIN ehr.event_context ctx ON ctx.composition_id = ehr.entry.composition_id
    INNER JOIN ehr.party_identified compr ON compo.composer = compr.id
    INNER JOIN ehr.ehr ehr ON ehr.id = compo.ehr_id
    INNER JOIN ehr.status status ON status.ehr_id = ehr.id
    LEFT JOIN ehr.party_identified party ON status.party = party.id
    -- LEFT JOIN ehr.system sys ON ctx.setting = sys.id
    LEFT JOIN ehr.party_identified fclty ON ctx.facility = fclty.id;

--- CREATED INDEX
CREATE INDEX label_idx ON ehr.containment USING GIST (label);
CREATE INDEX comp_id_idx ON ehr.containment USING BTREE(comp_id);
CREATE INDEX gin_entry_path_idx ON ehr.entry USING gin(entry jsonb_path_ops);
CREATE INDEX template_entry_idx ON ehr.entry (template_id);

-- to optimize comp_expand, index FK's
CREATE INDEX entry_composition_id_idx ON ehr.entry (composition_id);
CREATE INDEX composition_composer_idx ON ehr.composition (composer);
CREATE INDEX composition_ehr_idx ON ehr.composition (ehr_id);
CREATE INDEX status_ehr_idx ON ehr.status (ehr_id);
CREATE INDEX status_party_idx ON ehr.status (party);
CREATE INDEX context_facility_idx ON ehr.event_context (facility);
CREATE INDEX context_composition_id_idx ON ehr.event_context (composition_id);
CREATE INDEX context_setting_idx ON ehr.event_context (setting);


-- AUDIT TRAIL has been replaced by CONTRIBUTION
-- create table ehr.audit_trail (
--     id UUID PRIMARY KEY DEFAULT ext.uuid_generate_v4(),
--     composition_id UUID references ehr.composition(id),
--     committer UUID not null references ehr.party_identified(id), -- contributor
--     date_created TIMESTAMP,
--     date_created_tzid TEXT, -- timezone id
--     party UUID not null references ehr.party_identified(id), -- patient
--     serial_version VARCHAR(50),
--     system_id UUID references ehr.system(id)
-- );

