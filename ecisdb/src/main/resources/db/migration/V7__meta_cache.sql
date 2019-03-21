alter table ehr.template add column if not exists introspect jsonb;
alter table ehr.template add column if not exists parsed_opt bytea;
alter table ehr.template add column if not exists visitor bytea;
alter table ehr.template add column if not exists crc BIGINT;