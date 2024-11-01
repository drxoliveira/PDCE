package mhpdce.resources;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class WriterFile {

    private FileWriter fileWriter;
    private PrintWriter printWriterFile;

    public void createWriterFile (String fileLocation) {
        try {
            this.fileWriter = new FileWriter(fileLocation);
            this.printWriterFile = new PrintWriter (this.fileWriter);
        } catch ( IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
    }

    public void printWriterFile (double value) {
        this.printWriterFile.printf("%.4f",value);
        this.printWriterFile.println();
    }

    public void printWriterFile (List<Integer> vertex) {
        this.printWriterFile.printf("%s",vertex.toString());
        this.printWriterFile.println();
    }

    public void printWriterFile (double [] values) {

        for (Double value: values) {
            this.printWriterFile.printf("%.4f",value);
            this.printWriterFile.println();
        }
    }

    public void closeWriterFile() {
        try {
            this.fileWriter.close();
            this.printWriterFile.close();
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
    }

}
