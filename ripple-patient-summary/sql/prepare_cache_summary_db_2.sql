-- this script enhance an existing EtherCIS DB to support the cache summary extension it
-- is called by prepare_db shell script
-- PASS 1: build up the heading cross-ref heading-templates
-- C.Chevalley May 2017
-- See LICENSE.txt for licensing details
-------------------------------------------------------------------------------------------------
INSERT INTO ehr.template_heading_xref
VALUES
  ('ORDERS', (SELECT uid FROM ehr.template WHERE template_id='IDCR - Medication List.v0') ),
  ('ORDERS', (SELECT uid FROM ehr.template WHERE template_id='IDCR Procedures List.v0') ),
  ('ORDERS', (SELECT uid FROM ehr.template WHERE template_id='IDCR - Laboratory Order.v0') ),
  ('ORDERS', (SELECT uid FROM ehr.template WHERE template_id='IDCR - Service Request.v0') ),
  ('RESULTS', (SELECT uid FROM ehr.template WHERE template_id='IDCR - Generic MDT Output Report.v0') ),
  ('RESULTS', (SELECT uid FROM ehr.template WHERE template_id='IDCR - Minimal MDT Output Report.v0')),
  ('RESULTS', (SELECT uid FROM ehr.template WHERE template_id='IDCR - Laboratory Test Report.v0')),
  ('VITALS', (SELECT uid FROM ehr.template WHERE template_id='IDCR - Vital Signs Encounter.v1')),
  ('VITALS', (SELECT uid FROM ehr.template WHERE template_id='PSKY - Healthcheck.v0')),
  ('DIAGNOSES', (SELECT uid FROM ehr.template WHERE template_id='DiADeM Assessment.v1'));


