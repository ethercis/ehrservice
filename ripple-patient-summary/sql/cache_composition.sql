-- create the required functions to perform summary calculations manually or invoked by a trigger
-- C.Chevalley May 2017
-- See LICENSE.txt for licensing details
-------------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION ehr.get_cache_committer()
  RETURNS UUID AS
  $create_system_party$
  DECLARE
    sysIdentifier TEXT;
    sysPartyId    UUID;
  BEGIN
    sysIdentifier := '$RIPPLE_SUMMARY_CACHE$';

    IF (SELECT NOT EXISTS(SELECT *
                          FROM ehr.party_identified
                          WHERE party_identified.name = sysIdentifier))
    THEN
      INSERT INTO ehr.party_identified (name) VALUES (sysIdentifier)
      RETURNING id
        INTO sysPartyId;
    ELSE
      SELECT ehr.party_identified.id
      FROM ehr.party_identified
      WHERE party_identified.name = sysIdentifier
      INTO sysPartyId;
    END IF;
    RETURN sysPartyId;
  END
  $create_system_party$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION ehr.get_cache_system()
  RETURNS UUID AS
  $create_system_party$
  DECLARE
    sysDescription TEXT;
    systemId       UUID;
    sysSetting     TEXT;
  BEGIN
    sysDescription := 'LOCAL_HOST';
    sysSetting := 'cache@localhost';

    IF (SELECT NOT EXISTS(SELECT *
                          FROM ehr.system
                          WHERE system.description = sysDescription))
    THEN
      INSERT INTO ehr.system (description, settings) VALUES (sysDescription, sysSetting)
      RETURNING id
        INTO systemId;
    ELSE
      SELECT system.id
      FROM ehr.system
      WHERE system.settings = sysSetting
      INTO systemId;
    END IF;
    RETURN systemId;
  END
  $create_system_party$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ehr.set_cache_for_ehr(UUID)
  RETURNS UUID AS
  $create_system_party$
  DECLARE
    ehrId ALIAS FOR $1;
    languageId        VARCHAR(5);
    territoryId       INT;
    composerId        UUID;
    compositionId     UUID;
    templateUID       UUID;
    templateId        TEXT;
    containmentArchetypeId TEXT;
    settingId         UUID;
    sysTransationTime TIMESTAMP;
    sysPeriod         TSTZRANGE;
  BEGIN
    templateId := 'Ripple Dashboard Cache.v1';
    containmentArchetypeId = 'openEHR_EHR_COMPOSITION_ripple_cache_v1';

    SELECT template.uid
    FROM ehr.template
    WHERE template.template_id = templateId
    INTO templateUID;

    IF (SELECT NOT EXISTS(SELECT *
                          FROM ehr.composition
                            INNER JOIN ehr.entry ntry ON ntry.composition_id = composition.id
                          WHERE composition.ehr_id = ehrId AND ntry.template_uuid = templateUID))
    THEN
      languageId := 'en';
      composerId := ehr.get_cache_committer();
      sysTransationTime := now();
      sysPeriod := tstzrange(now(), 'infinity', '[)');
      SELECT territory.code
      FROM ehr.territory
      WHERE territory.twoletter = 'GB'
      INTO territoryId;
      -- use Other Care as a setting
      SELECT id
      FROM ehr.concept
      WHERE conceptid = 238 INTO settingId;
      --
      INSERT INTO ehr.composition (ehr_id, language, territory, composer, sys_transaction, sys_period)
      VALUES (ehrId, languageId, territoryId, composerId, sysTransationTime, sysPeriod)
      RETURNING id
        INTO compositionId;
      -- create a dummy entry holding the current template id
      INSERT INTO ehr.entry (composition_id, item_type, template_id, template_uuid, sys_transaction, sys_period)
      VALUES (compositionId, 'admin', templateId, templateUID, sysTransationTime, sysPeriod);
      -- create a context for this composition with the calculated summary
      INSERT INTO ehr.event_context (composition_id, start_time, other_context, setting, sys_transaction, sys_period)
      VALUES (compositionId,
              now(),
              ehr.other_context_for_ehr(ehrId),
              settingId,
              sysTransationTime,
              sysPeriod);
      -- create a dummy containment entry for this composition (ignore the path)
      INSERT INTO ehr.containment (comp_id, label)
        VALUES (compositionId, containmentArchetypeId::LTREE);
    ELSE
      SELECT entry.composition_id
      FROM ehr.entry
      WHERE entry.template_uuid = templateUID
      INTO compositionId;
    END IF;
    RETURN compositionId;
  END
  $create_system_party$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ehr.update_cache_for_ehr(UUID)
  RETURNS UUID AS
  $create_system_party$
  DECLARE
    ehrId             ALIAS FOR $1;
    retCompositionId  UUID;
    templateUID       UUID;
    templateId        TEXT;
  BEGIN
    templateId := 'Ripple Dashboard Cache.v1';
    SELECT template.uid FROM ehr.template WHERE template.template_id = templateId INTO templateUID;

    IF (SELECT NOT EXISTS(SELECT *
                          FROM ehr.composition
                            INNER JOIN ehr.entry ntry ON ntry.composition_id = composition.id
                          WHERE composition.ehr_id = ehrId AND ntry.template_uuid = templateUID))
    THEN
      -- make sure this ehrId exists (can be invoked from a DELETE ehr CASCADE...)
      PERFORM 1 FROM ehr.ehr WHERE ehr.id = ehrId;
      IF FOUND THEN
        RAISE INFO 'Non existing Summary Cache for % creating a new one...', ehrId;
        RETURN ehr.set_cache_for_ehr(ehrId);
      END IF;
    ELSE
      UPDATE ehr.event_context
        SET other_context = ehr.other_context_for_ehr(ehrId),
            sys_transaction = now(),
            sys_period = tstzrange(now(), 'infinity', '[)')
        FROM
          ehr.composition,
          ehr.entry
        WHERE
          event_context.composition_id = composition.id AND
          event_context.composition_id = entry.composition_id AND
          composition.ehr_id = ehrId AND
          entry.template_uuid = templateUID
      RETURNING composition.id INTO retCompositionId;
    END IF;
    RETURN retCompositionId;
  END
  $create_system_party$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ehr.other_context_for_ehr(UUID)
  RETURNS JSONB AS
  $other_context_ehr$
  DECLARE
    ehrId ALIAS FOR $1;
    ordersLastDate TIMESTAMPTZ;
    ordersCount BIGINT;
    resultsLastDate TIMESTAMPTZ;
    resultsCount BIGINT;
    vitalsLastDate TIMESTAMPTZ;
    vitalsCount BIGINT;
    diagnosesLastDate TIMESTAMPTZ;
    diagnosesCount BIGINT;
  BEGIN
    SELECT
      orders_last,
      orders_count,
      results_last,
      results_count,
      vitals_last,
      vitals_count,
      diagnoses_last,
      diagnoses_count

    FROM
          (select ehr_id, COALESCE(count(entry.id), 0) as results_count, MAX(entry.sys_transaction) as results_last from ehr.entry
            INNER JOIN ehr.composition compo ON compo.id = ehr.entry.composition_id
            INNER JOIN ehr.ehr ehr ON ehr.id = compo.ehr_id
            where entry.template_id IN (
              select tpl.template_id from ehr.template tpl
                INNER JOIN ehr.template_heading_xref xref
                  ON xref.template_id = tpl.uid
                INNER JOIN ehr.heading head
                  ON head.code = xref.heading_code
              WHERE head.code = 'RESULTS'
            )
          AND ehr.id = ehrId
      GROUP BY ehr_id
      ) results
      FULL OUTER JOIN
            (select ehr_id, COALESCE(count(entry.id), 0) as orders_count, MAX(entry.sys_transaction) as orders_last
             from ehr.entry
               INNER JOIN ehr.composition compo ON compo.id = ehr.entry.composition_id
               INNER JOIN ehr.ehr ehr ON ehr.id = compo.ehr_id
               where entry.template_id IN (
                 select tpl.template_id from ehr.template tpl
                   INNER JOIN ehr.template_heading_xref xref
                     ON xref.template_id = tpl.uid
                   INNER JOIN ehr.heading head
                     ON head.code = xref.heading_code
                 WHERE head.code = 'ORDERS'
             )
             AND ehr.id = ehrId
             GROUP BY ehr_id) orders
        ON orders.ehr_id = results.ehr_id
      FULL OUTER JOIN
            (select ehr_id, COALESCE(count(entry.id), 0) as vitals_count, MAX(entry.sys_transaction) as vitals_last from ehr.entry
              INNER JOIN ehr.composition compo ON compo.id = ehr.entry.composition_id
              INNER JOIN ehr.ehr ehr ON ehr.id = compo.ehr_id
              where entry.template_id IN (
                select tpl.template_id from ehr.template tpl
                  INNER JOIN ehr.template_heading_xref xref
                    ON xref.template_id = tpl.uid
                  INNER JOIN ehr.heading head
                    ON head.code = xref.heading_code
                WHERE head.code = 'VITALS'
            )
            AND ehr.id = ehrId
            GROUP BY ehr_id
            ) vitals
        ON orders.ehr_id = vitals.ehr_id
      FULL OUTER JOIN
            (select ehr_id, COALESCE(count(entry.id), 0) as diagnoses_count, MAX(entry.sys_transaction) as diagnoses_last from ehr.entry
              INNER JOIN ehr.composition compo ON compo.id = ehr.entry.composition_id
              INNER JOIN ehr.ehr ehr ON ehr.id = compo.ehr_id
              where entry.template_id IN (
                select tpl.template_id from ehr.template tpl
                  INNER JOIN ehr.template_heading_xref xref
                    ON xref.template_id = tpl.uid
                  INNER JOIN ehr.heading head
                    ON head.code = xref.heading_code
                WHERE head.code = 'DIAGNOSES'
            )
            AND ehr.id = ehrId
            GROUP BY ehr_id
            ) diagnoses
      ON orders.ehr_id = diagnoses.ehr_id
    INTO ordersLastDate,ordersCount,resultsLastDate,resultsCount,vitalsLastDate,vitalsCount,diagnosesLastDate,diagnosesCount;
    RETURN ehr.other_context_summary_field(ordersLastDate, ordersCount, resultsLastDate, resultsCount, vitalsLastDate, vitalsCount, diagnosesLastDate, diagnosesCount);

  END
  $other_context_ehr$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ehr.other_context_for_ehr(UUID)
  RETURNS JSONB AS
  $other_context_ehr$
  DECLARE
    ehrId ALIAS FOR $1;
    ordersLastDate TIMESTAMPTZ;
    ordersCount BIGINT;
    resultsLastDate TIMESTAMPTZ;
    resultsCount BIGINT;
    vitalsLastDate TIMESTAMPTZ;
    vitalsCount BIGINT;
    diagnosesLastDate TIMESTAMPTZ;
    diagnosesCount BIGINT;
  BEGIN
    select
      count(id) FILTER (WHERE heading_code = 'ORDERS') as orders_count,
      max(sys_transaction) FILTER (WHERE heading_code = 'ORDERS') as orders_last,
      count(id) FILTER (WHERE heading_code = 'RESULTS') as results_count,
      max(sys_transaction) FILTER (WHERE heading_code = 'RESULTS') as results_last,
      count(id) FILTER (WHERE heading_code = 'VITALS') as vitals_count,
      max(sys_transaction) FILTER (WHERE heading_code = 'ORDERS') as vitals_last,
      count(id) FILTER (WHERE heading_code = 'DIAGNOSES') as diagnoses_count,
      max(sys_transaction) FILTER (WHERE heading_code = 'DIAGNOSES') as diagnoses_last
      FROM (
          select
          compo.ehr_id,
          compo.id,
          compo.sys_transaction,
          xref.heading_code
          from ehr.entry
          JOIN ehr.composition compo ON entry.composition_id = compo.id
          JOIN ehr.ehr ehr ON ehr.id = compo.ehr_id
          JOIN ehr.template templ ON templ.template_id = entry.template_id
          LEFT JOIN ehr.template_heading_xref xref ON xref.template_id = templ.uid
          WHERE heading_code IS NOT NULL
          ORDER BY ehr_id
      ) subcount
      WHERE ehr_id = ehrId
      GROUP BY ehr_id

  INTO ordersCount, ordersLastDate, resultsCount, resultsLastDate, vitalsCount, vitalsLastDate, diagnosesCount, diagnosesLastDate;
    RETURN ehr.other_context_summary_field(ordersLastDate, ordersCount, resultsLastDate, resultsCount, vitalsLastDate, vitalsCount, diagnosesLastDate, diagnosesCount);

  END
  $other_context_ehr$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ehr.other_context()
  RETURNS JSONB AS
  $body$
  DECLARE
    ordersLastDate TIMESTAMPTZ;
    ordersCount BIGINT;
    resultsLastDate TIMESTAMPTZ;
    resultsCount BIGINT;
    vitalsLastDate TIMESTAMPTZ;
    vitalsCount BIGINT;
    diagnosesLastDate TIMESTAMPTZ;
    diagnosesCount BIGINT;
  BEGIN
    SELECT  orders_last,
            orders_count,
            results_last,
            results_count,
            vitals_last,
            vitals_count,
            diagnoses_last,
            diagnoses_count
        FROM ehr.ripple_patient_summary
        INTO ordersLastDate,ordersCount,resultsLastDate,resultsCount,vitalsLastDate,vitalsCount,diagnosesLastDate,diagnosesCount;

    RETURN ehr.other_context_summary_field(ordersLastDate, ordersCount, resultsLastDate, resultsCount, vitalsLastDate, vitalsCount, diagnosesLastDate, diagnosesCount);
  END
  $body$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ehr.init_other_context()
  RETURNS VOID AS
  $body$
  BEGIN
    SELECT ehr.set_cache_for_ehr(ehr.id)
    FROM ehr.ehr;
  END
  $body$
LANGUAGE plpgsql;