-- Generate EtherCIS tables for PostgreSQL 9.3
-- Author: Christian Chevalley
-- This script is to generate the complete set of tables including programs and organization
-- Not to be used for the OS version
--
--    alter table com.ethercis.ehr.consult_req_attachement
--        drop constraint FKC199A3AAB95913AB;
--
--    alter table com.ethercis.ehr.consult_req_attachement
--        drop constraint FKC199A3AA4204581F;
--
drop schema if exists ehr cascade;
-- drop schema if exists common cascade;

create schema ehr;

-- required to be able to auto generate uuids
CREATE EXTENSION "uuid-ossp" WITH SCHEMA ehr;

-- storeComposition schema common;


-- storeComposition common_im entities
-- CREATE TABLE "system" ---------------------------------------
CREATE TABLE ehr.system ( 
	id UUid PRIMARY KEY DEFAULT ehr.uuid_generate_v4(), 
	description Character Varying( 100 ) NOT NULL, 
	settings Character Varying( 250 ) NOT NULL
 );
 
COMMENT ON TABLE  ehr.system IS 'system table for reference';

CREATE TABLE ehr.territory ( 
	code int unique primary key, -- numeric code
	twoLetter char(2),
	threeLetter char(3),
	text Character Varying( 100 ) NOT NULL
 );

COMMENT ON TABLE  ehr.territory IS 'ISO 3166-1 countries codeset';

CREATE TABLE ehr.language ( 
	code varchar(5) unique primary key, 
	description Character Varying( 100 ) NOT NULL
 );

COMMENT ON TABLE  ehr.language IS 'ISO 639-1 language codeset';
 
CREATE TABLE ehr.terminology_provider ( 
	code varchar(20) unique primary key, 
	source Character Varying( 100 ) NOT NULL,
	authority varchar(50)
 );

COMMENT ON TABLE  ehr.terminology_provider IS 'openEHR identified terminology provider';

CREATE TABLE ehr.concept (
    id UUID unique primary key DEFAULT ehr.uuid_generate_v4(),
	conceptID int, 
	language varchar(5) references ehr.language(code),
	description varchar(250)
 );

COMMENT ON TABLE  ehr.concept IS 'openEHR common concepts (e.g. terminology) used in the system';

create table ehr.party_identified (
	id UUID primary key DEFAULT ehr.uuid_generate_v4(),
	name varchar(50),
  -- optional party ref attributes
  party_ref_value VARCHAR(50),
  party_ref_scheme VARCHAR(100),
  party_ref_namespace VARCHAR(50),
  party_ref_type VARCHAR(50)
);

-- list of identifiers for a party identified
create table ehr.identifier (
	id_value VARCHAR(50), -- identifier value
	issuer VARCHAR(50), -- authority responsible for the identification (ex. France ASIP, LDAP server etc.)
  assigner VARCHAR(50), -- assigner of the identifier
	type_name VARCHAR(50), -- coding origin f.ex. INS-C, INS-A, NHS etc.
	party UUID not null references ehr.party_identified(id) -- entity identified with this identifier (normally a person, patient etc.)
);

COMMENT ON TABLE ehr.identifier IS 'specifies an identifier for a party identified, more than one identifier is possible';

-- defines the modality for accessing an com.ethercisrcis.ehr
create table ehr.access (
	id UUID PRIMARY KEY DEFAULT ehr.uuid_generate_v4(),
	settings varchar(250),
	scheme char(50) -- name of access control scheme
 );
 
COMMENT ON TABLE ehr.access IS 'defines the modality for accessing an com.ethercis.ehr (security strategy implementation)';
-- 
create table ehr.status (
	id UUID NOT NULL PRIMARY KEY DEFAULT ehr.uuid_generate_v4(),
	is_queryable boolean default true,
	is_modifiable boolean default true,
	party UUID not null references ehr.party_identified(id),  -- subject (e.g. patient)
  sys_period tstzrange NOT NULL -- temporal table
 );

-- change history table
create table ehr.status_history (like ehr.status);

CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON ehr.status
      FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'ehr.status_history', true);

COMMENT ON TABLE ehr.status IS 'specifies an ehr modality and ownership (patient)';

-- storeComposition ehr_im entities
-- EHR Class emr_im 4.7.1
create table ehr.ehr (
    id UUID NOT NULL PRIMARY KEY DEFAULT ehr.uuid_generate_v4(),
    date_created timestamp default CURRENT_DATE,
    access UUID references ehr.access(id), -- access decision support (f.e. consent)
    status UUID references ehr.status(id),
    system_id UUID references ehr.system(id),
    directory UUID null
);
COMMENT ON TABLE ehr.ehr IS 'EHR itself';

create TABLE ehr.organization (
  id UUID PRIMARY KEY DEFAULT ehr.uuid_generate_v4(),
  is_primary BOOLEAN, -- true if this is primary organization, false if secondary and depends on a primary
  primary_organization UUID REFERENCES ehr.organization(id), -- if secondary, optionally references a primary organization
  name VARCHAR(50), -- organization usual name
  description VARCHAR(200), -- description
  is_active BOOLEAN -- true if active
);

create TABLE ehr.organization_addresses (
  id UUID PRIMARY KEY DEFAULT ehr.uuid_generate_v4(),
  organization_id UUID REFERENCES ehr.organization(id), -- an organization may have multiple addresses
  is_primary BOOLEAN, -- true if this is the primary address
  line1 VARCHAR(50),
  line2 VARCHAR(50),
  line3 VARCHAR(50),
  city VARCHAR(50),
  zip VARCHAR(20),
  state VARCHAR(50),
  country_id INT REFERENCES ehr.territory(code),
  other_details VARCHAR(250)
);

COMMENT ON TABLE ehr.organization_addresses IS 'Reasonable format for storing addresses';

CREATE TABLE ehr.organization_contact (
  id UUID PRIMARY KEY DEFAULT ehr.uuid_generate_v4(),
  organization_id UUID REFERENCES ehr.organization(id), -- and organization may have multiple contacts
  email_address VARCHAR(50),
  web_site VARCHAR(50),
  salutation VARCHAR(10),
  contact_name VARCHAR(50),
  job_title VARCHAR(100),
  department VARCHAR(100),
  work_phone VARCHAR(25),
  cell_phone VARCHAR(25),
  fax_number VARCHAR(25),
  other_details VARCHAR(200)
);

COMMENT ON TABLE ehr.organization_contact IS 'Contact specifications';

CREATE TABLE ehr.organization_assignment (
  organization_id UUID REFERENCES ehr.organization(id), -- organization
  party_id UUID REFERENCES ehr.party_identified(id) -- an identified party assigned to this organization
);

COMMENT ON TABLE ehr.organization_assignment IS 'Cross reference of assigned users to organizations';


create TABLE ehr.catalog (
  id UUID NOT NULL PRIMARY KEY DEFAULT ehr.uuid_generate_v4(),
  name VARCHAR(50), -- name of the catalog
  description VARCHAR(255), -- literal describing the purpose of the catalog
  version_number INT, -- version
  author UUID REFERENCES ehr.party_identified(id), -- creator of this catalog
  released DATE, -- date of release
  active BOOLEAN -- false  if this catalog is retired
);

COMMENT ON TABLE ehr.catalog IS 'definition of catalog, used to aggregate templates, archetypes, forms etc.';

create table ehr.program (
  id UUID PRIMARY KEY DEFAULT ehr.uuid_generate_v4(),
  name VARCHAR(50),         -- usual name of the program
  description VARCHAR(100), -- short literal describing the program
  manager UUID REFERENCES ehr.party_identified(id), -- person managing the program
  implicit_consent BOOLEAN, -- Y if consent is not required to add a patient to the program
  escalation_timeout INT, -- HOURS: specifies the required time for completion (f.ex. 3 days) before escalation
  reminders_occurences INT, -- number of reminders before escalation
  reminders_frequency INT, -- frequency of reminders (HOURS)
  catalog UUID REFERENCES ehr.catalog(id) -- catalog used along with this program
);

COMMENT ON TABLE ehr.program IS 'Care program definition';

CREATE TABLE ehr.organization_program (
  organization_id UUID REFERENCES ehr.organization(id), -- organization
  program_id UUID REFERENCES ehr.program(id)
);

COMMENT ON TABLE ehr.organization_program IS 'Cross reference organization/programs';

-- cross references
create TABLE ehr.catalog_templates (
  catalog UUID REFERENCES ehr.catalog(id), -- catalog id
  template VARCHAR(200) -- template identifier (must be maintained by the knowledge service)
);

create TABLE ehr.catalog_forms (
  catalog UUID REFERENCES ehr.catalog(id), -- catalog id
  form VARCHAR(200) -- form identifier (must be maintained by the knowledge service)
);

create TABLE ehr.catalog_reports (
  catalog UUID REFERENCES ehr.catalog(id), -- catalog id
  report VARCHAR(200) -- report identifier (must be maintained by the knowledge service)
);

create TABLE ehr.catalog_interfaces (
  catalog UUID REFERENCES ehr.catalog(id), -- catalog id
  interface VARCHAR(200) -- interface identifier (must be maintained by the knowledge service)
);

create TABLE ehr.program_party (
  program UUID REFERENCES ehr.program(id), -- id of a program
  resource UUID REFERENCES ehr.party_identified(id) -- id of a party used as a program resource
);

COMMENT ON TABLE ehr.program_party IS 'n to m relations to define the members of a program';

create table ehr.event_context (
	id UUID primary key DEFAULT ehr.uuid_generate_v4(),
 	start_time timestamp not null,
	end_time timestamp null,
	facility UUID references ehr.party_identified(id), -- points to a party identified 
	location varchar(50),
	setting UUID references ehr.concept(id), -- codeset setting, see ehr_im section 5
	program UUID references ehr.program(id), -- the program defined for this context
  sys_period tstzrange NOT NULL -- temporal table
);

-- change history table
create table ehr.event_context_history (like ehr.event_context);

CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON ehr.event_context
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'ehr.event_context_history', true);

COMMENT ON TABLE ehr.event_context IS 'defines the context of an event (time, who, where... see openEHR IM 5.2';

create table ehr.participation (
  id UUID primary key DEFAULT ehr.uuid_generate_v4(),
  event_context UUID NOT NULL REFERENCES ehr.event_context(id),
  performer UUID references ehr.party_identified(id),
  function VARCHAR(50),
  mode VARCHAR(50),
  start_time timestamp,
  sys_period tstzrange NOT NULL -- temporal table
);

-- change history table
create table ehr.participation_history (like ehr.participation);

CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON ehr.participation
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'ehr.participation_history', true);

COMMENT ON TABLE ehr.participation IS 'define a participating party for an event f.ex.';

--storeComposition table ehr.event_participation (
--	context UUID references ehr.event_context(id),
--	participation UUID references ehr.participation(id)
--);

-- COMMENT ON TABLE ehr.event_participation IS 'specifies parties participating in an event context';

-- TODO make it compliant with openEHR common IM section 6
-- storeComposition table ehr.versioned (
-- id UUID PRIMARY KEY DEFAULT ehr.uuid_generate_v4(),-- this is used by the object which this version def belongs to (composition etc.)
-- object UUID not null, -- a versioning strategy identifier, can be generated by the RDBMS (PG)
-- created timestamp default NOW()
-- );

-- COMMENT ON TABLE ehr.versioned IS 'used to reference a versioning system';

-- change control
create table ehr.contribution (
	id UUID primary key DEFAULT ehr.uuid_generate_v4(),
	-- audit details
	system_id UUID references ehr.system(id),
	committer UUID references ehr.party_identified(id),
	time_committed timestamp default NOW(),
	change_type UUID references ehr.concept(id),
	description varchar(100),
  sys_period tstzrange NOT NULL -- temporal table
);

-- change history table
CREATE TABLE ehr.contribution_history (like ehr.contribution);

COMMENT ON TABLE ehr.contribution IS 'Contribution table, compositions reference this table';

CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON ehr.contribution
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'ehr.contribution_history', true);

create type ehr.contribution_data_type as enum('composition', 'folder');
create type ehr.contribution_state as enum('complete', 'incomplete', 'deleted');

create table ehr.contribution_version (
	id UUID not null primary key DEFAULT ehr.uuid_generate_v4(),
	ehr_id UUID references ehr.ehr(id),
	contribution_type ehr.contribution_data_type, -- specifies the type of data it contains
	state ehr.contribution_state, -- current state in lifeCycleState
	signature varchar(50),
	contribution_id UUID references ehr.contribution(id),
  sys_period tstzrange NOT NULL -- temporal table
);

-- change history table
CREATE TABLE ehr.contribution_version_history (like ehr.contribution_version);

CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON ehr.contribution_version
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'ehr.contribution_version_history', true);

COMMENT ON TABLE ehr.contribution_version IS 'Definition of contribution version and state';

create table ehr.composition (
    id UUID PRIMARY KEY DEFAULT ehr.uuid_generate_v4(),
    ehr_id UUID references ehr.ehr(id),
--    version UUID references ehr.versioned(id),
    in_contribution UUID references ehr.contribution_version(id), -- in contribution version 
    preceeding_version UUID references ehr.contribution_version(id), -- previous version if any
    active boolean default true, -- true if this composition is still valid (e.g. not replaced yet)
    is_persistent boolean default true,
    language varchar(5) references ehr.language(code), -- pointer to the language codeset. Indicates what broad category this Composition is belogs to, e.g. �persistent� - of longitudinal validity, �event�, �process� etc.
    territory int references ehr.territory(code), -- Name of territory in which this Composition was written. Coded from openEHR �countries� code set, which is an expression of the ISO 3166 standard.
    composer UUID not null references ehr.party_identified(id), -- points to the PARTY_PROXY who has created the composition
    context UUID references ehr.event_context(id), -- point to EVENT_CONTEXT structure
    sys_period tstzrange NOT NULL -- temporal table
    -- item UUID not null, -- point to the first section in composition
);

-- change history table
CREATE TABLE ehr.composition_history (like ehr.composition);

COMMENT ON TABLE ehr.composition IS 'Composition table';

CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON ehr.composition
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'ehr.composition_history', true);

create type ehr.entry_type as enum('section','care_entry', 'admin', 'proxy');

create table ehr.entry (
	id UUID primary key DEFAULT ehr.uuid_generate_v4(),
	composition_id UUID references ehr.composition(id), -- belong to composition
	sequence int, -- ordering sequence number
	item_type ehr.entry_type,
  template_id VARCHAR(250), -- operational template to rebuild the structure entry
  template_uuid UUID, -- optional, used with operational template for consistency
  archetype_id VARCHAR(250), -- ROOT archetype id (not sure still in use...)
  category UUID null references ehr.concept(id), -- used to specify the type of content: Evaluation, Instruction, Observation, Action with different languages
  entry JSON,            -- actual content version dependent (9.3: json, 9.4: jsonb). entry is either CARE_ENTRY or ADMIN_ENTRY
  sys_period tstzrange NOT NULL -- temporal table
);

-- change history table
CREATE TABLE ehr.entry_history (like ehr.entry);

COMMENT ON TABLE ehr.entry IS 'this table hold the actual archetyped data values (from a template)';

CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON ehr.entry
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'ehr.entry_history', true);

create table ehr.audit_trail (
    id UUID PRIMARY KEY DEFAULT ehr.uuid_generate_v4(),
    composition_id UUID references ehr.composition(id),
    committer UUID not null references ehr.party_identified(id), -- contributor
    date_created TIMESTAMP,
    party UUID not null references ehr.party_identified(id), -- patient 
    serial_version VARCHAR(50),
    system_id UUID references ehr.system(id)
); 

