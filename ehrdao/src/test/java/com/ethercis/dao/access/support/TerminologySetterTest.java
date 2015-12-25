package com.ethercis.dao.access.support;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.jooq.SQLDialect;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * test the Terminology setter
 *
 * PRREQUISITES:
 * A valid DB running under PostgreSQL 9.x
 * A valid terminology file
 */
public class TerminologySetterTest extends TestCase {

    public void testPopulateTerminologyTables() {
        TerminologySetter setter = new TerminologySetter("jdbc:postgresql://localhost:5434/ethercis",
                                                            "postgres", "postgres",
                                                            SQLDialect.POSTGRES,
                                                            "/Development/eCIS/ehrservice/ehrdao/resources/terminology.xml");
        Connection connection = null;
        //setup connection
        try {
            connection = setter.connectDB();

        }
        catch (SQLException e){
            Assert.fail(e.getMessage());
        }

        if (connection == null)
            Assert.fail("cannot connect to db...");

        try {
            setter.createTerminologyTables(connection);
        }
        catch (FileNotFoundException e){
            Assert.fail(e.getMessage());
        }
    }
}