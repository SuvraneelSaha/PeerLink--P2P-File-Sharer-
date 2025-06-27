package P2P.utils;

import java.util.Random;

public class UploadUtils {

    public static int generateCode(){
        // port - generated port code
        int DYNAMIC_STARTING_PORT = 49152;
        int DYNAMIC_ENDING_PORT = 85535 ;
        // these two are unreserved ports which are not used by any applications ;
        // found from net

        Random random = new Random();
        return random.nextInt((DYNAMIC_ENDING_PORT-DYNAMIC_STARTING_PORT) + DYNAMIC_STARTING_PORT);
                // overflow is prevented

    }
}
