
// You can use this file as a starting point for your dictionary client
// The file contains the code for command line parsing and it also
// illustrates how to read and partially parse the input typed by the user. 
// Although your main class has to be in this file, there is no requirement that you
// use this template or hav all or your classes in this file.

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.System;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.io.BufferedReader;
import java.util.spi.ResourceBundleControlProvider;

//
// This is an implementation of a simplified version of a command
// line dictionary client. The only argument the program takes is
// -d which turns on debugging output.
//


public class CSdict {
    static final int MAX_LEN = 255;
    static Boolean debugOn = false;
    private static final int PERMITTED_ARGUMENT_COUNT = 1;
    private static String command;
    private static String[] arguments;
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static BufferedReader stdIn;
    private static String dictionary;
    private static boolean isConnectionOpen = false;


    public static void main(String [] args) {
        byte cmdString[];
        int len;

        // Verify command line arguments

        if (args.length == PERMITTED_ARGUMENT_COUNT) {
            debugOn = args[0].equals("-d");
            if (debugOn) {
                System.out.println("Debugging output enabled");
            } else {
                System.out.println("997 Invalid command line option - Only -d is allowed");
                return;
            }
        } else if (args.length > PERMITTED_ARGUMENT_COUNT) {
            System.out.println("996 Too many command line options - Only -d is allowed");
            return;
        }


        // Example code to read command line input and extract arguments.

        // Quit flag to exit loop
        Boolean quitFlag = false;

        do {
            try {
                cmdString = new byte[MAX_LEN];
                System.out.print("csdict> ");
                System.in.read(cmdString);

                // Convert the command string to ASII
                String inputString = new String(cmdString, "ASCII");

                // Split the string into words
                String[] inputs = inputString.trim().split("( |\t)+");
                // Set the command
                command = inputs[0].toLowerCase().trim();
                // Remainder of the inputs is the arguments.
                arguments = Arrays.copyOfRange(inputs, 1, inputs.length);
                len = arguments.length;

//                System.out.println("The command is: " + command);
//                System.out.println("The arguments are: ");
//                for (int i = 0; i < len; i++) {
//                    System.out.println("    " + arguments[i]);
//                }

                if (command.equals("") || command.charAt(0) == '#')
                    continue;
                switch (command) {
                    case "open":
                        if (len != 2) {
                            throw new ClientError("901 Incorrect number of arguments.");
                        }
                        try {
                            Integer.parseInt(arguments[1]);
                        } catch (NumberFormatException e) {
                            throw new ClientError("902 Invalid argument.");
                        }
                        openConnection(arguments[0], Integer.parseInt(arguments[1]));
                        break;
                    case "dict":
                        if (!isConnectionOpen) {
                            throw new ClientError("903 Supplied command not expected at this time");
                        }
                        if (len != 0) {
                            throw new ClientError("901 Incorrect number of arguments.");
                        }
                        printDictionaries();
                        break;
                    case "set":
                        if (!isConnectionOpen) {
                            throw new ClientError("903 Supplied command not expected at this time");
                        }
                        if (len != 1) {
                            throw new ClientError("901 Incorrect number of arguments.");
                        }
                        setDictionary(arguments[0]);
                        break;
                    case "define":
                        if (!isConnectionOpen) {
                            throw new ClientError("903 Supplied command not expected at this time");
                        }
                        if (len != 1) {
                            throw new ClientError("901 Incorrect number of arguments.");
                        }
                        defineWord(arguments[0]);
                        break;
                    case "match":
                        if (!isConnectionOpen) {
                            throw new ClientError("903 Supplied command not expected at this time");
                        }
                        if (len != 1) {
                            throw new ClientError("901 Incorrect number of arguments.");
                        }
                        match(dictionary, "exact", arguments[0], false);
                        break;
                    case "prefixmatch":
                        if (!isConnectionOpen) {
                            throw new ClientError("903 Supplied command not expected at this time");
                        }
                        if (len != 1) {
                            throw new ClientError("901 Incorrect number of arguments.");
                        }
                        match(dictionary, "prefix", arguments[0], false);
                        break;
                    case "close":
                        if (!isConnectionOpen) {
                            throw new ClientError("903 Supplied command not expected at this time");
                        }
                        if (len != 0) {
                            throw new ClientError("901 Incorrect number of arguments.");
                        }
                        close();
                        break;
                    case "quit":
                        if (len != 0) {
                            throw new ClientError("901 Incorrect number of arguments.");
                        } else {
                            quitFlag = true;
                        }
                        break;
                    default:
                        throw new ClientError("900 Invalid command.");
                }

            } catch (IOException exception) {
                System.out.println("998 Input error while reading commands, terminating.");
                System.exit(-1);
            }
            catch (ClientError e) {
                System.out.println(e.getMessage());
            }
        } while (quitFlag == false);
    }

    private static void openConnection(String hostname, int port) throws IOException {
        try {
            // will either need to make this a TimeLimitedCodeBlock or maybe a Future or something


            socket = new Socket();
            socket.connect(new InetSocketAddress(hostname, port), 30 * 1000);
            socket.setSoTimeout(30*1000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            dictionary = "*";
            String detailedStatusInfo = in.readLine();
            if (debugOn) {
                System.out.println("<-- " + detailedStatusInfo);
            }
            isConnectionOpen = true;
        }
        catch (SocketTimeoutException e) {
            if (e.toString().indexOf("Connect") != -1) {
                System.out.printf("920 Control connection to %s on port %s failed to open.\n", hostname, port);
            } else {
                System.out.println("999 Processing error. Timed out while waiting for a response.");
            }
        }
        catch (Exception e) {
            System.out.println("error occurred during connection");
        }
    }

    private static void setDictionary(String name) {
        dictionary = name;
    }

    private static void printDictionaries() throws IOException {
        String command = "SHOW DB\r\n";
        if (debugOn) {
            System.out.print("> " + command);
        }
        out.print(command);
        out.flush();
        String response = in.readLine();
        while (!response.equals(".")) {
            System.out.println(response);
            response = in.readLine();
        }
        System.out.println(".");
        String detailedStatusInfo = in.readLine();
        if (debugOn) {
            System.out.println("<-- " + detailedStatusInfo);
        }
    }

    private static void defineWord(String word) throws IOException {
        String command = String.format("DEFINE %s %s\r\n", dictionary, word);
        if (debugOn) {
            System.out.print("> " + command);
        }
        out.printf(command);
        Response response = new Response(in);

        if (response.getStatusCode() == response.SUCCESSFUL_RETRIEVAL) {
            int numOfDefinitions = response.getNumOfMatches();
            for (int i = 0; i < numOfDefinitions; i++) {
                Definition definition = new Definition(in);
                System.out.println(definition.getDatabaseInfo());
                System.out.println(definition.getDefinition());
                System.out.println(".");
            }
            in.readLine();
        } else if (response.getStatusCode() == response.NO_MATCH) {
            System.out.println("****No definition found****");
            match(dictionary, ".", word, true);

        }
    }

    private static void match(String dictionary, String strategy, String word, boolean isDefineFlag) throws IOException {
        String command = String.format("MATCH %s %s %s\r\n", dictionary, strategy, word);
        if (debugOn) {
            System.out.print("> " + command);
        }
        out.printf(command);
        Response response = new Response(in);
        String detailedStatusInfo;
        if (response.getStatusCode() == response.MATCH_FOUND) {
            String output;
            while (!(output = in.readLine()).equals(".")) {
                System.out.println(output);
            }
            System.out.println(".");
            detailedStatusInfo = in.readLine();
            if (debugOn) {
                System.out.println("<-- " + detailedStatusInfo);
            }
        } else if (response.getStatusCode() == response.NO_MATCH) {
            if (!isDefineFlag) {
                System.out.println("****No matching word(s) found****");
            } else {
                System.out.println("****No matches found****");
            }
        }
    }

    private static void close() throws IOException {
        String command = "quit\r\n";
        if (debugOn) {
            System.out.print("> " + command);
        }
        out.printf(command);
        Response response = new Response(in);
        if (response.getStatusCode() == response.SUCCESSFUL_CLOSE) {
            isConnectionOpen = false;
        } else {
            throw new ClientError("999 Processing error. Could not close connection.");
        }
    }
}
    
    
