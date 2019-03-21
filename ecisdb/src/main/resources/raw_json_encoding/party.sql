CREATE OR REPLACE FUNCTION ehr.js_party(UUID)
  RETURNS JSON AS
  $$
  DECLARE
    party_id ALIAS FOR $1;
  BEGIN
    RETURN (
      SELECT ehr.js_party_identified(name,
                                  ehr.js_party_ref(party_ref_value, party_ref_scheme, party_ref_namespace, party_ref_type))
      FROM ehr.party_identified
      WHERE id = party_id
    );
  END
  $$
LANGUAGE plpgsql;