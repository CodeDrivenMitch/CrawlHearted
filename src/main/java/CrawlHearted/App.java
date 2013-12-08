package CrawlHearted;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        if(args.length == 0){
            System.out.println("No arguments given" );
        } else {
            for ( String s : args) {
                System.out.println("Argument: " + s);
            }
        }
    }
}
