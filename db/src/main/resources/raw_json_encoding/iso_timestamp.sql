create or replace function ehr.iso_timestamp(timestamp with time zone)
   returns varchar as $$
  select substring(xmlelement(name x, $1)::varchar from 4 for 19)
$$ language sql immutable;