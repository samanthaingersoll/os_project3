package os_project3;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.io.*;

public class Random1000{
	
	public static void main(String [] args)throws IOException
	   {
		int max= 200;
		int min = 0;
	      PrintWriter out = new PrintWriter(new File("random.txt"));
	      new Random();
	      int number, count=0;
	      while(count<=999)
	      {
	         
	            number= ThreadLocalRandom.current().nextInt(min, max + 1);
	            out.print(number);
	            count++;
	         
	         out.println();
	      }
	      out.close();
	  }
}
	


