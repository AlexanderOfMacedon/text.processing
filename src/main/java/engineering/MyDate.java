package engineering;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class MyDate implements Comparable<MyDate> {
    private final Date date;
    private int priority;
    private static Map<Integer, String> mapWeekDays = new HashMap<>();

    static {
        mapWeekDays.put(1, "понедельник");
        mapWeekDays.put(2, "вторник");
        mapWeekDays.put(3, "сред");
        mapWeekDays.put(4, "четверг");
        mapWeekDays.put(5, "пятниц");
        mapWeekDays.put(6, "суббот");
        mapWeekDays.put(7, "воскресен");
    }

    MyDate(Date date) {
        this.priority = 0;
        this.date = date;
    }

    MyDate(Date date, int priority){
        this.priority = priority;
        this.date = date;
    }

    Date getDate() {
        return date;
    }

    int getPriority(){
        return priority;
    }

    void setPriority(int priority){
        this.priority = priority;
    }

    public static MyDate parseDate(String dateFormat) {
        DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        try {
            return new MyDate(format.parse(dateFormat));
        } catch (ParseException e) {
            System.out.println(e);
            return null;
        }
    }

    int getWeekDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        return weekDay == 1 ? 7 : weekDay - 1;
    }

    int getMonth() {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.getMonthValue();
    }

    int getYear() {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.getYear();
    }

    public int getMonthDay() {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.getDayOfMonth();
    }

    public int getDist(MyDate myDate) {
        LocalDate d1 = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate d2 = myDate.getDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        ;
        Duration diff = Duration.between(d1.atStartOfDay(), d2.atStartOfDay());
        return (int) diff.toDays();
    }

    MyDate minusDays(int days) {
        LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).minusDays(days);
        return new MyDate(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
    }

    MyDate getNearlySunday() {
        int weekDay = getWeekDay();
        if (weekDay == 7) {
            return new MyDate(date);
        }
        MyDate ret = minusDays(weekDay);
        ret.setPriority(1);
        return ret;
    }

    MyDate getNearlySaturday() {
        int weekDay = getWeekDay();
        if (weekDay == 6) {
            return new MyDate(date);
        }
        MyDate ret = minusDays(weekDay + 1);
        ret.setPriority(1);
        return ret;
    }

    MyDate getYesterday() {
        return minusDays(1);
    }

    MyDate getdbYesterday() {
        return minusDays(2);
    }

    public List<String> getNames(MyDate nearlyDate) {
        List<String> names = new ArrayList<>();
        names.add(String.valueOf(getMonthDay()));
        names.add(mapWeekDays.get(getWeekDay()));
        if (compareTo(nearlyDate.getYesterday()) == 0) {
            names.add("вчера");
        }
        if (compareTo(nearlyDate.getdbYesterday()) == 0) {
            names.add("позавчера");
        }
        if (compareTo(nearlyDate) == 0) {
            names.add("сегодня");
        }
        return names;
    }

    @Override
    public String toString() {
        return String.format("%02d", getMonthDay()) + "." + String.format("%02d", getMonth()) + "."
                + String.format("%04d", getYear());
    }

    @Override
    public int compareTo(MyDate o) {
        return Comparator.comparing(MyDate::getYear)
                .thenComparing(MyDate::getMonth)
                .thenComparing(MyDate::getMonthDay)
                .compare(this, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyDate date1 = (MyDate) o;
        return Objects.equals(date, date1.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }
}
