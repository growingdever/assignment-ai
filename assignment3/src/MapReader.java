import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by loki on 2015. 12. 8..
 */
public class MapReader {
    public static char[][] readWumpusWorld(int size, String gameboard) {
        char[][] newWorld = new char[size][size];

        try {
            //4 P,W,G,A
            File file = new File(gameboard);
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);

            for (int i = 0; i < size; i ++) {
                reader.readLine();

                String line1 = reader.readLine();
                if (line1.equals("")) {
                    i--;
                    continue;
                }

                // skip one line
                reader.readLine();

                int offset = 0;
                for (int j = 0; j < size; j ++) {
                    offset ++;

                    char breeze = line1.charAt(offset + 1);
                    newWorld[size - 1 - i][j] = breeze;

                    offset += 5;
                }
            }

            reader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return newWorld;
    }
}
