package os_project3;

import java.util.*;
import java.io.*;
import java.nio.file.Paths;

public class Simulator { 
    public static void main(String[] args) throws Exception { 
        // take note of which arguments are required
        Map<String, String> requiredArgs = new HashMap<String, String>();
        requiredArgs.put("input", "unset");
        requiredArgs.put("policy", "unset");

        // set file paths to default
        // input path is only used if -i is not specified but -g is
        File inputFile = new File(Paths.get(".").toAbsolutePath().normalize().toString() + "/input.txt");
        File outputFile = new File(Paths.get(".").toAbsolutePath().normalize().toString() + "/output.txt");
        // policy must be set, or nothing happens
        String policy;

        if (args.length == 0) {
            // some arguments are required, so
            // print possible arguments and exit
            System.out.println("You didn't include any arguments!");
            System.out.println("You need to do that, some are required");
            printHelpDialog();
            java.lang.System.exit(1);
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--help") || args[i].equals("-h")) {
                // print possible arguments and exit
                printHelpDialog();
                java.lang.System.exit(1);
            } else if (args[i].equals("--input") || args[i].equals("-i")) {
                // TODO: Import txt to array
                inputFile = new File(args[i].toString());
                requiredArgs.put("input", args[i].toString());
            } else if (args[i].equals("--generate") || args[i].equals("-g")) {
                // TODO: call RNG to create next-track array
            } else if (args[i].equals("--output") || args[i].equals("-o")) {
                outputFile = new File(args[i].toString());
            } else if (args[i].equals("--start") || args[i].equals("-s")) {
                // TODO: implement start point stuff
            } else if (args[i].equals("--policy") || args[i].equals("-p")) {
                policy = args[i].toString();
                requiredArgs.put("policy", args[i].toString());
            } else {
                // arg is something unrecognized
                System.out.println("The argument (" + args[i].toString() + ") is unrecognized!");
                System.out.println("It will be ignored.");
            }
        }


        // check that all required arguments are present - values are changed from "unset"
        for (Map.Entry<String, String> entry: requiredArgs.entrySet()){ 
            if (entry.getValue().equals("unset")) {
                System.out.println("You are missing a required argument! " 
                                    + entry.getKey());
                printHelpDialog();
                java.lang.System.exit(1);
            }
        }
        

    }

    private static void printHelpDialog() {
        System.out.println("Possible arguments are as follows:");
        System.out.printf("%-15s %s%n", "-h --help", "shows the help dialog (you are here)");
        System.out.printf("%-15s %s%n", "-i --input", "(REQUIRED) file that contains the 'next track' numbers, one per line");
        System.out.printf("%-15s %s%n", "-g --generate", "create a file to be used as input of 1000 numbers between 1-200");
        System.out.printf("%-15s %s%n", "",  "if used after -i will replace the file specified with -i");
        System.out.printf("%-15s %s%n", "-o --output", "specifies the output file to which to write the log tables");
        System.out.printf("%-15s %s%n", "",  "defaults to output.txt if not provided");
        System.out.printf("%-15s %s%n", "-s --start", "a number between 1-200 that specifies the on which track to begin");
        System.out.printf("%-15s %s%n", "", "defaults to 100");
        System.out.printf("%-15s %s%n", "-p --policy", "(REQUIRED) the disk cheduling polic(ies) to use. ");
        System.out.printf("%-15s %s%n", "", "Possible policies are FIFO, LIFO, SSTF, SCAN, C-SCAN, N-STEP-SCAN, and FSCAN");
        System.out.printf("%-15s %s%n", "", "to use multiple, use commas but no spaces (e.g. 'FIFO,C-SCAN,SSTF')");
    }
}
