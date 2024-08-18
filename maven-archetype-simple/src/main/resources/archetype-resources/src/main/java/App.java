package ${package};

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world! Application class.
 *
 */
public class App
{
    /**
     * Logger for the class.
     */
    //private static final Logger logger = Logger.getLogger(App.class.getName());
    private static final Logger logger = LoggerFactory.getLogger(App.class.getName());

    /**
     * Main method to start the application.
     * @param args Command line arguments
     */
    public static void main( String[] args )
    {
        logger.info( "Hello World!" );
    }
}
