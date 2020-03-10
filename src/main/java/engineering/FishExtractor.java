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
    private static List<String> predatorsFish = new ArrayList<>(
            List.of("щука",
                    "окунь",
                    "судак",
                    "сом",
                    "жерех"));
    private static List<String> peaceFish = new ArrayList<>(
            List.of("лещ",
                    "карп",
                    "карась",
                    "плотва",
                    "толстолобик",
                    "язь",
                    "голавль",
                    "густера",
                    "линь",
                    "красноперка"));

    static {
        fishNamesVersion.putAll("щука", Arrays.asList("щук", "шнур", "щуч"));
        fishNamesVersion.putAll("окунь", Arrays.asList("окун", "окуш", "полосат", "горбач", "горбат"));
        fishNamesVersion.putAll("лещ", Arrays.asList("лещ"));
        fishNamesVersion.putAll("судак", Arrays.asList("судак", "клыкаст", "берш"));
        fishNamesVersion.putAll("сом", Arrays.asList("сомы", "сома", "сомов", "сомик", "сом"));
        fishNamesVersion.putAll("карп", Arrays.asList("карп", "сазан", "зеркальн"));
        fishNamesVersion.putAll("карась", Arrays.asList("карас"));
        fishNamesVersion.putAll("плотва", Arrays.asList("плотв"));
        fishNamesVersion.putAll("толстолобик", Arrays.asList("толстолоб", "лобаст"));
        fishNamesVersion.putAll("жерех", Arrays.asList("жерех", "жереш"));
        fishNamesVersion.putAll("язь", Arrays.asList("язь", "язи", "язя", "язей"));
        fishNamesVersion.putAll("голавль", Arrays.asList("голавл"));
        fishNamesVersion.putAll("густера", Arrays.asList("густер"));
        fishNamesVersion.putAll("линь", Arrays.asList("линь", "линя", "линей"));
        fishNamesVersion.putAll("красноперка", Arrays.asList("красноперк"));
        fishNamesVersion.putAll("налим", Arrays.asList("налим"));
        fishNamesVersion.putAll("форель", Arrays.asList("форел"));
    }

    @Override
    public JSONObject extract(JSONObject data) {
        if (!data.has("text")) {
            System.out.println("Input json don't have key: text.");
            return null;
        }
        String text = data.getString("text");
//        System.out.println("text: " + text);
        Map<String, String> fishTexts = collectFishsText(text, getFishsIndexes(text));
        Multimap<String, Integer> multimap= ArrayListMultimap.create();
        multimap.putAll("хищник", getPredatorsIndexes(text));
        multimap.putAll("мирная", getPeaceFishIndexes(text));
        fishTexts.putAll(collectFishsText(text, multimap));
//        System.out.println("fishs texts: " + fishTexts);
        return new JSONObject(fishTexts);
    }

    private Multimap<String, Integer> getFishsIndexes(String text) {
        Multimap<String, Integer> fishIndexes = ArrayListMultimap.create();
        for (String fish : fishNamesVersion.keySet()) {
            fishIndexes.putAll(fish, getFishIndexes(text, fish));
        }
        return fishIndexes;
    }

    private List<Integer> getIndexesByName(String text, String name, String prefix, String suffix) {
        Matcher matcher = Pattern.compile(prefix + name + suffix).matcher(text);
        List<Integer> indexes = new ArrayList<>();
        while (matcher.find()) {
            indexes.add(matcher.start());
        }
        return indexes;
    }

    private List<Integer> getPredatorsIndexes(String text) {
        List<Integer> indexes = new ArrayList<>();
        indexes.addAll(getIndexesByName(text, "хищник", "[,.!: ;?]", ""));
        indexes.addAll(getIndexesByName(text, "жерлиц", "[,.!: ;?]", ""));
        indexes.addAll(getIndexesByName(text, "живц", "[,.!: ;?]", ""));
        indexes.addAll(getIndexesByName(text, "живец", "[,.!: ;?]", ""));
        for(String fish: predatorsFish){
            if(fish.equals("сом")){
                for(String fishName: fishNamesVersion.get(fish)){
                    indexes.addAll(getIndexesByName(text, fishName, "[,.!: ;?]", "[,.!: ;?]"));
                }
            } else {
                for(String fishName: fishNamesVersion.get(fish)){
                    indexes.addAll(getIndexesByName(text, fishName, "[,.!: ;?]", ""));
                }
            }
        }
        return indexes;
    }

    private List<Integer> getPeaceFishIndexes(String text){
        List<Integer> indexes = new ArrayList<>();
        for(String fish: peaceFish){
            for(String fishName: fishNamesVersion.get(fish)){
                indexes.addAll(getIndexesByName(text, fishName, "[,.!: ;?]", ""));
            }
        }
        return indexes;
    }

    private List<Integer> getFishIndexes(String text, String fish) {
        List<Integer> indexes = new ArrayList<>();
        for (String fishName : fishNamesVersion.get(fish)) {
            indexes.addAll(getIndexesByName(text, fishName, "[,.!: ;?]", ""));
        }
        return indexes;
    }

    private Map<String, String> collectFishsText(String text, Multimap<String, Integer> fishsIndexes) {
        if (fishsIndexes.keySet().size() == 0) {
            return new HashMap<>();
        }
        Multimap<String, String> fishsTexts = ArrayListMultimap.create();
        Map<Integer, String> indexesNames = getIndexesNames(fishsIndexes);
        int startIndex = 0, punctuationListIndex = 0, index = 0;
        String currentFish = indexesNames.get(indexesNames.keySet().stream().min(Integer::compare).get());
        List<Integer> punctuationIndexes = getPunctuationIndexes(text);
        List<Integer> indexes = new ArrayList<>(indexesNames.keySet());
        if (punctuationIndexes.size() == 0) {
            fishsTexts.put(currentFish, " " + text.substring(startIndex, text.length() - 1));
            return sumMultimapTexts(fishsTexts);
        }
        for (int i = 1; i < indexes.size(); i++) {
            index = indexes.get(i);
            String fish = indexesNames.get(index);
            if (!fish.equals(currentFish)) {
                punctuationListIndex = evaluateDelimIndex(punctuationIndexes, indexes.get(i - 1), index);
                if (punctuationListIndex > indexes.get(i - 1) && punctuationListIndex < index) {
                    fishsTexts.put(currentFish, " " + text.substring(startIndex, punctuationListIndex));
                    startIndex = punctuationListIndex + 1;
                    currentFish = fish;
                }
            }
            if (startIndex >= text.length()) {
                startIndex = indexes.get(i - 1) + 3;
            }
        }
        fishsTexts.put(currentFish, " " + text.substring(startIndex, text.length() - 1));
        return sumMultimapTexts(fishsTexts);
    }


    private int evaluateDelimIndex(List<Integer> puncIndexes, int leftIndex, int rightIndex) {
        int boardIndex = evaluateBoardIndex(puncIndexes, rightIndex);
        if (boardIndex == 0) {
            return puncIndexes.get(boardIndex);
        }
        while (rightIndex - puncIndexes.get(boardIndex) < 10 && puncIndexes.get(boardIndex) > leftIndex
                && boardIndex > 0) {
            boardIndex--;
        }
        return puncIndexes.get(boardIndex);
    }

    private int evaluateBoardIndex(List<Integer> indexes, int board) {
        for (int i = 1; i < indexes.size(); i++) {
            if (indexes.get(i) > board) {
                return i - 1;
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
