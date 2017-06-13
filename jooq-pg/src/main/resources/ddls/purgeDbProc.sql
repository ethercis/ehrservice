CREATE OR REPLACE FUNCTION ehr.purge_db()
  RETURNS TEXT AS
  $$
  BEGIN
    TRUNCATE ehr.compo_xref CASCADE;
    TRUNCATE ehr.containment CASCADE ;
    TRUNCATE ehr.ehr CASCADE;
    TRUNCATE ehr.entry_history CASCADE;
    TRUNCATE ehr.composition_history CASCADE;
    TRUNCATE ehr.event_context CASCADE;
    TRUNCATE ehr.event_context_history CASCADE;
    TRUNCATE ehr.participation_history CASCADE;
    TRUNCATE ehr.contribution_history CASCADE;
    TRUNCATE ehr.status_history CASCADE;
    TRUNCATE ehr.party_identified CASCADE;
    TRUNCATE ehr.system CASCADE;
    TRUNCATE ehr.entry CASCADE;
    TRUNCATE ehr.participation CASCADE;
    TRUNCATE ehr.event_context CASCADE;
    TRUNCATE ehr.party_identified CASCADE;
    TRUNCATE ehr.identifier CASCADE;
    TRUNCATE ehr.composition CASCADE;
    TRUNCATE ehr.contribution CASCADE;
    RETURN 'Persisted RM data deleted...';
  END
  $$
LANGUAGE plpgsql;