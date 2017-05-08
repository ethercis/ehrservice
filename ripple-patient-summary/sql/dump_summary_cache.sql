-- dump the current cache summary compositions for all defined EHRs
-- C.Chevalley May 2017
-- See LICENSE.txt for licensing details
-------------------------------------------------------------------------------------------------
select
  DISTINCT "ehr_join"."id" as "ehrId",
           ("ehr"."event_context"."other_context"->('/context/other_context[at0001]') #>> '{/context/other_context[at0001]/items[at0005],0,/value,magnitude}')::float as "diagnosesCount",
           ("ehr"."event_context"."other_context"->('/context/other_context[at0001]') #>> '{/items[at0002],0,/value,magnitude}')::float as "ordersCount",
           "ehr"."event_context"."other_context"->('/context/other_context[at0001]') #>> '{/items[at0006],0,/value,value}' as "ordersDate",
           ("ehr"."event_context"."other_context"->('/context/other_context[at0001]') #>> '{/items[at0004],0,/value,magnitude}')::float as "resultsCount",
           "ehr"."event_context"."other_context"->('/context/other_context[at0001]') #>> '{/items[at0009],0,/value,value}' as "resultsDate",
           ("ehr"."event_context"."other_context"->('/context/other_context[at0001]') #>> '{/items[at0003],0,/value,magnitude}')::float as "vitalsCount",
           "ehr"."event_context"."other_context"->('/context/other_context[at0001]') #>> '{/items[at0007],0,/value,value}' as "vitalsDate"
from "ehr"."entry"
  join "ehr"."event_context"
    on "ehr"."event_context"."composition_id" = "ehr"."entry"."composition_id"
  join "ehr"."composition" as "composition_join"
    on "composition_join"."id" = "ehr"."entry"."composition_id"
  join "ehr"."ehr" as "ehr_join"
    on "ehr_join"."id" = "composition_join"."ehr_id"
where "composition_join"."id" in ((
  select "alias_11019187"."comp_id"
  from (
         select distinct on ("ehr"."containment"."comp_id") "ehr"."containment"."comp_id"
         from "ehr"."containment"
         where (label ~ 'openEHR_EHR_COMPOSITION_ripple_cache_v1')
       ) as "alias_11019187"
))