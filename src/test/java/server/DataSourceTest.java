package server;

import DataSource.DataSource;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by shieh on 3/23/16.
 */
public class DataSourceTest {

    DataSource dataSource;

    @Before
    public void setUp() throws Exception {
        String dbuser = "root";
        String dbpw = "510894";
        dataSource = new DataSource(dbuser, dbpw);
    }

    @Test
    public void getPassword() throws Exception {
        System.out.println(dataSource.getPasswordDB("shieh"));
        System.out.println(dataSource.getPasswordDB("hi"));
        System.out.println(dataSource.getPasswordDB("no"));
        System.out.println("sadf".equals(dataSource.getPasswordDB("no")));
    }
}