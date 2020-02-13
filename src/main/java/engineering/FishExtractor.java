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
        if(fishsIndexes.keySet().size() == 0) {
            return new HashMap<>();
        }
        Multimap<String, String> fishsTexts = ArrayListMultimap.create();
        Map<Integer, String> indexesNames = getIndexesNames(fishsIndexes);
        int startIndex = 0, punctuationListIndex = 0, index = 0;
        String currentFish = indexesNames.get(indexesNames.keySet().stream().min(Integer::compare).get());
        List<Integer> punctuationIndexes = getPunctuationIndexes(text);
        List<Integer> indexes = new ArrayList<>(indexesNames.keySet());
        for(int i=1;i<indexes.size();i++){
            index = indexes.get(i);
            String fish = indexesNames.get(index);
            if (!fish.equals(currentFish)) {
                punctuationListIndex = evaluateDelimIndex(punctuationIndexes, indexes.get(i - 1), index);
                if(punctuationListIndex > indexes.get(i - 1)){
                    fishsTexts.put(currentFish, text.substring(startIndex, punctuationListIndex));
                    startIndex = punctuationListIndex + 1;
                    currentFish = fish;
                }
            }
        }
        fishsTexts.put(currentFish, text.substring(startIndex, text.length() - 1));
        return sumMultimapTexts(fishsTexts);
    }



    private int evaluateDelimIndex(List<Integer> puncIndexes, int leftIndex, int rightIndex){
        int boardIndex = evaluateBoardIndex(puncIndexes, rightIndex);
        if(boardIndex == 0){
            return puncIndexes.get(boardIndex);
        }
        while (rightIndex - puncIndexes.get(boardIndex) < 16 && puncIndexes.get(boardIndex - 1) > leftIndex){
            boardIndex--;
        }
        return puncIndexes.get(boardIndex);
    }

    private int evaluateBoardIndex(List<Integer> indexes, int board){
        for(int i=1;i<indexes.size();i++){
            if(indexes.get(i) > board){
                return i-1;
            }
        }
        return indexes.size() - 1;
    }

    private List<Integer> getPunctuationIndexes(String text) {
        Set<Integer> punctuationIndexes = new TreeSet<>();
        Matcher m = Pattern.compile("[.,:?!;]").matcher(text);
        while (m.find()) {
            punctuationIndexes.add(m.start());
        }
        return new ArrayList<>(punctuationIndexes);
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
