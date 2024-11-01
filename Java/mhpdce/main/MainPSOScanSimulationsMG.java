package mhpdce.main;

import mhpdce.metaheuristic.singleobjective.ModifiedBinaryParticleSwarmOptimization;
import mhpdce.model.Solution;
import mhpdce.model.StudyMap;
import mhpdce.resources.ReadFile;
import mhpdce.resources.Resource;
import mhpdce.resources.WriterFile;

import java.util.Locale;

public class MainPSOScanSimulationsMG {

    public static void main (String [] args) {

        Locale.setDefault(Locale.US);
        System.out.println("MBPSO for Irregular Spatial Cluster Detection");

        int numberSimulationsH0 = 999;

        String prefix = "../data/";
        WriterFile writerFile = new WriterFile();
        writerFile.createWriterFile("Map_MG_Chagas_Simulations.txt");

        ReadFile manageFile = new ReadFile(prefix + "Map_MG_Chagas.txt");
        StudyMap studyMap = manageFile.generateStudyMap();

        int ni = 1000;

        int np = studyMap.getTotalUnits();
        int nd = studyMap.getTotalUnits();

        double phi = 2.01;
        double w = 1.00 / ((phi -1.00) + Math.sqrt(phi * phi -2.00 * phi));

        double gMax = 8.00;
        double gMin = -8.00;

        double c1 = phi * w;
        double c2 = phi * w;

        double gr = 0.03;

        double trMax = 0.10;
        double trMin = 0.10;

        double sMax = 0.20;

        System.out.println("Simulation H1");

        ModifiedBinaryParticleSwarmOptimization pSOScan = new ModifiedBinaryParticleSwarmOptimization(studyMap,
                ni, np, nd, w, gMax, gMin, c1, c2, gr, trMax, trMin, sMax, false);

        long startTime = System.currentTimeMillis();
        pSOScan.execute ();
        double runTime = (double)(System.currentTimeMillis() - startTime) / 1000.0;

        Solution solution = new Solution();
        solution.copy(pSOScan.getGBest());

        System.out.println(solution);

        writerFile.printWriterFile(solution.getVertices());
        writerFile.printWriterFile(solution.getScanStatisticsValue());
        writerFile.printWriterFile(runTime);

        String fileCases = prefix + "Map_MG_Chagas_H0_999.txt";

        for (int i = 0; i < numberSimulationsH0; i++) {

            System.out.println("Simulation H0: " + (i + 1));

            StudyMap studyMapH0 = Resource.loadFileCases(studyMap, fileCases, i);

            pSOScan = new ModifiedBinaryParticleSwarmOptimization(studyMapH0,
                    ni, np, nd, w, gMax, gMin, c1, c2, gr, trMax, trMin, sMax, false);

            startTime = System.currentTimeMillis();
            pSOScan.execute ();
            runTime = (double)(System.currentTimeMillis() - startTime) / 1000.0;

            solution = new Solution();
            solution.copy(pSOScan.getGBest());

            writerFile.printWriterFile(solution.getVertices());
            writerFile.printWriterFile(solution.getScanStatisticsValue());
            writerFile.printWriterFile(runTime);

        }

        writerFile.closeWriterFile();
    }

}
