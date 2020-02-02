package engineering;

import org.json.JSONObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DatesExtractor implements FeatureExtractor {
    private static final Map<Integer, String> mapMonth = new HashMap<>();
    private static final List<String> weekDaysStrings = Arrays.asList("понедельник", "вторник", "сред", "четверг",
            "пятниц", "суббот", "воскресен");

    public DatesExtractor() {
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
        Set<String> dates = new HashSet<>();
        dates.addAll(findByMonth(text, date));
        dates.addAll(findByDays(text, date));
        dates.addAll(findByDates(text, date));
        return dates;
    }

    private Set<String> findByMonth(String text, MyDate date) {
        int currentMonth = date.getMonth();
        int earlierMonth = currentMonth > 1 ? currentMonth - 1 : 12;
        Set<String> dates = new HashSet<>();
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

    private Set<String> findDateByMonth(String text, int currentMonth, int currentYear) {
        Set<String> dates = new HashSet<>();
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
                    dates.add(date.toString());
                }
            } catch (Exception e) {
                System.out.println("Error: " + e);
            }
        }
        return dates;
    }

    private Set<String> findByDays(String text, MyDate currentDate) {
        Set<String> dates = new HashSet<>();
        if (text.contains("сегодня")) {
            dates.add(currentDate.toString());
        }
        if (text.contains("вчера")) {
            dates.add(currentDate.getYesterday().toString());
        }
        if (text.contains("позавчера")) {
            dates.add(currentDate.getdbYesterday().toString());
        }
        if (text.contains("выходн")) {
            dates.add(currentDate.getSaturday().toString());
            dates.add(currentDate.getSunday().toString());
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

    private Set<String> findByDates(String text, MyDate currentDate){
        Matcher matcher = Pattern.compile("(0[1-9]|[12][0-9]|3[01])[\\/.](0[1-9]|1[012])").matcher(text);
        Set<String> dates = new HashSet<>();
        String tempDate;
        while (matcher.find()){
            tempDate = matcher.group();
            MyDate date = MyDate.parseDate(tempDate + "." + String.valueOf(currentDate.getYear()));
            if(date.compareTo(currentDate) > 0){
                if(date.getMonth() > 10 && currentDate.getMonth() < 3){
                    dates.add(tempDate + "." + String.valueOf(currentDate.getYear() - 1));
                }
            } else {
                dates.add(date.toString());
            }
        }
        return dates;
    }

}
