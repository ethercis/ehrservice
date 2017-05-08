-- create the view used to update RIPPLE Patient Summary Cache
-- NOT TO BE USED IN PRODUCTION
-- C.Chevalley May 2017
-- See LICENSE.txt for licensing details
-------------------------------------------------------------------------------------------------
CREATE OR REPLACE VIEW ehr.ripple_patient_summary AS
select 
	(
	select composition_id, count(id) from ehr.entry
	where entry.template_id IN (
	select tpl.template_id from ehr.template tpl
	  INNER JOIN ehr.template_heading_xref xref 
		ON xref.template_id = tpl.uid
	  INNER JOIN ehr.heading head 
		ON head.code = xref.heading_code
	  WHERE head.code = 'ORDERS'
	  )
	) AS orders_count,
	(
	select sys_transaction from ehr.entry 
	where entry.template_id IN (
	select tpl.template_id from ehr.template tpl
	  INNER JOIN ehr.template_heading_xref xref 
		ON xref.template_id = tpl.uid
	  INNER JOIN ehr.heading head 
		ON head.code = xref.heading_code
	  WHERE head.code = 'ORDERS'
	  )
	  ORDER BY sys_transaction DESC
	  LIMIT 1
	)AS orders_last,
	(
	select count(id) from ehr.entry 
	where entry.template_id IN (
	select tpl.template_id from ehr.template tpl
	  INNER JOIN ehr.template_heading_xref xref 
		ON xref.template_id = tpl.uid
	  INNER JOIN ehr.heading head 
		ON head.code = xref.heading_code
	  WHERE head.code = 'RESULTS'
	  )
	) AS results_count,
	(
	select sys_transaction from ehr.entry 
	where entry.template_id IN (
	select tpl.template_id from ehr.template tpl
	  INNER JOIN ehr.template_heading_xref xref 
		ON xref.template_id = tpl.uid
	  INNER JOIN ehr.heading head 
		ON head.code = xref.heading_code
	  WHERE head.code = 'RESULTS'
	  )
	  ORDER BY sys_transaction DESC
	  LIMIT 1
	)AS results_last,
		(
	select count(id) from ehr.entry 
	where entry.template_id IN (
	select tpl.template_id from ehr.template tpl
	  INNER JOIN ehr.template_heading_xref xref 
		ON xref.template_id = tpl.uid
	  INNER JOIN ehr.heading head 
		ON head.code = xref.heading_code
	  WHERE head.code = 'VITALS'
	  )
	) AS vitals_count,
	(
	select sys_transaction from ehr.entry 
	where entry.template_id IN (
	select tpl.template_id from ehr.template tpl
	  INNER JOIN ehr.template_heading_xref xref 
		ON xref.template_id = tpl.uid
	  INNER JOIN ehr.heading head 
		ON head.code = xref.heading_code
	  WHERE head.code = 'VITALS'
	  )
	  ORDER BY sys_transaction DESC
	  LIMIT 1
	)AS vitals_last,
	(
	select count(id) from ehr.entry 
	where entry.template_id IN (
	select tpl.template_id from ehr.template tpl
	  INNER JOIN ehr.template_heading_xref xref 
		ON xref.template_id = tpl.uid
	  INNER JOIN ehr.heading head 
		ON head.code = xref.heading_code
	  WHERE head.code = 'DIAGNOSES'
	  )
	) AS diagnoses_count,
	(
	select sys_transaction from ehr.entry 
	where entry.template_id IN (
	select tpl.template_id from ehr.template tpl
	  INNER JOIN ehr.template_heading_xref xref 
		ON xref.template_id = tpl.uid
	  INNER JOIN ehr.heading head 
		ON head.code = xref.heading_code
	  WHERE head.code = 'DIAGNOSES'
	  )
	  ORDER BY sys_transaction DESC
	  LIMIT 1
	)AS diagnoses_last;