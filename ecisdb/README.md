### EtherCIS DB Migration

This module implements the creation and migration of DB ethercis using *exclusively* Maven.

In the following, we assume PostgreSQL 10+ is installed and running. Further, the following extensions should be installed:

[temporal_tables](temporal_tables)

[jsquery](https://github.com/postgrespro/jsquery)

NB. depending on your environment, you may have to install more dependencies, in particular `pgxn` client (https://github.com/pgxn/pgxnclient)

### Initial Creation of DB ethercis

This uses the script located in 

```
createdb.sql
```

The script should be invoked using `psql` , for example:

```
$ sudo psql -U postgres -h 127.0.0.1 < .../ehrservice/db/createdb.sql
```

NB. replace the `...` by your actual path to the script

### Using the Migration

The migration should be invoked in 2 steps:

1. Compile the java class implementing V3__ migration:

   `mvn compile`

2. Launch the actual migrations:

   `mvn flyway:migrate`

   

