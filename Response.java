import java.io.BufferedReader;
import java.io.IOException;

public class Response {
    public static final int INVALID_DATABASE = 550;
    public static final int NO_MATCH = 552;
    public static final int SUCCESSFUL_RETRIEVAL = 150;
    public static final int MATCH_FOUND = 152;
    public static final int DICTIONARY_INFO = 151;
    public static final int OK = 250;

    private int statusCode;
    private int numOfMatches = -1;



    public Response(BufferedReader in) throws IOException {
        String initialResponse = in.readLine();
        String[] codes = initialResponse.split(" ");
        statusCode = Integer.parseInt(codes[0]);
        if (statusCode == SUCCESSFUL_RETRIEVAL || statusCode == MATCH_FOUND || statusCode == 152) {
            numOfMatches = Integer.parseInt(codes[1]);
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public int getNumOfMatches() {
        return numOfMatches;
    }
}
