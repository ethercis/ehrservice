-- PL/SQL to create tables Territory, Language and Concept fromBinder openEHR terminology
-- The terminology file is an XML file and should be located in PG_DATA directory to follow PG convention
-- the tables are re-populated each time.
-- NB: the terminology.xml file must be valid, in particular remove the namespace declaration at the beginning
-- as it causes parsing issues with PostgreSQL (and it is not consistent since it is not used afterward...)

-- Christian 3/6/14

CREATE OR REPLACE FUNCTION ehr.read_language_from_terminology()
  RETURNS boolean AS
$BODY$
DECLARE
      myxml XML;                               -- read xml file into that var.
      datafile varchar := 'terminology.xml'; 

BEGIN

myxml := pg_read_file(datafile, 0, 100000000);  -- arbitrary 100 MB max.
-- RAISE NOTICE 'xml file content %', myxml;
-- demonstrating 4 variants of how to fetch values for educational purposes --
-- DROP TABLE IF EXISTS EHR.XML_TEST ;
CREATE TEMP TABLE tmp ON COMMIT DROP AS
SELECT xpath('@code', x) AS id   -- id is unique  
      ,xpath('@Description', x) AS col1 -- one value
FROM   unnest(xpath('//Language', myxml)) x;

RETURN TRUE;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION ehr.read_concept_from_terminology()
  RETURNS boolean AS
$BODY$
DECLARE
      myxml XML;                               -- read xml file into that var.
      datafile varchar := 'terminology.xml'; 

BEGIN

myxml := pg_read_file(datafile, 0, 100000000);  -- arbitrary 100 MB max.
-- RAISE NOTICE 'xml file content %', myxml;
-- demonstrating 4 variants of how to fetch values for educational purposes --
-- DROP TABLE IF EXISTS EHR.XML_TEST ;
CREATE TEMP TABLE tmp2 ON COMMIT DROP AS
SELECT xpath('@ConceptID', x) AS id   -- id is unique  
      ,xpath('@Language', x) AS language
      ,xpath('@Rubric', x) AS rubric
FROM   unnest(xpath('//Concept', myxml)) x;

RETURN TRUE;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE OR REPLACE FUNCTION ehr.read_territory_from_terminology()
  RETURNS boolean AS
$BODY$
DECLARE
      myxml XML;                               -- read xml file into that var.
      datafile varchar := 'terminology.xml'; 

BEGIN

myxml := pg_read_file(datafile, 0, 100000000);  -- arbitrary 100 MB max.
-- RAISE NOTICE 'xml file content %', myxml;
-- demonstrating 4 variants of how to fetch values for educational purposes --
-- DROP TABLE IF EXISTS EHR.XML_TEST ;
CREATE TEMP TABLE tmp3 ON COMMIT DROP AS
SELECT xpath('@NumericCode', x) AS code   -- id is unique  
      ,xpath('@TwoLetter', x) AS two
      ,xpath('@ThreeLetter', x) AS three
      ,xpath('@Text', x) AS textual
FROM   unnest(xpath('//Territory', myxml)) x;

RETURN TRUE;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;


-- Territory
SELECT ehr.read_territory_from_terminology();
-- SELECT code[1], two[1], three[1], textual[1] FROM tmp3;
-- delete all rows in language to rebuild the table
DELETE FROM ehr.territory;
INSERT INTO ehr.territory (code, twoletter, threeletter, text) SELECT CAST(CAST(code[1] AS TEXT) AS INTEGER), two[1], three[1], textual[1] FROM tmp3;

-- Language
SELECT ehr.read_language_from_terminology();
-- SELECT id[1], col1[1] FROM tmp;
-- delete all rows in language to rebuild the table
DELETE FROM ehr.language;
INSERT INTO ehr.language (code, description) SELECT id[1], col1[1] FROM tmp;

-- Concept
SELECT ehr.read_concept_from_terminology();
-- SELECT CAST(CAST(id[1] AS TEXT) AS integer), language[1], rubric[1] FROM tmp2;
-- delete all rows in language to rebuild the table
DELETE FROM ehr.concept;
INSERT INTO ehr.concept (conceptID, language, description) SELECT CAST(CAST(id[1] AS TEXT) AS integer), language[1], rubric[1] FROM tmp2;




