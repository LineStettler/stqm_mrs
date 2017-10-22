package ch.fhnw.swc.mrs.data;

import ch.fhnw.swc.mrs.model.Movie;
import ch.fhnw.swc.mrs.model.PriceCategory;
import ch.fhnw.swc.mrs.model.RegularPriceCategory;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.NoSuchColumnException;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.xml.sax.InputSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Line on 16.10.17.
 */
public class ITMovieDao {
    /** Class under test: UserDAO. */
    private MovieDAO dao;
    private IDatabaseTester tester;
    private Connection connection;

    private static final String COUNT_SQL = "SELECT COUNT(*) FROM movies";
    private static final String DB_CONNECTION = "jdbc:hsqldb:mem:mrs";

    /** Create a new Integration Test object. */
    public ITMovieDao() {
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, HsqlDatabase.DB_DRIVER);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, DB_CONNECTION);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, "sa");
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, "");
    }

    @Override
    protected void setUpDatabaseConfig(DatabaseConfig config) {
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());
    }

    @Override
    protected IDataSet getDataSet() throws Exception {
        InputStream stream = this.getClass().getResourceAsStream("MovieDaoTestData.xml");
        return new FlatXmlDataSetBuilder().build(new InputSource(stream));
    }

    static {
        try {
            new HsqlDatabase().initDB(DB_CONNECTION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize a DBUnit DatabaseTester object to use in tests.
     *
     * @throws Exception
     *             whenever something goes wrong.
     */
    public void setUp() throws Exception {
        super.setUp();
        PriceCategory.init();
        tester = new JdbcDatabaseTester(HsqlDatabase.DB_DRIVER, DB_CONNECTION);
        connection = getConnection().getConnection();
        dao = new SQLMovieDAO(connection);
    }

    public void tearDown() throws Exception {
        connection.close();
        tester.onTearDown();
    }

    public void testDeleteNonexisting() throws Exception {
        // count no. of rows before deletion
        Statement s = connection.createStatement();
        ResultSet r = s.executeQuery(COUNT_SQL);
        r.next();
        int rows = r.getInt(1);
        assertEquals(3, rows);

        // Delete non-existing record
        Movie movie = new Movie("Casablanca", LocalDate.of(2017,3,10), RegularPriceCategory.getInstance(), 6 );
        movie.setId(42);
        dao.delete(movie);

        r = s.executeQuery(COUNT_SQL);
        r.next();
        rows = r.getInt(1);
        assertEquals(2, rows);
    }


    public void testDelete() throws Exception {
        // count no. of rows before deletion
        Statement s = connection.createStatement();
        ResultSet r = s.executeQuery(COUNT_SQL);
        r.next();
        int rows = r.getInt(1);
        assertEquals(3, rows);

        // delete existing record
        Movie movie = new Movie("Avatar", LocalDate.of(2017,4,11), RegularPriceCategory.getInstance(), 6 );
        movie.setId(13);
        dao.delete(movie);

        // Fetch database data after deletion
        IDataSet databaseDataSet = tester.getConnection().createDataSet();
        ITable actualTable = databaseDataSet.getTable("MOVIES");

        InputStream stream = this.getClass().getResourceAsStream("MovieDaoTestResult.xml");
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(stream);
        ITable expectedTable = expectedDataSet.getTable("MOVIES");

        Assertion.assertEquals(expectedTable, actualTable);
    }

    public void testGetAll() throws DatabaseUnitException, SQLException, Exception {
        List<Movie> movieList = dao.getAll();
        ITable actualTable = convertToTable(movieList);

        InputStream stream = this.getClass().getResourceAsStream("MovieDaoTestData.xml");
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(stream);
        ITable expectedTable = expectedDataSet.getTable("MOVIES");

        Assertion.assertEquals(expectedTable, actualTable);
    }

    public void testGetAllSingleRow() throws Exception {
        InputStream stream = this.getClass().getResourceAsStream("MovieDaoSingleRowTest.xml");
        IDataSet dataSet = new FlatXmlDataSetBuilder().build(stream);
        DatabaseOperation.CLEAN_INSERT.execute(tester.getConnection(), dataSet);

        List<Movie> movielist = dao.getAll();
        assertEquals(1, movielist.size());
        assertEquals("Avatar", movielist.get(0).getTitle());
    }

    public void testGetAllEmptyTable() throws Exception {
        InputStream stream = this.getClass().getResourceAsStream("UserDaoEmpty.xml");
        IDataSet dataSet = new XmlDataSet(stream);
        DatabaseOperation.CLEAN_INSERT.execute(tester.getConnection(), dataSet);

        List<Movie> movielist = dao.getAll();
        assertNotNull(movielist);
        assertEquals(0, movielist.size());
    }

    public void testGetById() throws SQLException {
        Movie movie = dao.getById(42);
        assertEquals("Avatar", movie.getTitle());
        assertEquals(42, movie.getId());
    }

    public void testGetByTitle() throws SQLException {
        List<Movie> movielist = dao.getByTitle("Avatar");
        assertEquals(2, movielist.size());
    }

    public void testUpdate() throws SQLException {
        // count no. of rows before operation
        Statement s = connection.createStatement();
        ResultSet r = s.executeQuery(COUNT_SQL);
        r.next();
        int rows0 = r.getInt(1);

        Movie daisy = dao.getById(13);
        daisy.setTitle("Daisy");
        dao.saveOrUpdate(daisy);
        Movie actual = dao.getById(13);
        assertEquals(daisy.getTitle(), actual.getTitle());

        r = s.executeQuery(COUNT_SQL);
        r.next();
        int rows1 = r.getInt(1);
        assertEquals(rows0, rows1);
    }

    public void testSave() throws Exception {
        // count no. of rows before operation
        Statement s = connection.createStatement();
        ResultSet r = s.executeQuery(COUNT_SQL);
        r.next();
        int rows1 = r.getInt(1);

        Movie goofy = new Movie("Lost", LocalDate.of(2016,5,11), RegularPriceCategory.getInstance(), 6 );
        dao.saveOrUpdate(goofy);
        Movie actual = dao.getById(goofy.getId());
        assertEquals(goofy.getTitle(), actual.getTitle());

        r = s.executeQuery(COUNT_SQL);
        r.next();
        int rows2 = r.getInt(1);
        assertEquals(rows1 + 1, rows2);
    }

    @SuppressWarnings("deprecation")
    private ITable convertToTable(List<Movie> movielist) throws Exception {
        ITableMetaData meta = new TableMetaData();
        DefaultTable t = new DefaultTable(meta);
        int row = 0;
        for (Movie m : movielist) {
            t.addRow();
            LocalDate d = m.getReleaseDate();
            t.setValue(row, "id", m.getId());
            t.setValue(row, "title", m.getTitle());
            t.setValue(row, "isrented", m.isRented());
            t.setValue(row, "pricecategorie", m.getPriceCategory());
            t.setValue(row, "releasedate", d);
            t.setValue(row, "agerating", m.getAgeRating());
            row++;
        }
        return t;
    }

    private static final class TableMetaData implements ITableMetaData {

        private List<Column> cols = new ArrayList<>();

        TableMetaData() {
            cols.add(new Column("id", DataType.INTEGER));
            cols.add(new Column("title", DataType.VARCHAR));
            cols.add(new Column("isrented", DataType.BOOLEAN));
            cols.add(new Column("releasedate", DataType.DATE));
            cols.add(new Column("pricecategory", DataType.XXX));
            cols.add(new Column("agerating", DataType.INTEGER));
        }

        @Override
        public int getColumnIndex(String colname) throws DataSetException {
            int index = 0;
            for (Column c : cols) {
                if (c.getColumnName().equals(colname.toLowerCase())) {
                    return index;
                }
                index++;
            }
            throw new NoSuchColumnException(getTableName(), colname);
        }

        @Override
        public Column[] getColumns() throws DataSetException {
            return cols.toArray(new Column[4]);
        }

        @Override
        public Column[] getPrimaryKeys() throws DataSetException {
            Column[] cols = new Column[1];
            cols[0] = new Column("id", DataType.INTEGER);
            return cols;
        }

        @Override
        public String getTableName() {
            return "clients";
        }
    }
}
