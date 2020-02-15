package engineering;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class Lemmatizer {
    public static void main(String[] args) throws IOException {
        Lemmatizer lemmatizer = new Lemmatizer();
        String str = "вчера снова копринская акватория изначально вообще то собирались на сить  и даже уже перемахнули плотину в угличе  но актуальная информация вечером накануне перед выездом поселила червячка сомнения 7 и 8 половили  а 9 уже некоторые и поклёвки не видели  и рыба в камере отсутствует  прекрасно  короче  по пути меняем планы и уходим на мышкин  а там на переправу и дальше в сторону коприно";
        System.out.println("до: " + str);
        System.out.println("после: " + lemmatizer.lemmatize(str));
    }

    private String lemmatize(String text) throws IOException {
        List<String> tokens = tokenize(text);
        for(int i=0;i<tokens.size();i++){
            tokens.set(i, lemmatizeToken(new String(tokens.get(i).getBytes(), "utf-8")));
        }
        return collectTokens(tokens);
    }

    private List<String> tokenize(String text){
        List<String> tokens = new ArrayList<>();
        StringTokenizer stringTokenizer = new StringTokenizer(text);
        while (stringTokenizer.hasMoreTokens()){
            tokens.add(stringTokenizer.nextToken());
        }
        return tokens;
    }

    private String collectTokens(List<String> tokens){
        StringBuilder stringBuilder = new StringBuilder(tokens.get(0));
        for(int i=1;i< tokens.size();i++){
            stringBuilder.append(" " + tokens.get(i));
        }
        return stringBuilder.toString();
    }

    private String lemmatizeToken(String token) throws IOException {
        if(isNumeric(token)){
            return token;
        }
        LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
        String temp = luceneMorphology.getNormalForms(new String(token.getBytes(), "utf-8")).get(0);
        return temp;
    }


    public boolean isNumeric(String strNum) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }
}
