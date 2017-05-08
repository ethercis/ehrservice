-- initialize the cache summary for all EHRs
-- C.Chevalley May 2017
-- See LICENSE.txt for licensing details
-------------------------------------------------------------------------------------------------
SELECT ehr.set_cache_for_ehr(ehr.id) FROM ehr.ehr;