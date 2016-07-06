SELECT create_subject('toto', '12334', 'issuer', '', '');

SELECT delete_subject('12334', 'issuer');

-- SELECT party FROM ehr.identifier WHERE id_value='12334' AND issuer='issuer';