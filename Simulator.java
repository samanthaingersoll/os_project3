package os_project3;

import java.util.*;
import java.io.*;
import java.lang.Math.*;
import java.nio.file.Paths;
import java.util.concurrent.ThreadLocalRandom;

public class Simulator { 
    public static void main(String[] args) throws Exception { 
        // take note of which arguments are required
        Map<String, String> requiredArgs = new HashMap<String, String>();
        requiredArgs.put("input", "unset");
        requiredArgs.put("policy", "unset");

        // set file paths to default
        // default input path is only used if -i is not specified but -g is
        File inputFile = new File(Paths.get(".").toAbsolutePath().normalize().toString() + "/input.txt");
        String outputFile = Paths.get(".").toAbsolutePath().normalize().toString() + "/output.txt";
        // policy must be set, or nothing happens
        String policies[] = {"","","","","","","","",""};
        int startPoint = 100;

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
                inputFile = new File(args[i+1].toString());
                requiredArgs.put("input", args[i+1].toString());
                System.out.println("input flag recognized; setting input file to " + args[i+1].toString());
                i++; //skip the next arg
            } else if (args[i].equals("--generate") || args[i].equals("-g")) {
                generateNumbers(1000, inputFile);
                requiredArgs.put("input", "generated");
                System.out.println("generate flag recognized; creating 1000 new numbers in " + inputFile.getPath());
            } else if (args[i].equals("--output") || args[i].equals("-o")) {
                outputFile = args[i+1].toString();
                System.out.println("output flag recognized; setting output file to " + args[i+1].toString());
                i++; //skip the next arg
            } else if (args[i].equals("--start") || args[i].equals("-s")) {
                startPoint = Integer.parseInt(args[i+1]);
                System.out.println("start flag recognized; setting start point to " + args[i+1].toString());
                i++; //skip the next arg
            } else if (args[i].equals("--policy") || args[i].equals("-p")) {
                policies = args[i+1].toString().split(",");
                requiredArgs.put("policy", args[i+1].toString());
                i++; //skip the next arg
            } else {
                // arg is something unrecognized
                System.out.println("The argument (" + args[i].toString() + ") is unrecognized!");
                System.out.println("It will be ignored.");
            }
        }

        // check that all required arguments are present - values are changed from "unset"
        for (Map.Entry<String, String> entry: requiredArgs.entrySet()){ 
            if (entry.getValue().equals("unset")) {
                System.out.println("You are missing a required argument! " + entry.getKey());
                printHelpDialog();
                java.lang.System.exit(1);
            }
        }

        //List<String> policies = new ArrayList<String>(Arrays.asList(policiesStr.split(",")));

        if (!inputFile.exists()) {
            inputFile.createNewFile();
        }

        // finally, get to the actual meat and potatoes of the program
        
        // determine how wide the output table should be
        int tableWidth = 8; //start with 1, which is the left wall
        for (String policy: policies) {
            tableWidth++; //add one, for the separator/right wall if last
            tableWidth += (2 * policy.length()); //increase by #chars of policy x 2
        }
        // make vertical divider of proper width
        String divider = new String(new char[tableWidth]).replace('\0', '-');
        
        // output headers of table
        logLine(outputFile, divider+"\n");
        logLine(outputFile, String.format("%s", "| Next |"));
        for (String policy: policies) {
            // this centers the policy name in a box twice its length
            logLine(outputFile, String.format("%"+Math.ceil(policy.length()/2)+"s%"+policy.length()+"s%"+Math.floor(policy.length()/2)+"s%s","", policy,"", "|"));
        }
        logLine(outputFile, String.format("%n"));
        logLine(outputFile, divider+"\n");
        
        // set up the reader/writer for input/output
            

        int lastTrack = startPoint;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String nextLine = "";
            Map<String, double[]> seekTimes = new HashMap<String, double[]>();
            double times[] = new double[1000]; 
            for (int i = 0; i < policies.length; i++) {
                seekTimes.put(policies[i], times);
            }
            int lineNum = 0;
            while ((nextLine = reader.readLine()) != null) {
                int tracksTraversed[] = {0,0,0,0,0,0,0,0};
                int nextTrack = Integer.parseInt(nextLine);
                for (int i = 0; i < policies.length; i++) {
                    if (policies[i].length() != 0) {
                        double results[] = {0,0};
                        switch (policies[i]) {
                            case "FIFO": results = processFIFO(nextTrack, lastTrack);
                                break;
                            case "LIFO": results = processLIFO(nextTrack, lastTrack); 
                                break;
                            case "SSTF": results = processSSTF(nextTrack, lastTrack);
                                break;
                            case "SCAN": results = processSCAN(nextTrack, lastTrack);
                                break;
                            case "C-SCAN": results = processCSCAN(nextTrack, lastTrack);
                                break;
                            case "CN-STEP-SCAN": results = processNSTEPSCAN(nextTrack, lastTrack);
                                break;
                            case "FSCAN": results = processFSCAN(nextTrack, lastTrack);
                                break;
                        }
                        tracksTraversed[i] =(int)Math.round(results[0]);
                        double[] time = seekTimes.get(policies[i]);
                        time[lineNum] = results[1];
                        seekTimes.put(policies[i], time);
                    }
                    lastTrack = nextTrack;
                }

                String row = String.format("%-2s%03d%3s", "|", nextTrack, "|");
                for (int i = 0; i < tracksTraversed.length; i++) {
                    if (tracksTraversed[i] != 0) {
                        row = row + String.format("%"+Math.ceil(policies[i].length()/2)+"s%"+policies[i].length()+"d%"+Math.floor(policies[i].length()/2)+"s%s","", tracksTraversed[i],"", "|");
                    }
                }
                row = row + "\n";
                logLine(outputFile, row);

                for (int i = 0; i < tracksTraversed.length; i++) {
                    tracksTraversed[i] = 0;
                }   
                lineNum++;
            }
            logLine(outputFile, divider+"\n");
            reader.close();

            // calculate and display avg seek times
            double avgs[] = {0,0,0,0,0,0,0,0};
            for (int i = 0; i < policies.length; i++) {
                times = seekTimes.get(policies[i]);
                double sum = 0;
                for (int j = 0; j < times.length; j++) {
                    sum += times[j];
                }
                avgs[i] = (sum / seekTimes.get(policies[i]).length);;
            }
            String row = String.format("%-2s%3s%3s", "|", "avg", "|");
            for (int i = 0; i < avgs.length; i++) {
                if (avgs[i] != 0) {
                  row = row + String.format("%"+Math.ceil(policies[i].length()/2)+"s%"+policies[i].length()+".0f%"+Math.floor(policies[i].length()/2)+"s%s", "", avgs[i], "ns", "|");
                }
            }
            row = row + "\n";
            logLine(outputFile, row);
            logLine(outputFile, divider+"\n");

        } catch (FileNotFoundException fnf) {
            System.out.println("The specified file cannot be found!");
        } catch (IOException ioe) {
            System.out.println("There was an I/O error!");
        } 
        

        
    }

    private static double[] processFIFO(int nextTrack, int lastTrack) {
        long startTime = System.nanoTime();
        int moves = 0;
        while (lastTrack != nextTrack) {
            if (nextTrack > lastTrack) {
                lastTrack++;
            } else if (nextTrack < lastTrack) {
                lastTrack--;
            }
            moves++;
        }
        long stopTime = System.nanoTime();
        long totalTime = stopTime - startTime;
        
        double[] results = {moves, (totalTime)};
        return results;
    }
    private static double[] processLIFO(int nextTrack, int lastTrack) {
        double[] results = {1, 1};
        return results;
    }
    private static double[] processSSTF(int nextTrack, int lastTrack) {
        double[] results = {2, 2};
        return results;
    }
    private static double[] processSCAN(int nextTrack, int lastTrack) {
        double[] results = {3, 3};
        return results;
    }
    private static double[] processCSCAN(int nextTrack, int lastTrack) {
        double[] results = {4, 4};
        return results;
    }
    private static double[] processNSTEPSCAN(int nextTrack, int lastTrack) {
        double[] results = {5, 5};
        return results;
    }
    private static double[] processFSCAN(int nextTrack, int lastTrack) {
        double[] results = {6, 6};
        return results;
    }

    private static File generateNumbers(int num, File file) {
        try {
            PrintWriter out = new PrintWriter(file);
            new Random();
            int number, count=0;
            while(count<=999) {
            number = ThreadLocalRandom.current().nextInt(0, 200);
            count++;
                out.println(number);
            }
            out.close();
        } catch (FileNotFoundException fnf) {
            System.out.println("The specified file cannot be found!");
        }
        return file;
    }

    private static void printHelpDialog() {
        System.out.println("Possible arguments are as follows:");
        System.out.printf("%-15s %s%n", "-h --help", "shows the help dialog (you are here)");
        System.out.printf("%-15s %s%n", "-i --input", "(REQUIRED) file that contains the 'next track' numbers, one per line");
        System.out.printf("%-15s %s%n", "-g --generate", "create a file to be used as input of 1000 numbers between 1-200");
        System.out.printf("%-15s %s%n", "",  "if used after -i will replace the file specified with -i");
        System.out.printf("%-15s %s%n", "",  "otherwise defaults to input.txt");
        System.out.printf("%-15s %s%n", "-o --output", "specifies the output file to which to write the log tables");
        System.out.printf("%-15s %s%n", "",  "defaults to output.txt if not provided");
        System.out.printf("%-15s %s%n", "-s --start", "a number between 1-200 that specifies the on which track to begin");
        System.out.printf("%-15s %s%n", "", "defaults to 100");
        System.out.printf("%-15s %s%n", "-p --policy", "(REQUIRED) the disk cheduling polic(ies) to use. ");
        System.out.printf("%-15s %s%n", "", "Possible policies are FIFO, LIFO, SSTF, SCAN, C-SCAN, N-STEP-SCAN, and FSCAN");
        System.out.printf("%-15s %s%n", "", "to use multiple, use commas but no spaces (e.g. 'FIFO,C-SCAN,SSTF')");
    }

    private static void logLine(String filePath, String line) {
        try { 
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
            writer.write(line); 
            //writer.newLine();
            System.out.printf("%s", line); 

            writer.close(); 
        } 
        catch (IOException except) { 
            except.printStackTrace(); 
        } 
    }
}
