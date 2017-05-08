DROP TABLE IF EXISTS operational_template;
CREATE TABLE operational_template(filename text);
COPY operational_template FROM PROGRAM 'dir /B \Development\Dropbox\eCIS_Development\knowledge\production\operational_templates';
SELECT * FROM operational_template ORDER BY filename ASC;