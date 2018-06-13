Adding column to the template table to allow caching of compiled operational template and introspection
=======================================================================================================
CHC, 10.05.18

alter table ehr.template add column if not exists introspect jsonb;
alter table ehr.template add column if not exists parsed_opt bytea;
alter table ehr.template add column if not exists visitor bytea;
alter table ehr.template add column if not exists crc BIGINT;

If the alter blocks
===================

identify the blocking process(es):

select pid, pg_blocking_pids(pid) as blocked_by, query as blocked_query
from pg_stat_activity
where pg_blocking_pids(pid)::text != '{}';

Terminate or kill the processes:

select pg_cancel_backend(<PID>)

select pg_terminate_backend(<PID>)