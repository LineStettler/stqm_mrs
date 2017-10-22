package ch.fhnw.swc.mrs.model;

import ch.fhnw.swc.mrs.data.DbMRSServices;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Line on 15.10.17.
 */
public class ITBill {

    private Movie m1;
    private Movie m2;
    private User u1;
    private User u2;

    public void setup(){
        m1 = new Movie();
        m1.setTitle("Avatar");
        m1.setAgeRating(3);
        m1.setPriceCategory(RegularPriceCategory.getInstance());

        m2 = new Movie();
        m2.setTitle("Casablanca");
        m2.setAgeRating(3);
        m2.setPriceCategory(RegularPriceCategory.getInstance());

        u1 = new User("Hunziker", "Hans", LocalDate.of(1960,2,12));
        u2 = new User("Meier", "Franz", LocalDate.of(1969,12,12));
    }

    @Test
    public void testRegularUsernames() {
        DbMRSServices services = new DbMRSServices();
        services.init();
        services.createRental(u1, m1);
        services.createRental(u1, m2);
    }

    @Test
    public void testLonguserNames(){
        DbMRSServices services = new DbMRSServices();
        services.init();
        services.createRental(u1,m1);
        services.createRental(u1,m2);
    }

}
