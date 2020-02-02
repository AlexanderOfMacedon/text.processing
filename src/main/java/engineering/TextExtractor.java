package engineering;

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
        Map<String, Pair<Integer, Integer>> dateBounds = getPairBounds(mapIndexes, selectedDates, text.length());
        System.out.println("dateBounds: " + dateBounds);
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
        int endBound = mapIndexes.get(selectedDates.get(0)).stream()
                .min(Integer::compare).get();
        for (int i = 0; i < selectedDates.size() - 1; i++) {
            Pair<Integer, Integer> bounds = evalBoudns(mapIndexes.get(selectedDates.get(i)).stream()
                    .sorted()
                    .collect(Collectors.toList()), mapIndexes.get(selectedDates.get(i + 1)).stream()
                    .sorted()
                    .collect(Collectors.toList()), texLength);
            pairBounds.put(selectedDates.get(i), bounds);
            endBound = bounds.second;
        }
        pairBounds.put(selectedDates.get(selectedDates.size() - 1), new Pair<>(endBound, texLength));
        return pairBounds;
    }

    private Pair<Integer, Integer> evalBoudns(List<Integer> firstDateIndexes, List<Integer> secondDateIndexes,
                                              int textLenght){
        if(secondDateIndexes == null){
            return new Pair<>(firstDateIndexes.get(0), textLenght);
        }
        if(firstDateIndexes.size() == 1){
            return new Pair<>(firstDateIndexes.get(0), secondDateIndexes.get(0));
        }
        if(secondDateIndexes.get(0) - firstDateIndexes.get(0) < 24){
            if(secondDateIndexes.size() == 1){
                return new Pair<>(firstDateIndexes.get(0), secondDateIndexes.get(0));
            } else {
                return new Pair<>(firstDateIndexes.get(1), secondDateIndexes.get(1));
            }
        }
        return new Pair<>(firstDateIndexes.get(0), secondDateIndexes.get(0));
    }

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
