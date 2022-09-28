
// You can use this file as a starting point for your dictionary client
// The file contains the code for command line parsing and it also
// illustrates how to read and partially parse the input typed by the user. 
// Although your main class has to be in this file, there is no requirement that you
// use this template or hav all or your classes in this file.

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.System;
import java.io.IOException;
import java.net.Socket;
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


    public static void main(String [] args) {
        byte cmdString[] = new byte[MAX_LEN];
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

        try {
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

            System.out.println("The command is: " + command);
            len = arguments.length;
            System.out.println("The arguments are: ");
            for (int i = 0; i < len; i++) {
                System.out.println("    " + arguments[i]);
            }

            while (!command.equals("quit")) {
                switch (command) {
                    case "open":
                        openConnection(arguments[0], Integer.parseInt(arguments[1]));
                        break;
                    case "dict":
                        printDictionaries();
                        break;
                    case "set":
                        setDictionary(arguments[0]);
                        break;
                    case "define":
                        defineWord(arguments[0]);
                        break;
                    case "match":
                        match(dictionary, "exact", arguments[0]);
                        break;
                    case "quit":
                        return;
                    default:
                        System.out.println("invalid command");
                        break;
                }
                System.out.print("csdict> ");
                cmdString = new byte[MAX_LEN];
                System.in.read(cmdString);

                // Convert the command string to ASII
                inputString = new String(cmdString, "ASCII");

                // Split the string into words
                inputs = inputString.trim().split("( |\t)+");
                // Set the command
                command = inputs[0].toLowerCase().trim();
                // Remainder of the inputs is the arguments.
                arguments = Arrays.copyOfRange(inputs, 1, inputs.length);

                System.out.println("The command is: " + command);
                len = arguments.length;
                System.out.println("The arguments are: ");
                for (int i = 0; i < len; i++) {
                    System.out.println("    " + arguments[i]);
                }
            }

	    System.out.println("Done.");

        } catch (IOException exception) {
            System.err.println("998 Input error while reading commands, terminating.");
            System.exit(-1);
        }
    }

    private static void openConnection(String hostname, int port) throws IOException {
        try {
            socket = new Socket(hostname, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            dictionary = "*";
            String detailedStatusInfo = in.readLine();
            System.out.println(detailedStatusInfo);
        }
        catch (Exception e) {
            System.out.println("error occurred during connection");
        }
    }

    private static void setDictionary(String name) {
        dictionary = name;
    }

    private static void printDictionaries() throws IOException {
        out.print("SHOW DB\r\n");
        out.flush();
        System.out.println("> SHOW DB");
        String response = in.readLine();
        while (!response.equals(".")) {
            System.out.println(response);
            response = in.readLine();
        }
        System.out.println(".");
        String detailedStatusInfo = in.readLine();
    }

    private static void defineWord(String word) throws IOException {
        out.printf("DEFINE %s %s\r\n", dictionary, word);
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
            match(dictionary, ".", word);

        }
    }

    private static void match(String dictionary, String strategy, String word) throws IOException {
        out.printf("MATCH %s %s %s\r\n", dictionary, strategy, word);
        Response response = new Response(in);
        if (response.getStatusCode() == response.MATCH_FOUND) {
            String output;
            while (!(output = in.readLine()).equals(".")) {
                System.out.println(output);
            }
            String detailedStatusInfo = in.readLine();
        }


    }
}
    
    
