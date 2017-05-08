-- dump the heading cross references
-- C.Chevalley May 2017
-- See LICENSE.txt for licensing details
-------------------------------------------------------------------------------------------------
select heading_code, templ.template_id from
  ehr.template_heading_xref
  INNER JOIN ehr.template templ
    ON templ.uid = template_heading_xref.template_id