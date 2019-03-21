## JOOQ Binding and XML INFORMATION_SCHEMA

### Generating XML INFORMATION_SCHEMA 

Some background is provided at https://www.jooq.org/doc/latest/manual/code-generation/xmlgenerator/

Please note that a live DB connection is required.

To generate the XML DDL file, use the following command:

```
mvn generate-sources -f pom-generatexml.xml
```

The corresponding xml representation is generated in

```
target/generated-sources/jooq/org/jooq/generated/information_schema.xml
```

NB. I couldn't find a way to specify the target directory/file.