package mhpdce.main;

import mhpdce.method.EllipticScan;
import mhpdce.model.Solution;
import mhpdce.model.StudyMap;
import mhpdce.resources.ReadFile;
import mhpdce.resources.Resource;
import mhpdce.resources.WriterFile;

import java.util.Locale;

public class MainEllScanSimulationsMG {

    public static void main (String [] args) {

        Locale.setDefault(Locale.US);
        System.out.println("Scan Elliptic for Irregular Spatial Cluster Detection");

        int numberSimulationsH0 = 999;

        String prefix = "../data/";
        WriterFile writerFile = new WriterFile();
        writerFile.createWriterFile("Map_MG_Chagas_Simulations.txt");

        ReadFile manageFile = new ReadFile(prefix + "Map_MG_Chagas.txt");
        StudyMap studyMap = manageFile.generateStudyMap();

        double [] eccentricity = {1.0, 1.5, 2.0, 3.0, 4.0};
        int [] numberAngles = {1, 4, 6, 9, 12};
        double windowSize = 0.20;
        boolean verbose = false;

        System.out.println("Simulation H1");

        EllipticScan ellScan = new EllipticScan(studyMap, eccentricity, numberAngles, windowSize, verbose);

        long startTime = System.currentTimeMillis();
        ellScan.execute();
        double runTime = (double)(System.currentTimeMillis() - startTime) / 1000.0;

        Solution solution = new Solution();
        solution.copy(ellScan.getBestSolution());

        System.out.println(solution);

        writerFile.printWriterFile(solution.getVertices());
        writerFile.printWriterFile(solution.getScanStatisticsValue());
        writerFile.printWriterFile(runTime);

        String fileCases = prefix + "Map_MG_Chagas_H0_999.txt";

        for (int i = 0; i < numberSimulationsH0; i++) {

            System.out.println("Simulation H0: " + (i + 1));

            StudyMap studyMapH0 = Resource.loadFileCases(studyMap, fileCases, i);

            ellScan = new EllipticScan(studyMapH0, eccentricity, numberAngles, windowSize, verbose);

            startTime = System.currentTimeMillis();
            ellScan.execute();
            runTime = (double)(System.currentTimeMillis() - startTime) / 1000.0;

            solution = new Solution();
            solution.copy(ellScan.getBestSolution());

            writerFile.printWriterFile(solution.getVertices());
            writerFile.printWriterFile(solution.getScanStatisticsValue());
            writerFile.printWriterFile(runTime);

        }

        writerFile.closeWriterFile();
    }

}
