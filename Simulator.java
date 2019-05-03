package os_project3;

import java.util.*;
import java.io.*;
import java.lang.Math.*;
import java.nio.file.Paths;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class Simulator { 
    public static void main(String[] args) throws Exception { 
        // set up a system to log which args were used
        Map<String, String> includedArgs = new HashMap<String, String>();
        String possibleArgs[] = {"v", "h", "i", "g", "o", "s", "p", "b"};
        for (String arg: possibleArgs) {
            includedArgs.put(arg, "false");
        }

        // set file paths to default
        // default input path is only used if -i is not specified but -g is
        File readFile = new File(Paths.get(".").toAbsolutePath().normalize().toString() + "/input.txt");
        String writeFile = Paths.get(".").toAbsolutePath().normalize().toString() + "/output.txt";
        // policy must be set, or nothing happens
        // the system can handle up to eight policies at once,
        // but no doubles. Seeing as there are only seven to choose from,
        // this should cover any instance of runtime variability
        String policies[] = {"","","","","","","","",""};
        String possiblePolicies[] = {"FIFO","LIFO","SSTF","SCAN","C-SCAN","N-STEP-SCAN","FSCAN"};

        int startPoint = 100; // which track to start on (default)
        int batch = 5; // number of nextTracks to send to each policy (default)
        boolean debug = false;

        if (args.length == 0) {
            // at least the required args are necessary
            System.out.println("You didn't include any arguments!\n You need to do that, some are required");
            printHelpDialog();
            java.lang.System.exit(1);
        }
        
        // check the args provided and do things with them
        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-h":
                    case "--help":
                        // print possible arguments and exit
                        printHelpDialog();
                        java.lang.System.exit(1);
                        break;
                    case "-v":
                    case "--verbose":
                        debug = true;
                        break;
                    case "-i":
                    case "--input":
                        if (!(args[i+1].toString().endsWith(".txt"))) {
                            System.err.println("The input path must be to a txt file!\n e.g. \"-i input.txt\"");
                            java.lang.System.exit(1);
                        } else {
                            includedArgs.put("i", args[i+1].toString());
                            i++; //skip the next arg
                        }
                        break;
                    case "-g":
                    case "--generate":
                        if ((!(args[i+1].toString().equals("random"))) && (!(args[i+1].toString().equals("alternate")))) {
                            System.err.println("The generate argument must be one of the accepted generation methods!\n (random or alternate)\n e.g. \"-g random\"");
                            java.lang.System.exit(1);
                        } else {
                            includedArgs.put("g", args[i+1].toString());
                            i++; //skip the next arg
                        }
                        break;
                    case "-o":
                    case "--output":
                        if (!(args[i+1].toString().endsWith(".txt"))) {
                            System.err.println("The output path must be to a txt file!\n e.g. \"-i output.txt\"");
                            java.lang.System.exit(1);
                        } else {
                            includedArgs.put("o", args[i+1].toString());
                            i++; //skip the next arg
                        }
                        break;
                    case "-s":
                    case "--start":
                        try {
                            startPoint = Integer.parseInt(args[i+1]);
                            if (startPoint < 1 || startPoint > 200) {
                                System.err.println("Start point must be between 1-200!");
                                java.lang.System.exit(1);
                            } else {
                                System.out.println("start flag recognized; setting start point to " + args[i+1].toString());
                                i++; //skip the next arg
                            }
                        } catch (NumberFormatException nfe) {
                            System.err.println("The start flag must be followed by an integer!");
                            java.lang.System.exit(1);
                        }
                        break;
                    case "-p":
                    case "--policy":
                        if (args[i+1].startsWith("-")) {
                            System.err.println("You must include a policy type after the policy flag!");
                            java.lang.System.exit(1);
                        } else {
                            includedArgs.put("p", args[i+1]);
                            policies = args[i+1].toString().split(",");
                            // check that the included policies are valid
                            // and that there are no doubles
                            Map<String, Integer> includedPolicies = new HashMap<String, Integer>();
                            for (String policy: possiblePolicies) {
                                includedPolicies.put(policy, 0);
                            }
                            for (String arg: policies) {
                                boolean valid = false;
                                for (String policy: possiblePolicies) {
                                    if (arg.equals(policy)) {
                                        valid = true;
                                        if (includedPolicies.get(policy) > 0) {
                                            System.err.println("You cannot have duplicate policies!\n (there is more than one " + policy + " in your arguments)");
                                            java.lang.System.exit(1);
                                        }
                                        includedPolicies.put(policy, 0);
                                    }
                                }
                                if (valid == false) {
                                    System.err.println(arg + " is not a valid policy, please remove or fix it and try again!");
                                    java.lang.System.exit(1);
                                }
                            }
                            i++; //skip the next arg
                        }
                        break;
                    case "-b":
                    case "--batch":
                        try {
                            batch = Integer.parseInt(args[i+1]);
                            if (100 % batch != 0) {
                                System.err.println("1000 must be cleanly divisble by your batch number,\n please change it and try again");
                                java.lang.System.exit(1);
                            }
                            System.out.println("batch flag recognized; setting batch number to " + args[i+1].toString());
                            i++; //skip the next arg
                        } catch (NumberFormatException nfe) {
                            System.err.println("The batch number must be followed by an integer!");
                            java.lang.System.exit(1);
                        }
                        break;
                    default:
                        // arg is something unrecognized
                        System.out.println("The argument (" + args[i].toString() + ") is unrecognized!\n It will be ignored.");
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("You ended your arguments with one that requires more information.\n You can see what exactly it requires by adding anything to the end and re-running the program.");
            java.lang.System.exit(1);
        }

        // check that the required args are included
        if (includedArgs.get("i").equals("false")) {
            if (includedArgs.get("g").equals("false")) {
                System.err.println("The input flag is a required argument!\n Please include it and run the program again");
            } else {
                System.out.println("The input flag is usually a required argument,\n but since you included the generate flag\n we're making a new file called \'input.txt\' anyway");
            }
        }
        if (includedArgs.get("p").length() == 0) {
            System.err.println("The policy flag is a required argument!\n Please include it and run the program again");
        }

        // process some of the args in this way because the order matters
        // and the user may not have entered them in that order
        for (String arg: possibleArgs) {
            switch (arg) {
                case "i":
                    if (!(includedArgs.get("g").equals("false"))) {
                        readFile = new File(includedArgs.get("i"));
                        System.out.println("input flag recognized; setting input file to " + includedArgs.get("i"));
                    }
                    break;
                case "g":
                    if (!(includedArgs.get("g").equals("false"))) {
                        generateNumbers(includedArgs.get("g"), readFile);
                        System.out.println("generate flag recognized; creating 1000 new numbers in " + readFile.getPath());
                    }
                    break;
                case "o":
                    if (!(includedArgs.get("o").equals("false"))) {
                        writeFile = includedArgs.get("o");
                    }
                    System.out.println("output flag recognized; setting output file to " + includedArgs.get("o"));

            }
        }

        // create a new input file if none exist (it should)
        if (!readFile.exists()) {
            readFile.createNewFile();
        }
        // make a new output file
        File output = new File(writeFile);
        if (output.exists()) {
            output.delete();
            output.createNewFile();
        }

        // first, log a little information 
        logLine(writeFile, "\nThe table is organized as such:\n");
        logLine(writeFile, "| Policy Name |\n");
        logLine(writeFile, "| Next track | Tracks Moved |\n");
        logLine(writeFile, "| ... |\n");
        logLine(writeFile, "| Average Seek Length |\n");
        logLine(writeFile, "| Average Seek Time |\n");
        
        // determine how wide the output table should be
        int tableWidth = 1; //start with 1, which is the left wall
        for (String policy: policies) {
            tableWidth++; //add one, for the separator/right wall if last
            tableWidth += (Math.ceil(2*(policy.length()/3))+policy.length()+Math.floor(2*(policy.length()/3))); //increase by #chars of policy x 2
        }
        // make vertical divider of proper width
        String divider = new String(new char[tableWidth]).replace('\0', '-');
        divider = divider + "\n";
        
        // output headers of table
        logLine(writeFile, divider);
        logLine(writeFile, "|");
        for (String policy: policies) {
            // this centers the policy name in a box twice its length
            logLine(writeFile, String.format("%"+(Math.ceil(2*(policy.length()/3))+"s%"+policy.length()+"s%"+Math.floor(2*(policy.length()/3)))+"s%s","", policy,"", "|"));
        }
        logLine(writeFile, "\n");
        // output the start point
        logLine(writeFile, divider);
        logLine(writeFile, "|");
        for (String policy: policies) {
            logLine(writeFile, String.format("%"+(Math.ceil(2*(policy.length()/3))+"s%"+policy.length()+"s%"+Math.floor(2*(policy.length()/3)))+"s%s","", startPoint,"", "|"));
        }
        logLine(writeFile, "\n");
        logLine(writeFile, divider);

                
        // finally, get to the actual meat and potatoes of the program
        
        // these maps exists so we can get the proper pipes/semaphores knowing only the policy they belong to
        Map<String, PipedInputStream> readPipes = new HashMap<String, PipedInputStream>();
        Map<String, PipedOutputStream> writePipes = new HashMap<String, PipedOutputStream>();
        Map<String, Semaphore[]> semaphores = new HashMap<String, Semaphore[]>();
        // create threads for each policy
        for (String policy: policies) {
            // set up the reader/writer for input/output
            PipedInputStream newWritePipe = new PipedInputStream();
            PipedOutputStream newReadPipe = new PipedOutputStream();
            newWritePipe.connect(newReadPipe);
            // and semaphores to control access between the main and policy threads during use
            // 0/1 are read/write, 2/3 are used alternately to control movement between steps of the process
            Semaphore newSemaphores[]  = {new Semaphore(0), new Semaphore(1), new Semaphore(1), new Semaphore(1)};
            // add those to the HashMaps so we can easily access them later
            readPipes.put(policy, newWritePipe);
            writePipes.put(policy, newReadPipe);
            semaphores.put(policy, newSemaphores);

            // start 
            Thread policyThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    startNewThread(policy, semaphores, readPipes, writePipes);
                }
            });
            policyThread.start();
        }


        // STEP 1
        // some useful variables to reader Threads,
        // must be done this way since the variables are not final
        // and therefore cannot be passed as parameters
        for (String policy: policies) {
            startStep(semaphores, policy, 'A');

            // send the outputFile path
            semaphores.get(policy)[1].acquire();
            if (debug == true) System.out.println("MAIN: Sending " + writeFile + " to " + policy);
            writePipes.get(policy).write(writeFile.getBytes(), 0, writeFile.length());
            semaphores.get(policy)[0].release();

            // send the chosen starting track
            semaphores.get(policy)[1].acquire();
            if (debug == true) System.out.println("MAIN: Sending startPoint to " + policy);
            writePipes.get(policy).write(startPoint);
            semaphores.get(policy)[0].release();

            // send the number of lines to read at a time
            semaphores.get(policy)[1].acquire();
                if (debug == true) System.out.println("MAIN: Sending batch number (" + batch + ") to " + policy);
            writePipes.get(policy).write(batch);
            semaphores.get(policy)[0].release();

            // send the verbose status
            semaphores.get(policy)[1].acquire();
                if (debug == true) System.out.println("MAIN: Sending debug=" + debug + " to " + policy);
            if (debug == true) {
                writePipes.get(policy).write(1);
            } else {
                writePipes.get(policy).write(0);
            }
            semaphores.get(policy)[0].release();

            endStep(semaphores, policy, 'A');
        }


        // STEP 3
        // read each line of the input file and send that number to each policy's reader thread
        try {
            int count = 1;
            BufferedReader reader = new BufferedReader(new FileReader(readFile));
            while (reader.ready() == true) {
                // read <batch> numbers from the file and send them to the reader threads
                for (int i = 0; i < batch; i++) {
                    int next = 0;
                    next = Integer.parseInt(reader.readLine());
                    for (String policy: policies) {
                        startStep(semaphores, policy, 'B');
                        semaphores.get(policy)[1].acquire();
                        if (debug == true) System.out.println("(" + count + ") MAIN: Sending (" + next + ") to " + policy);                                              
                        writePipes.get(policy).write(next);
                        semaphores.get(policy)[0].release();
                        endStep(semaphores, policy, 'B');
                    }
                    count++;
                }
            }    
            if (debug == true) System.out.println("MAIN: We're done reading the file, closing file reader"); 
            reader.close();
            

            // STEP 8
            // read the results the policy threads pipe out and log them
            count = 1;
            String message = "";
            while (count < 1000) {
                for (String policy: policies) {
                    startStep(semaphores, policy, 'A');
                    semaphores.get(policy)[0].acquire();
                    int nextTrack = readPipes.get(policy).read();
                    if (debug == true) System.out.println("(" + count + ") MAIN: Receiving (" + nextTrack + ") next track from " + policy);
                    semaphores.get(policy)[1].release();

                    semaphores.get(policy)[0].acquire();
                    int moves = readPipes.get(policy).read();
                    if (debug == true) System.out.println("(" + count + ") MAIN: Receiving (" + moves + ") number of moves from " + policy);
                    semaphores.get(policy)[1].release(); 

                    message = message + String.format("%s%" + (1+(policy.length()-1)) + "d%" + (policy.length()/3) + "s%" + (policy.length()-1) + "d", "|", nextTrack , "|",  moves);
                    endStep(semaphores, policy, 'A');
                }
                message = message + "|\n";
                logLine(writeFile, message);
                count++;
                message = "";
            }
            logLine(writeFile, divider);

            // get and log the average seek times and moves
            for (String policy: policies) {
                startStep(semaphores, policy, 'B');
                semaphores.get(policy)[0].acquire();
                int averageTimes = readPipes.get(policy).read();
                if (debug == true) System.out.println("MAIN: Receiving (" + averageTimes + ") average seek time from " + policy);
                semaphores.get(policy)[1].release(); 
                message = message + String.format("%s%" + (1+((policy.length()-1) + (policy.length()/3) + (policy.length()-1))) + "s", "|", averageTimes + " ns");

            }
            message = message + "|\n";
            logLine(writeFile, message);
            logLine(writeFile, divider);
            message = "";

            for (String policy: policies) {
                semaphores.get(policy)[0].acquire();
                int averageMoves = readPipes.get(policy).read();
                if (debug == true) System.out.println("MAIN: Receiving (" + averageMoves + ") average move count from " + policy);
                semaphores.get(policy)[1].release(); 
                message = message + String.format("%s%" + (1+((policy.length()-1) + (policy.length()/3) + (policy.length()-1))) + "s", "|", averageMoves + " moves");
                endStep(semaphores, policy, 'B');
            }
            message = message + "|\n";
            logLine(writeFile, message);
            logLine(writeFile, divider);



        } catch (FileNotFoundException fnf) {
            System.err.println("The specified file cannot be found!");
        } catch (IOException ioe) {
            System.err.println("There was an I/O error!");
        } catch (Exception e) {
            System.err.println("There was an error.");
        }
    }

    private static File generateNumbers(String method, File file) {
        try {
            int numbers[] = new int[1000];
            new Random();
            // the first number is always random
            numbers[0] = ThreadLocalRandom.current().nextInt(1, 201);
            for (int i = 1; i < 1000; i++) {
                switch (method) {
                    case "random":
                        numbers[i] = ThreadLocalRandom.current().nextInt(1, 201);
                        break;
                    case "alternate":
                        // 10% chance of being the same track as previous
                        // 1% chance of being the furthest track possible from previous
                        // decreasing linear probability of being further as we go further
                        for (int j = 1; j < 100; j++) {
                            // make a probability number 1-1000
                            int next = ThreadLocalRandom.current().nextInt(1, 1001);
                            if (next > 450 && next <= 550) {
                                // center 10% of the numbers result in the same number as last
                                numbers[i] = (numbers[i-1]);
                            } else {
                                // TODO: the other stuff
                            }
                        }
                        break;
                }
            }

            PrintWriter out = new PrintWriter(file);
            for (int num: numbers) {
                out.println(num);
            }
            out.close();
        } catch (FileNotFoundException fnfe) {
            System.out.println("The specified file cannot be found!");
        }
        return file;
    }

    private static String moveHead(int currentTrack, int moveToTrack, String direction) {
        
        int moves = 0;
        if (direction.equals("any")) {
            if (((200 - moveToTrack) + currentTrack) < (moveToTrack - currentTrack)) {
                // if wrapping around the bottom is faster than going up the ladder
                direction = "down";
            } 
            if (((200 - currentTrack) + moveToTrack) < (currentTrack - moveToTrack)) {
                // if wrapping around the top is faster than going down the ladder
                direction = "up";
            }
        }
        long startTime = System.nanoTime();
        while (currentTrack != moveToTrack) {
            switch (direction) {
                case "up":
                    if (moveToTrack > currentTrack) {
                        currentTrack++;
                    } else if (currentTrack < 200) {
                        currentTrack++;
                    } else if (currentTrack == 200) {
                        currentTrack = 1;
                    }
                    break;
                case "down":
                    if (moveToTrack < currentTrack) {
                        currentTrack--;
                    } else if (currentTrack > 1) {
                        currentTrack--;
                    } else if (currentTrack == 1) {
                        currentTrack = 200;
                    }
                    break;
                case "any":
                    if (moveToTrack > currentTrack) {
                        currentTrack++;
                    } else {
                        currentTrack--;
                    }
                    break;
            }
            moves++;
        }
        double time = (System.nanoTime() - startTime);
        return String.format("%d%s%.4f", moves, ",", time);
    }

    private static void printHelpDialog() {
        System.out.println("Possible arguments are as follows:");
        System.out.printf("%-15s %s%n", "-h --help", "shows the help dialog (you are here)");
        System.out.printf("%-15s %s%n", "-i --input", "(REQUIRED) file that contains the 'next track' numbers, one per line");
        System.out.printf("%-15s %s%n", "-g --generate", "generate a new set of 1000 random numbers to be used as 'next track' numbers");
        System.out.printf("%-15s %s%n", "",  "Possible methods are random and alternate");
        System.out.printf("%-15s %s%n", "",  "if used with -i will replace the file specified with -i");
        System.out.printf("%-15s %s%n", "",  "otherwise defaults to input.txt");
        System.out.printf("%-15s %s%n", "-o --output", "specifies the output file to which to write the log tables");
        System.out.printf("%-15s %s%n", "",  "defaults to output.txt if not provided");
        System.out.printf("%-15s %s%n", "-s --start", "a number between 1-200 that specifies the on which track to begin");
        System.out.printf("%-15s %s%n", "", "defaults to 100");
        System.out.printf("%-15s %s%n", "-b --batch", "a positive number, how many tracks to send to the policies at a time");
        System.out.printf("%-15s %s%n", "", "defaults to 5");
        System.out.printf("%-15s %s%n", "-p --policy", "(REQUIRED) the disk cheduling polic(ies) to use. ");
        System.out.printf("%-15s %s%n", "", "Possible policies are FIFO, LIFO, SSTF, SCAN, C-SCAN, N-STEP-SCAN, and FSCAN");
        System.out.printf("%-15s %s%n", "", "to use multiple, use commas but no spaces (e.g. 'FIFO,C-SCAN,SSTF')");
        System.out.printf("%-15s %s%n", "-v --verbose", "Enable debug mode, where the system will tell you way more information than you need.");
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

    public static String centerText(String text, int len){
        String out = String.format("%"+len+"s%s%"+len+"s", "",text,"");
        float mid = (out.length()/2);
        float start = mid - (len/2);
        float end = start + len; 
        return out.substring((int)start, (int)end);
    }
    
    private static void startStep(Map<String, Semaphore[]> semaphores, String policy, char side) {
        try {
            if (side == 'A') {
                semaphores.get(policy)[2].acquire();
            } else if (side == 'B') {
                semaphores.get(policy)[3].acquire();
            }
        } catch (InterruptedException ie) {
            System.err.println("Starting the next step was interrupted!");
        }
    }
    private static void endStep(Map<String, Semaphore[]> semaphores, String policy, char side) {
        if (side == 'A') {
            semaphores.get(policy)[2].release();
        } else if (side == 'B') {
            semaphores.get(policy)[3].release();
        }
    }
    


    private static void startNewThread(String policy, Map<String, Semaphore[]> semaphores, Map<String, PipedInputStream> readPipes, Map<String, PipedOutputStream> writePipes) {

        // STEP 2
        // get variables that we will need
        String writeFile = "";
        int startPoint = 0;
        int batch = 0;
        boolean debug = false;
        try {
            startStep(semaphores, policy, 'B');

            // get the path to the outputFile
            semaphores.get(policy)[0].acquire();
            while (!(writeFile.endsWith("txt"))) {
                writeFile = writeFile + (char) readPipes.get(policy).read();
            }
            semaphores.get(policy)[1].release();

            // get the starting track
            semaphores.get(policy)[0].acquire();
            startPoint = readPipes.get(policy).read();
            semaphores.get(policy)[1].release(); 

            // get the number of lines to process at a time
            semaphores.get(policy)[0].acquire();
            batch = readPipes.get(policy).read();
            semaphores.get(policy)[1].release();

            // get verbose status
            semaphores.get(policy)[0].acquire();
            int temp = readPipes.get(policy).read();
            if (temp == 1) debug = true;
            if (debug == true) {
                System.out.println(policy + ": received " + writeFile);
                System.out.println(policy + ": received (" + startPoint + ") as startPoint");
                System.out.println(policy + ": received (" + batch + ") as batch number");
                System.out.println(policy + ": received debug=" + debug);
            }
            semaphores.get(policy)[1].release(); 

            endStep(semaphores, policy, 'B');
        } catch (IOException ioe) {
        } catch (InterruptedException ie) {}


        int tracks[] = new int[1000];
        startStep(semaphores, policy, 'A');
        for (int count = 1; count <= 1000; ) {
            // STEP 4
            // get the next set of tracks from the main thread
            for (int i = 0; i < batch; i++) {
                try { 
                    semaphores.get(policy)[0].acquire();
                    tracks[count-1] = readPipes.get(policy).read();
                    if (debug == true) System.out.println("(" + count + ") " + policy + ": Receiving (" + tracks[count-1] + ") from MAIN");                                 
                    semaphores.get(policy)[1].release();  
                } catch (IOException e) {
                } catch (InterruptedException ie) {}    
                count++;       
            } 
        }
        endStep(semaphores, policy, 'A');
        if (debug == true) System.out.println(policy + ": finished reading the input"); 
            
        int currentTrack = startPoint;
        for (int count = 0; count < 1000; ) {
            // STEP 5
            // sort the tracks in batches of size <batch>
            // THIS IS WHERE THE POLICIES DIFFER
            
            if (count != 0) {
                currentTrack = tracks[count-1];
            }
            int batchSet[] = new int[batch];
            for (int i = 0; i < batch; i++) {
                batchSet[i] = tracks[count];
                count++;
            }
            count -= batch;
            switch (policy) {
                case "FIFO":
                    batchSet = sortFIFO(batchSet);
                    break;
                case "LIFO":
                    batchSet = sortLIFO(batchSet);
                    break;
                case "SSTF":
                    batchSet = sortSSTF(currentTrack, batchSet);
                    break;
                case "SCAN":
                    batchSet = sortSSTF(currentTrack, batchSet);
                    break;
                case "C-SCAN":
                    batchSet = sortSSTF(currentTrack, batchSet);
                    break;
                case "N-STEP-SCAN":
                    batchSet = sortSSTF(currentTrack, batchSet);
                    break;
                case "FSCAN":
                    batchSet = sortSSTF(currentTrack, batchSet);
                    break;
                // TODO: others
            }
            for (int i = 0; i < batch; i++) {
                tracks[count] = batchSet[i];
                count++;
            }
        }

        // STEP 6
        // the processing of the next tracks
        int count = 1;
        currentTrack = startPoint;
        int moves[] = new int[1000];
        double times[] = new double[1000];
        while (count < 1000) {
            try {
                for (int i = 0; i < batch; i++) {
                    // move the head to the next track from the current 
                    String results[] = moveHead(currentTrack, tracks[count-1], "any").split(",");
                    // record how many moves it took
                    moves[count-1] = Integer.parseInt(results[0]);
                    // and how long it took
                    times[count-1] += Double.parseDouble(results[1]);
                    if (debug == true) System.out.println("(" + count + ") " + policy + ": moving from (" + currentTrack + ") to (" + tracks[count-1] + ") took " + moves[count-1] + " moves and " + times[count-1] + "ns"); 
                currentTrack = tracks[count-1];
                count++;
                }
            } catch (Exception e) {}
        }
        
        startStep(semaphores, policy, 'B');
        count = 1;
        while (count < 1000) {
            try {
                // STEP 7
                // send the results to the parent thread
                // send nextTrack to master thread
                semaphores.get(policy)[1].acquire();
                writePipes.get(policy).write(tracks[count-1]);
                semaphores.get(policy)[0].release();
                // send moves to master thread
                semaphores.get(policy)[1].acquire();
                writePipes.get(policy).write(moves[count-1]);
                semaphores.get(policy)[0].release();
            } catch (Exception e) {}
            count++;
        }
        endStep(semaphores, policy, 'B');

        double sumOfSeekTimes = 0;
        int sumOfSeekLengths = 0;
        for (int i = 0; i< times.length; i++) {
            sumOfSeekTimes += times[i];
            sumOfSeekLengths += moves[i];
        }
        int averageTimes = (int) sumOfSeekTimes/1000;
        int averageMoves = (int) sumOfSeekLengths/1000;
        try {
            startStep(semaphores, policy, 'A');
            semaphores.get(policy)[1].acquire();
            writePipes.get(policy).write(averageTimes);
            semaphores.get(policy)[0].release();
            if (debug == true) System.out.println(policy + ": Avg time: " + averageTimes + " ns");
            
            semaphores.get(policy)[1].acquire();
            writePipes.get(policy).write(averageMoves);
            semaphores.get(policy)[0].release();
            if (debug == true) System.out.println(policy + ": Avg moves: " + averageMoves);
            endStep(semaphores, policy, 'A');
        } catch (Exception e) {}

        if (debug == true) System.out.println(policy + " is closed");
    }

    private static int[] sortFIFO(int[] tracks) {
        return tracks;
    }

    private static int[] sortLIFO(int[] tracks) {
        int newTracks[] = new int[tracks.length];
        for (int i = 0; i < tracks.length; i++) {
            newTracks[i] = tracks[(tracks.length-1) - i];
        }
        return newTracks;
    }
    
    private static int[] sortSSTF(int currentTrack, int[] tracks) {
        int newTracks[] = new int[tracks.length];
        for (int i = 0; i < tracks.length; i++) {
            int moves[] = new int[tracks.length];
            int leastMoves = 999;
            int leastIndex = 999;
            for (int j = 0; j < (tracks.length - i); j++) {
                moves[j] = Integer.parseInt(moveHead(currentTrack, tracks[j], "any").split(",")[0]);
            }
            for (int j = 0; j < (tracks.length - i); j++) {
                if (moves[j] < leastMoves) {
                    leastMoves = moves[j];
                    leastIndex = j;
                }
            }
            newTracks[i] = tracks[leastIndex];
            currentTrack = newTracks[i];
            for (int j = leastIndex; j < (tracks.length-1); j++) {
                tracks[j] = tracks[j+1];
            }
        }
        return newTracks;
    }

    private static int[] sortSCAN(int currentTrack, int[] tracks) {
		int [] scheduleSequence;
        int newTracks[] = new int[tracks.length];

		//sort arrays in acending order 
		Arrays.sort(newTracks, 0, newTracks.length);


	    scheduleSequence = new int[newTracks.length];
		int nextSequenceIndex = 0;
		int startPoint = 0;


		// get the first element in array that is smaller then the currentTrack
		for (int i=0; i<newTracks.length; i++)
		{
			if (newTracks[i]>currentTrack)
			{
				startPoint = i-1;
				break;
			}
		}

		for (int i = startPoint; i>=0; i--)
		{
			scheduleSequence[nextSequenceIndex++] = newTracks[i];
		}


		scheduleSequence[nextSequenceIndex++] = 0;

		for (int i = startPoint+1; i<newTracks.length; i++)
		{
			scheduleSequence[nextSequenceIndex++] = newTracks[i];
		}

		return scheduleSequence;

    }

    private static int[] sortCSCAN(int[] tracks) {
        // TODO: implement the real deal
        return tracks;
    }

    private static int[] sortNSTEPSCAN(int[] tracks) {
        // TODO: implement the real deal
        return tracks;
    }

    private static int[] sortFSCAN(int[] tracks) {
        // TODO: implement the real deal
        return tracks;
    }
}