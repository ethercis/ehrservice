-- modify table template to use md5 (standard in PG10) instead of CRC checksum
ALTER TABLE ehr.template ADD COLUMN md5  BYTEA;
ALTER TABLE ehr.template DROP COLUMN crc;