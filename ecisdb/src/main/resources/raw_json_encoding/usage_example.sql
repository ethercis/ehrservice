select ehr.js_code_phrase('en', 'ISO_639-1');

select ehr.js_dv_coded_text('event', ehr.code_phrase('433', 'openehr'));

select ehr.js_dv_text('event');

select ehr.object_version_id('7f44db5c-eb13-4f78-8e92-33fc218bdfb5', 'test.host.org', 1);

select ehr.js_archetyped('openEHR-EHR-COMPOSITION.adverse_reaction_list.v1', 'IDCR - Adverse Reaction List.v1');

select ehr.js_party_ref('999999-345', '2.16.840.1.113883.2.1.4.3', 'NHS-UK', 'PARTY');

select ehr.js_party_identified('Dr Ian Shannon', NULL);

select ehr.js_party_identified('Home', ehr.party_ref('999999-345', '2.16.840.1.113883.2.1.4.3', 'NHS-UK', 'PARTY'));

select ehr.js_party_identified('Home', ehr.party_ref(NULL, NULL, NULL, NULL));

select ehr.js_dv_date_time('2017-05-18 09:15:06.000000', '+01:00');

select ehr.js_context_setting('3de12f78-1059-490c-b198-06c74f869c70');

select ehr.js_party('44c87386-d65a-4813-9f43-ec5377d9a3d1');

select ehr.js_party('dc36a7d4-5d73-40c4-847e-d974a08f1b2e');

select ehr.js_context('70700d41-14e5-495c-953a-14789b4873bf');

select ehr.js_composition('19560a2f-5d15-4398-a1a1-a813ea992650');