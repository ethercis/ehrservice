-- identify entry templates_ids (e.g. used template in composition) but without a corresponding template text (.opt)
-- into the knowledge repository.
-- The templates must be resolved to set referential integrity in the DB
-- This script assumes that operational_templates are stored in ${PG_DATA}/ecis_knowledge/operational_templates
DROP TYPE IF EXISTS template_property;
CREATE TYPE template_property AS (template_uid UUID, template_id TEXT);

WITH template_paths AS (
	SELECT PATH FROM ehr.opt_list('./ecis_knowledge/operational_templates')
), template_props AS (
	SELECT ehr.ecis_template_props(path) FROM template_paths
), template_defs AS (
	SELECT DISTINCT (ecis_template_props::TEXT::template_property).template_uid, (ecis_template_props::TEXT::template_property).template_id
		FROM template_props
		ORDER BY template_id
)
SELECT DISTINCT entry.template_id
FROM ehr.entry
WHERE entry.template_id NOT IN (SELECT template_defs.template_id FROM template_defs)
ORDER BY template_id;