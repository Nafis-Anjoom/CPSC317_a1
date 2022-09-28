import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Definition {
    private String dictId;
    private String dictName;
    private String definition = "";

    public Definition(BufferedReader in) throws IOException {
        String databaseInfo = in.readLine();
        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Matcher m = p.matcher(databaseInfo);

        dictId = databaseInfo.split(" ")[2];
        while(m.find()) {
            dictName = m.group(0);
        }

        String input;
        definition += in.readLine();
        while(!(input = in.readLine()).equals(".")) {
            definition += "\n" + input;
        }
    }

    public String getDefinition() {
        return definition;
    }

    public String getDatabaseInfo() {
        return String.format("@ %s %s", dictId, dictName);
    }
}
