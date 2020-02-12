package engineering;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.json.JSONObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FishExtractor implements FeatureExtractor {

    private static Multimap<String, String> fishNamesVersion = ArrayListMultimap.create();

    static {
        fishNamesVersion.putAll("щука", Arrays.asList("щук", "шнур", "щуч"));
        fishNamesVersion.putAll("окунь", Arrays.asList("окун", "окуш", "полосат", "горбач", "горбат"));
    }

    @Override
    public JSONObject extract(JSONObject data) {
        if (!data.has("text")) {
            System.out.println("Input json don't have key: text.");
            return null;
        }
        String text = data.getString("text");
        System.out.println("text: " + text);
        System.out.println("fishs texts: " + collectFishsText(text, getFishsIndexes(text)));
        return null;
    }

    private Multimap<String, Integer> getFishsIndexes(String text) {
        Multimap<String, Integer> fishIndexes = ArrayListMultimap.create();
        for (String fish : fishNamesVersion.keySet()) {
            fishIndexes.putAll(fish, getFishIndexes(text, fish));
        }
        return fishIndexes;
    }

    private List<Integer> getFishIndexes(String text, String fish) {
        List<Integer> indexes = new ArrayList<>();
        for (String fishName : fishNamesVersion.get(fish)) {
            Matcher m = Pattern.compile("(?=(" + fishName + "))").matcher(text);
            while (m.find()) {
                indexes.add(m.start());
            }
        }
        return indexes;
    }

    private Map<String, String> collectFishsText(String text, Multimap<String, Integer> fishsIndexes) {
        Multimap<String, String> fishsTexts = ArrayListMultimap.create();
        Map<Integer, String> indexesNames = getIndexesNames(fishsIndexes);
        int startIndex = evaluateStartIndex(text, indexesNames.keySet()), punctuationListIndex = 0;
        String currentFish = indexesNames.get(indexesNames.keySet().stream().min(Integer::compare).get());
        List<Integer> punctuationIndexes = getPunctuationIndexes(text);
        List<Integer> indexes = new ArrayList<>(indexesNames.keySet());
        for(int i=1;i<indexes.size();i++){
            String fish = indexesNames.get(indexes.get(i));
            if (!fish.equals(currentFish)) {
                int index = i;
                punctuationListIndex = punctuationIndexes.subList(punctuationListIndex, punctuationIndexes.size())
                        .stream()
                        .filter(s->s < indexes.get(index))
                        .max(Integer::compare)
                        .get();
                int endIndex = punctuationListIndex < indexes.get(i - 1) ? index : punctuationListIndex;
                fishsTexts.put(currentFish, text.substring(startIndex, endIndex));
                startIndex = endIndex + 1;
                currentFish = fish;
            }
        }
        return sumMultimapTexts(fishsTexts);
    }

    private List<Integer> getPunctuationIndexes(String text) {
        Set<Integer> punctuationIndexes = new TreeSet<>();
        Matcher m = Pattern.compile("[.,:?!;]").matcher(text);
        while (m.find()) {
            punctuationIndexes.add(m.start());
        }
        return new ArrayList<>(punctuationIndexes);
    }

    private int evaluateStartIndex(String text, Set<Integer> indexesFishs) {
        int minIndexFishs = indexesFishs.stream().min(Integer::compare).get();
        Matcher m = Pattern.compile("[.,:?!;]").matcher(text);
        if (m.find()) {
            return Math.min(m.start(), minIndexFishs);
        } else {
            return minIndexFishs;
        }
    }

    private Map<String, String> sumMultimapTexts(Multimap<String, String> fishsTexts) {
        Map<String, String> sumMap = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (String fish : fishsTexts.keySet()) {
            for (String text : fishsTexts.get(fish)) {
                stringBuilder.append(text);
            }
            sumMap.put(fish, stringBuilder.toString());
            stringBuilder.setLength(0);
        }
        return sumMap;
    }

    private Map<Integer, String> getIndexesNames(Multimap<String, Integer> fishsIndexes) {
        Map<Integer, String> indexesNames = new TreeMap<>();
        for (String fish : fishsIndexes.keySet()) {
            for (Integer index : fishsIndexes.get(fish)) {
                indexesNames.put(index, fish);
            }
        }
        return indexesNames;
    }
}
