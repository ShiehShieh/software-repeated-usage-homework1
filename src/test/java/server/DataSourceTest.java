package server;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by huangli on 3/23/16.
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
        System.out.println(dataSource.getPassword("shieh"));
        System.out.println(dataSource.getPassword("hi"));
        System.out.println(dataSource.getPassword("no"));
        System.out.println("sadf".equals(dataSource.getPassword("no")));
    }
}