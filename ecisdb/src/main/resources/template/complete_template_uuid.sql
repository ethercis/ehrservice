-- this script complete column template_uuid in table ehr.entry using table ehr.template
-- this assumes table ehr.template is up to date!
UPDATE ehr.entry
   SET template_uuid = ehr.template.uid
FROM ehr.template
   WHERE ehr.template.template_id = ehr.entry.template_id;
