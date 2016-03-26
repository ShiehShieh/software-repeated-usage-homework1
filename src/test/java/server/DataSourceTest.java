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
        dataSource = new DataSource();
    }

    @Test
    public void getPassword() throws Exception {
        System.out.println(dataSource.getPassword("shieh"));
    }
}