-- purge the db from all Patient Summary Cache compositions
-- C.Chevalley May 2017
-- See LICENSE.txt for licensing details
-------------------------------------------------------------------------------------------------
DELETE FROM ehr.composition USING ehr.entry
WHERE composition.id = entry.composition_id
AND entry.template_id = 'Ripple Dashboard Cache.v1'