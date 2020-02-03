package engineering;

import org.json.JSONObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DatesExtractor implements FeatureExtractor {
    private static final Map<Integer, String> mapMonth = new HashMap<>();
    private static final List<String> weekDaysStrings;

    static {
        mapMonth.put(1, "янв");
        mapMonth.put(2, "февр");
        mapMonth.put(3, "март");
        mapMonth.put(4, "апрел");
        mapMonth.put(5, "май");
        mapMonth.put(6, "июн");
        mapMonth.put(7, "июл");
        mapMonth.put(8, "август");
        mapMonth.put(9, "сентябр");
        mapMonth.put(10, "октябр");
        mapMonth.put(11, "ноябр");
        mapMonth.put(12, "декабр");
        weekDaysStrings = Arrays.asList("понедельник", "вторник", "сред", "четверг",
                "пятниц", "суббот", "воскресен");
    }

    @Override
    public JSONObject extract(JSONObject data) {
        JSONObject jsonObject = new JSONObject();
        if (!data.has("currentDate")) {
            System.out.println("Input json don't have key: date.");
            return null;
        }
        MyDate currentDate = MyDate.parseDate(data.getString("currentDate"));
        jsonObject.put("dates", getDates(data.getString("text"), currentDate));
        return jsonObject;
    }


    private Set<String> getDates(String text, MyDate date) {
        Set<MyDate> dates = new HashSet<>();
        dates.addAll(findDatesInFirst(text, date));
        dates.addAll(findDatesWithBeWord(text, date));
        return filterDates(dates);
    }

    Set<String> filterDates (Set<MyDate> dates){
        if(dates.size() > 2){
            return new HashSet<>();
        }
        if(dates.size() == 2){
            List<MyDate> dateList = new ArrayList<>(dates);
            if(Math.abs(dateList.get(0).getDist(dateList.get(1))) > 1){
                if(dateList.get(0).getPriority() == dateList.get(1).getPriority()){
                    return new HashSet<>();
                }
                else{
                    return Collections.singleton(dates.stream().
                            max((d1,d2)->Integer.compare(d1.getPriority(), d2.getPriority()))
                            .map(MyDate::toString).get());

                }
            }
        }
        return dates.stream().map(MyDate::toString).collect(Collectors.toSet());
    }

    private Set<MyDate> findDatesWithBeWord(String text, MyDate currentDate){
        Set<MyDate> dates = new HashSet<>();
        int indexBe = text.indexOf("был");
        if(indexBe < 0){
            return dates;
        }
        String subText = text.substring(indexBe, indexBe + Math.min(text.length() - indexBe, 30));
        dates.addAll(findByNumbers(subText, currentDate));
        dates.addAll(findByMonth(subText, currentDate));
        dates.addAll(findByDays(subText, currentDate));
        dates.addAll(findByDates(subText, currentDate));
        return dates;

    }

    private Set<MyDate> findDatesInFirst(String text, MyDate currentDate){
        Set<MyDate> dates = new HashSet<>();
        String subText = text.substring(0, Math.min(text.length() / 4, 100));
        dates.addAll(findByMonth(subText, currentDate));
        dates.addAll(findByDays(subText, currentDate));
        dates.addAll(findByDates(subText, currentDate));
        return dates;
    }

    private Set<MyDate> findByMonth(String text, MyDate date) {
        int currentMonth = date.getMonth();
        int earlierMonth = currentMonth > 1 ? currentMonth - 1 : 12;
        Set<MyDate> dates = new HashSet<>();
        JSONObject jsonObject;
        int fromIndex = 0;
        do {
            fromIndex = text.indexOf(mapMonth.get(currentMonth), fromIndex + 1);
            if (fromIndex < 0) {
                break;
            }
            dates.addAll(findDateByMonth(text.substring(Math.max(fromIndex - 15, 0),
                    Math.min(fromIndex + 15, text.length() - 1)),
                    currentMonth, date.getYear()));

        } while (fromIndex < text.length());
        fromIndex = 0;
        do {
            fromIndex = text.indexOf(mapMonth.get(earlierMonth), fromIndex + 1);
            if (fromIndex < 0) {
                break;
            }
            dates.addAll(findDateByMonth(text.substring(Math.max(fromIndex - 15, 0),
                    Math.min(fromIndex + 15, text.length() - 1)),
                    earlierMonth, date.getYear()));

        } while (fromIndex < text.length());

        return dates;
    }

    private Set<MyDate> findDateByMonth(String text, int currentMonth, int currentYear) {
        Set<MyDate> dates = new HashSet<>();
        String digitString = text.replaceAll("[^0-9]+", " ").trim();
        if (digitString.length() == 0) {
            return dates;
        }
        List<Integer> digits = Arrays.stream(digitString.trim().split(" "))
                .map(Integer::valueOf)
                .collect(Collectors.toList());
        for (Integer digit : digits) {
            try {
                if (digit < 32 && digit > 0) {
                    MyDate date = new MyDate(new GregorianCalendar(currentYear,
                            currentMonth - 1, digit).getTime());
                    date.setPriority(1);
                    dates.add(date);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e);
            }
        }
        return dates;
    }

    private Set<MyDate> findByDays(String text, MyDate currentDate) {
        Set<MyDate> dates = new HashSet<>();
        if (text.contains("сегодня")) {
            dates.add(currentDate);
        }
        if (text.contains("вчера")) {
            dates.add(currentDate.getYesterday());
        }
        if (text.contains("позавчера")) {
            dates.add(currentDate.getdbYesterday());
        }
        if (text.contains("выходн")) {
            dates.add(currentDate.getNearlySaturday());
            dates.add(currentDate.getNearlySunday());
        }
        for(String weekDay : weekDaysStrings){
            WeekDaysEnum weekDaysEnum = WeekDaysEnum.fromString(weekDay);
            if(weekDaysEnum == null){
                break;
            }
            if(text.contains(weekDay)){
                dates.add(weekDaysEnum.getNearlyDate(currentDate));
            }
        }
        return dates;
    }

    private Set<MyDate> findByDates(String text, MyDate currentDate){
        Matcher matcher = Pattern.compile("(0[1-9]|[12][0-9]|3[01])[\\/.](0[1-9]|1[012])").matcher(text);
        Set<MyDate> dates = new HashSet<>();
        String tempDate;
        while (matcher.find()){
            tempDate = matcher.group();
            MyDate date = MyDate.parseDate(tempDate + "." + String.valueOf(currentDate.getYear()));
            if(date.compareTo(currentDate) > 0){
                if(date.getMonth() > 10 && currentDate.getMonth() < 3){
                    date = MyDate.parseDate(tempDate + "." + String.valueOf(currentDate.getYear() - 1));
                    date.setPriority(1);
                    dates.add(date);
                }
            } else {
                date.setPriority(1);
                dates.add(date);
            }
        }
        return dates;
    }

    private Set<MyDate> findByNumbers(String text, MyDate currentDate) {
        Matcher matcher = Pattern.compile("(0[1-9]|[12][0-9]|3[01])").matcher(text);
        Set<MyDate> dates = new HashSet<>();
        String tempDate;
        while (matcher.find()) {
            tempDate = matcher.group();
            MyDate date = MyDate.parseDate(tempDate + "." + String.valueOf(currentDate.getMonth())
                    + "." + String.valueOf(currentDate.getYear()));
            if(date.compareTo(currentDate) > 0){
                if(date.getMonth() == 1){
                    date = MyDate.parseDate(tempDate + ".12." + String.valueOf(currentDate.getYear()));
                } else {
                    date = MyDate.parseDate(tempDate + "." + String.valueOf(currentDate.getMonth() - 1)
                            + "." + String.valueOf(currentDate.getYear()));
                }
            }
            System.out.println("DATAAAA: " + date);
            date.setPriority(1);
            dates.add(date);
        }
        return dates;
    }
}
