package engineering;

import jdk.internal.net.http.common.Pair;
import org.json.JSONObject;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.stream.Collectors;

public class TextExtractor implements FeatureExtractor {
    @Override
    public JSONObject extract(JSONObject data) {
        JSONObject texts = new JSONObject();
        if (!data.has("currentDate") || !data.has("dates") || !data.has("text")) {
            System.out.println("Input json don't have key: date or currentDate or text.");
        }
        MyDate currentDate = MyDate.parseDate(data.getString("currentDate"));
        Set<MyDate> dates = data.getJSONArray("dates").toList()
                .stream()
                .map(s -> MyDate.parseDate((String) s))
                .collect(Collectors.toSet());
        String text = data.getString("text");
        System.out.println(text);
        if (dates.size() > 0) {
            Multimap<String, Integer> mapIndexes = getDatesIndexes(text, dates, currentDate);
            System.out.println("mapIndexes: " + mapIndexes);
            getTextParts(text, mapIndexes, currentDate);
        }
        return null;
    }

    private Map<String, String> getTextParts(String text, Multimap<String, Integer> mapIndexes, MyDate currentDate) {
        Map<String, String> textParts = new HashMap<>();
        List<MyDate> dates = mapIndexes.keySet()
                .stream()
                .map(MyDate::parseDate)
                .sorted()
                .collect(Collectors.toList());
        if (dates.size() == 0) {
            return textParts;
        }
        List<String> selectedDates = selectDates(dates, currentDate);
        if (selectedDates.size() == 0) {
            return textParts;
        }
        return textParts;
    }

    private List<String> selectDates(List<MyDate> dates, MyDate currentDate) {
        List<MyDate> rightsDates = dates.stream()
                .filter(date -> date.compareTo(currentDate) <= 0)
                .collect(Collectors.toList());
        if (rightsDates.size() == 0) {
            return new ArrayList<>();
        }
        List<Integer> diffs = new ArrayList<>();
        for (int i = 1; i < rightsDates.size(); i++) {
            diffs.add(-rightsDates.get(i).getDist(rightsDates.get(i - 1)));
        }
        List<MyDate> selectedDates = new ArrayList<>();
        selectedDates.add(rightsDates.get(rightsDates.size() - 1));
        for (int i = diffs.size() - 1; i >= 0; i--) {
            if (diffs.get(i) > 1) {
                break;
            }
            selectedDates.add(rightsDates.get(i));
        }
        Collections.reverse(selectedDates);
        return selectedDates.stream()
                .map(MyDate::toString)
                .collect(Collectors.toList());
    }

    private Map<String, Pair<Integer, Integer>> getPairBounds(Multimap<String, Integer> mapIndexes,
                                                              List<String> selectedDates, int texLength) {
        Map<String, Pair<Integer, Integer>> pairBounds = new HashMap<>();
        for (int i = 0; i < selectedDates.size() - 1; i++) {
            List<Integer> temp = mapIndexes.get(selectedDates.get(i)).stream()
                    .sorted()
                    .collect(Collectors.toList());
            if (temp.size() == 1) {
                pairBounds.put(selectedDates.get(i), new Pair<>(temp.get(0), 1));
            }
        }
        return pairBounds;
    }

//    private Pair<Integer, Integer> evalBoudns(List<Integer> firstDateIndexes, List<Integer> secondDateIndexes,
//                                              int textLenght){
//        if(secondDateIndexes == null){
//
//        }
//    }

    private Multimap<String, Integer> getDatesIndexes(String text, Set<MyDate> dates, MyDate currentDate) {
        Multimap<String, Integer> mapIndexes = ArrayListMultimap.create();
        for (MyDate date : dates) {
            for (Integer index : getDateIndexes(text, date, currentDate)) {
                mapIndexes.put(date.toString(), index);
            }
        }
        return mapIndexes;
    }

    private Set<Integer> getDateIndexes(String text, MyDate date, MyDate currentDate) {
        int fromIndex;
        List<String> names = date.getNames(currentDate);
        Set<Integer> setIndexes = new HashSet<>();
        for (String name : names) {
            fromIndex = text.indexOf(name, 0);
            while (fromIndex < text.length()) {
                if (fromIndex < 0) {
                    break;
                }
                setIndexes.add(fromIndex);
                fromIndex = text.indexOf(name, fromIndex + 1);
            }
        }
        return setIndexes;
    }
}
