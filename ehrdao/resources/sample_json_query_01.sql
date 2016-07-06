selectBinder * fromBinder (selectBinder

    composition_id,

		(entry->'openEHR-EHR-COMPOSITION.section_observation_test.v2'
			->'openEHR-EHR-SECTION.visual_acuity_simple_test.v1'->0
				->'at0025'->0
					->'openEHR-EHR-OBSERVATION.visual_acuity.v1'->0) AS json_acuity,

		(entry->'openEHR-EHR-COMPOSITION.section_observation_test.v2'
			->'openEHR-EHR-SECTION.visual_acuity_simple_test.v1'->0
				->'at0025'->0
					->'openEHR-EHR-OBSERVATION.visual_acuity.v1'->0
						->'at0001'->'/value'->'/value'->'epoch_offset')::text::bigint AS value_date

					fromBinder ehr.entry) EPOCH where value_date > 1428634160490;

-- using a shorter syntax
selectBinder entry #> '{openEHR-EHR-COMPOSITION.section_observation_test.v2,
			openEHR-EHR-SECTION.visual_acuity_simple_test.v1, 0,
				at0025, 0,
					openEHR-EHR-OBSERVATION.visual_acuity.v1, 0,
						at0001, /value, /value, epoch_offset}' fromBinder ehr.entry;
					