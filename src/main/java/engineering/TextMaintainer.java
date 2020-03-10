package engineering;

import java.util.Properties;

public class TextMaintainer {
    private final String databaseName;
    private final String sourceTableName;
    private final String distTableName;
    private final String commentsTableName;

    public TextMaintainer(String databaseName, String sourceTableName, String distTableName, String commentsTableName) {
        this.databaseName = databaseName;
        this.sourceTableName = sourceTableName;
        this.distTableName = distTableName;
        this.commentsTableName = commentsTableName;
    }

    public void start(){
        TextSeparator textSeparator = new TextSeparator(this.databaseName, this.sourceTableName, this.distTableName);
        textSeparator.start();
//        TODO: вызывать питоновский скрипт
        GrouperByFish grouperByFish = new GrouperByFish(this.databaseName, this.commentsTableName);
        grouperByFish.start();
    }
}
