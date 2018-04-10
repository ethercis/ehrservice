-- CTE enforces 1-to-1 entry-composition relationship since multiple entries can be
-- associated to one composition. This is not supported at this stage.
CREATE OR REPLACE FUNCTION ehr.js_composition(UUID)
  RETURNS JSON AS
  $$
  DECLARE
    composition_uuid ALIAS FOR $1;
  BEGIN
    RETURN (
      WITH composition_data AS (
          SELECT
            composition.language  as language,
            composition.territory as territory,
            composition.composer  as composer,
            event_context.id      as context_id,
            territory.twoletter   as territory_code,
            entry.template_id     as template_id,
            entry.archetype_id    as archetype_id,
            concept.conceptid     as category_defining_code,
            concept.description   as category_description,
            entry.entry           as content
          FROM ehr.composition
            INNER JOIN ehr.entry ON entry.composition_id = composition.id
            LEFT JOIN ehr.event_context ON event_context.composition_id = composition.id
            LEFT JOIN ehr.territory ON territory.code = composition.territory
            LEFT JOIN ehr.concept ON concept.id = entry.category
          WHERE composition.id = composition_uuid
        LIMIT 1
      )
      SELECT
        jsonb_strip_nulls(
            jsonb_build_object(
                '@class', 'COMPOSITION',
                'language', ehr.js_code_phrase(language, 'ISO_639-1'),
                'territory', ehr.js_code_phrase(territory_code, 'ISO_3166-1'),
                'composer', ehr.js_party(composer),
                'category',
                ehr.js_dv_coded_text(category_description, ehr.js_code_phrase(category_defining_code :: TEXT, 'openehr')),
                'context', ehr.js_context(context_id),
                'content', content
            )
        )
      FROM composition_data
    );
  END
  $$
LANGUAGE plpgsql;