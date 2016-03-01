import java.util.Scanner;
import java.util.NoSuchElementException;
import java.lang.IllegalStateException;
	
public class IOValidator{

    protected Scanner in = new Scanner(System.in);

    public int inInt(String msg){
        int count = 0;
        int maxTries = 3;

        while(true) {
            try {

                System.out.print(msg);
                return in.nextInt();
                
            } catch (NoSuchElementException |  IllegalStateException e) {

            
                System.out.println("Input error[integer only], please retry:");
                
            }finally{
                
                in.nextLine();
                
            }
        }
    }

    public String inStr(String msg){
        int count = 0;
        int maxTries = 3;

        while(true) {
            try {

                in.reset();
                System.out.print(msg);
                return in.nextLine();
                
            } catch (NoSuchElementException |  IllegalStateException e) {

                System.out.println("Input error, please retry:");
            
            }
        }	
    }
}