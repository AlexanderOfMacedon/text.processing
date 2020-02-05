package engineering;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class MyDateTest {

    @Test
    public void test1() {
        MyDate date1 = MyDate.parseDate("11.01.2011");
        MyDate date2 = MyDate.parseDate("11.01.2011");
        date2.setPriority(2);
        Assert.assertEquals(date1,date2);

    }
}