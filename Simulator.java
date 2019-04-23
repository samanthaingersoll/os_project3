public class Simulator { 
    public static void main(String[] args) { 
        

        for (int i = 0; i < args.length; i++) {
            if (args[i].contains(".txt")) {
                //TODO: Import txt to array
                try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
                    stream.forEach(System.out::println);
                }
            } else {
                //TODO: call RNG to create next-track array
            }
        }
        

    }
}
