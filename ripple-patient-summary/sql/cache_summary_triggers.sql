-- create the cache summary triggers
-- C.Chevalley May 2017
-- See LICENSE.txt for licensing details
-------------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION ehr.cache_summary_update_insert_trigger()
  RETURNS TRIGGER AS
  $trigger_entry$
  DECLARE
    cacheTemplateUUID UUID;
    ehrId UUID;
  BEGIN
    RAISE INFO 'UPDATE INSERT SUMMARY CALC INVOKED %', TG_OP;
    SELECT uid FROM ehr.template WHERE template_id = 'Ripple Dashboard Cache.v1' INTO cacheTemplateUUID;

    IF (NEW.template_uuid IS NULL OR NEW.template_uuid != cacheTemplateUUID) THEN
      RAISE INFO 'NON SUMMARY ENTRY CALC INVOKED FOR UPDATE OR DELETE';
        -- perform cache calculation for the correlated ehr Id
        SELECT ehr_id
        FROM ehr.composition
          INNER JOIN ehr.entry ntry
            ON composition.id = ntry.composition_id
            WHERE ntry.id = NEW.id
          INTO ehrId;
        RAISE INFO 'INSERT/UPDATE: UPDATE CACHE FOR EHR_ID %', ehrId;
        PERFORM ehr.update_cache_for_ehr(ehrId);
    ELSE
      RAISE INFO 'NP NON SUMMARY ENTRY CALC INVOKED FOR UPDATE OR DELETE, %', NEW.template_id;
    END IF;
    RETURN NEW;

  END
  $trigger_entry$
LANGUAGE plpgsql;

-- composition level trigger for DELETE (required for ON DELETE...CASCADE order)
CREATE OR REPLACE FUNCTION ehr.cache_summary_delete_trigger()
  RETURNS TRIGGER AS
  $trigger_compo$
  DECLARE
    cacheTemplateUUID UUID;
    foundTemplateUUID UUID;
    ehrId UUID;
  BEGIN
    RAISE INFO 'DELETE SUMMARY CALC INVOKED %', TG_OP;
    SELECT uid FROM ehr.template WHERE template_id = 'Ripple Dashboard Cache.v1' INTO cacheTemplateUUID;

    SELECT template_uuid FROM
            ehr.entry
            INNER JOIN ehr.composition compo
              ON compo.id = entry.composition_id
              WHERE entry.composition_id = OLD.id INTO foundTemplateUUID;

    IF (foundTemplateUUID IS NULL OR foundTemplateUUID != cacheTemplateUUID) THEN
      RAISE INFO 'NON SUMMARY ENTRY CALC INVOKED FOR DELETE!';
      -- perform cache calculation for the correlated ehr Id

      RAISE INFO 'DELETE: UPDATE CACHE FOR EHR_ID % (composition_id %)', OLD.ehr_id, OLD.id;
      PERFORM ehr.update_cache_for_ehr(OLD.ehr_id);
    ELSE
      RAISE INFO 'NOP NON SUMMARY ENTRY CALC INVOKED FOR DELETE! %', OLD.id;
    END IF;
    RETURN OLD;
  END
  $trigger_compo$
LANGUAGE plpgsql;

-- versioning for event_context: INSERT/UPDATE
CREATE OR REPLACE FUNCTION ehr.event_context_versioning_upsert_trigger()
  RETURNS TRIGGER AS
  $trigger_version$
  DECLARE
    cacheTemplateUUID UUID;
    foundTemplateUUID UUID;
  BEGIN
    SELECT uid FROM ehr.template WHERE template_id = 'Ripple Dashboard Cache.v1' INTO cacheTemplateUUID;

      RAISE INFO 'UPSERT EVENT_CONTEXT_VERSIONING_TRIGGER';

      SELECT template_uuid
        FROM ehr.event_context
        INNER JOIN ehr.entry ntry
          ON event_context.composition_id = ntry.composition_id
        WHERE event_context.id = NEW.id
        INTO foundTemplateUUID;

      IF (foundTemplateUUID IS NULL OR foundTemplateUUID != cacheTemplateUUID) THEN
        -- perform cache calculation for the correlated ehr Id
        PERFORM ehr.fct_versioning('sys_period', 'ehr.event_context_history', true);
      END IF;
      RETURN NEW;
  END
  $trigger_version$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ehr.event_context_versioning_delete_trigger()
  RETURNS TRIGGER AS
  $$
  DECLARE
  cacheTemplateUUID UUID;
  foundTemplateUUID UUID;
  BEGIN
    RAISE INFO 'DELETE EVENT_CONTEXT_VERSIONING_TRIGGER';

    SELECT template_uuid
    FROM ehr.event_context
      INNER JOIN ehr.entry ntry
        ON event_context.composition_id = ntry.composition_id
    WHERE event_context.id = OLD.id
    INTO foundTemplateUUID;

    IF (foundTemplateUUID IS NULL OR foundTemplateUUID != cacheTemplateUUID) THEN
      -- perform cache calculation for the correlated ehr Id
      PERFORM ehr.fct_versioning('sys_period', 'ehr.event_context_history', true);
    END IF;

  RETURN OLD;
  END

  $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ehr.is_event_context_summary_cache(UUID)
  RETURNS BOOLEAN AS
  $create_system_party$
  DECLARE
    eventContextId ALIAS FOR $1;
    cacheTemplateUUID UUID;
    foundTemplateUUID UUID;
  BEGIN
    SELECT uid FROM ehr.template WHERE template_id = 'Ripple Dashboard Cache.v1' INTO cacheTemplateUUID;

    SELECT template_uuid
    FROM ehr.event_context
      INNER JOIN ehr.entry ntry
        ON event_context.composition_id = ntry.composition_id
    WHERE event_context.id = eventContextId
    INTO foundTemplateUUID;

    RAISE INFO 'IS EVENT CONTEXT SUMMARY: context_id %, RESULT % (foundTemplate %)', eventContextId, foundTemplateUUID IS NULL OR foundTemplateUUID = cacheTemplateUUID, foundTemplateUUID;

    RETURN foundTemplateUUID IS NULL OR foundTemplateUUID = cacheTemplateUUID;
  END
  $create_system_party$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ehr.entry_versioning_trigger()
  RETURNS TRIGGER AS
  $trigger_entry$
  DECLARE
    cacheTemplateUUID UUID;
    ehrId UUID;
  BEGIN
    SELECT uid FROM ehr.template WHERE template_id = 'Ripple Dashboard Cache.v1' INTO cacheTemplateUUID;

    IF (TG_OP = 'INSERT') THEN
      RAISE INFO 'INSERT ENTRY_VERSIONING_TRIGGER';
      IF (NEW.template_uuid <> cacheTemplateUUID) THEN
        RAISE INFO 'INSERT ENTRY_VERSIONING_TRIGGER: NON CACHE';
        -- perform cache calculation for the correlated ehr Id
        EXECUTE ehr.versioning('sys_period', 'ehr.entry_history', true);
      ELSE
        RAISE INFO 'INSERT ENTRY_VERSIONING_TRIGGER: NON CACHE FOR SUMMARY! template new: % template found %', NEW.template_id, cacheTemplateUUID;
      END IF;
      RETURN NEW;
    END IF;

    RAISE INFO 'NON INSERT ENTRY_VERSIONING_TRIGGER';

    IF (OLD.template_uuid <> cacheTemplateUUID) THEN
        -- perform cache calculation for the correlated ehr Id
      PERFORM ehr.versioning('sys_period', 'ehr.entry_history', true);
    END IF;

    RETURN OLD;

  END
  $trigger_entry$
LANGUAGE plpgsql;