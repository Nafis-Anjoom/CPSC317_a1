import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Testbench {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("dict.org", 2628);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        in.readLine();
//        out.println("DEFINE * airplane\r\n");
//        String terminate = "\r\n.\n";
//        ByteBuffer buffer = StandardCharsets.UTF_8.encode(terminate);
//        String encodedTerminate = StandardCharsets.UTF_8.decode(buffer).toString();
//        String input;
//        while (!(input = in.readLine()).equals(".")) {
//            System.out.println(input);
//        }
        String database = "*";
        String strategy = ".";
        String word = "airplan";
        out.printf("MATCH %s %s %s\r\n", database, strategy, word);
        Response response = new Response(in);
        if (response.getStatusCode() == Response.MATCH_FOUND) {
            for (int i = 0; i < response.getNumOfMatches(); i++) {
                System.out.println(in.readLine());
            }

        }
        String input;
        while (!(input = in.readLine()).equals(".")) {
            System.out.println(input);
        }


    }
}
