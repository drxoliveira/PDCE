package mhpdce.main;

import mhpdce.metaheuristic.singleobjective.ModifiedBinaryParticleSwarmOptimization;
import mhpdce.model.Solution;
import mhpdce.model.StudyMap;
import mhpdce.resources.Function;
import mhpdce.resources.ReadFile;
import mhpdce.resources.Resource;
import mhpdce.resources.WriterFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainPSOScanSimulationsNE {

    public static List<Solution> executeNullHypothesisSimulations(StudyMap studyMap, String fileCases,
                                                                  int numberSimulations) {

        System.out.println("Null Hypothesis Simulations.");

        int ni = 1000;

        int np = studyMap.getTotalUnits();
        int nd = studyMap.getTotalUnits();

        double phi = 2.01;
        double w = 1.00 / ((phi -1.00) + Math.sqrt(phi * phi -2.00 * phi));

        double gMax =  8.00;
        double gMin = -8.00;

        double c1 = phi * w;
        double c2 = phi * w;

        double gr = 0.03;

        double trMax = 0.10;
        double trMin = 0.10;

        double sMax = 0.20;

        List<Solution> solutionListH0 = new ArrayList<>();
        double [] runTimeArray = new double[numberSimulations];

        for(int simulation = 0; simulation < numberSimulations; simulation++) {

            if (simulation % 1000 == 0) System.out.println("Simulation: " + (simulation + 1));

            StudyMap studyMapH0 = Resource.loadFileCases(studyMap, fileCases, simulation);

            ModifiedBinaryParticleSwarmOptimization pSOScan = new ModifiedBinaryParticleSwarmOptimization(studyMapH0,
                    ni, np, nd, w, gMax, gMin, c1, c2, gr, trMax, trMin, sMax, false);

            long startTime = System.currentTimeMillis();
            pSOScan.execute ();
            runTimeArray[simulation] = (double) (System.currentTimeMillis() - startTime) / 1000.0;

            Solution bestSolution = new Solution();
            bestSolution.copy(pSOScan.getGBest());
            bestSolution.setVertices(null);

            solutionListH0.add(bestSolution);

        }

        String result = String.format("Time Average: %.4f; Time Standard Deviation: %.4f.",
                Function.average(runTimeArray), Function.standardDeviation(runTimeArray) );
        System.out.println(result);

        return solutionListH0;

    }

    public static void executeAlternativeHypothesisSimulations(StudyMap studyMap, String fileCases, int numberSimulations,
                                                               List<Solution> solutionListH0, int [] cluster, String label) {

        System.out.println("-------------------------------------------------------");
        System.out.println("Alternative Hypothesis Simulations: " + fileCases);

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

        List<Solution> solutionListH1 = new ArrayList<>();
        double [] runTimeArray = new double[numberSimulations];

        for(int simulation = 0; simulation < numberSimulations; simulation++) {

            if (simulation % 1000 == 0) System.out.println("Simulation: " + (simulation + 1));

            StudyMap studyMapH1 = Resource.loadFileCases(studyMap, fileCases, simulation);

            ModifiedBinaryParticleSwarmOptimization pSOScan = new ModifiedBinaryParticleSwarmOptimization(studyMapH1,
                    ni, np, nd, w, gMax, gMin, c1, c2, gr, trMax, trMin, sMax, false);

            long startTime = System.currentTimeMillis();
            pSOScan.execute ();
            runTimeArray[simulation] = (double) (System.currentTimeMillis() - startTime) / 1000.0;

            Solution bestSolution = new Solution();
            bestSolution.copy(pSOScan.getGBest());
            solutionListH1.add(bestSolution);

        }

        String prefix = "";

        String result = String.format("Time Average: %.4f; Time Standard Deviation: %.4f",
                Function.average(runTimeArray), Function.standardDeviation(runTimeArray) );
        System.out.println(result);

        double [] power = Resource.calculatePowerDetection(solutionListH0, solutionListH1, 95);

        result = String.format("Power Average: %.4f; Power Standard Deviation: %.4f; Power Standard Error: %.4f.",
                Function.average(power), Function.standardDeviation(power), Function.standardError(power));
        System.out.println(result);

        WriterFile writerFile = new WriterFile();
        writerFile.createWriterFile(prefix + "Power_" + label + ".txt");
        writerFile.printWriterFile(power);
        writerFile.closeWriterFile();

        double [] precision = Resource.calculatePrecision(studyMap, solutionListH0, solutionListH1, cluster, 95);

        result = String.format("Precision Average: %.4f; Precision Standard Deviation: %.4f; Precision Standard Error: %.4f.",
                Function.average(precision), Function.standardDeviation(precision), Function.standardError(precision));
        System.out.println(result);

        writerFile.createWriterFile(prefix + "Precision_" + label + ".txt");
        writerFile.printWriterFile(precision);
        writerFile.closeWriterFile();

        double [] recall = Resource.calculateRecall(studyMap, solutionListH0, solutionListH1, cluster, 95);

        result = String.format("Recall Average: %.4f; Recall Standard Deviation: %.4f; Recall Standard Error: %.4f.",
                Function.average(recall), Function.standardDeviation(recall), Function.standardError(recall));
        System.out.println(result);

        writerFile.createWriterFile(prefix + "Recall_" + label + ".txt");
        writerFile.printWriterFile(recall);
        writerFile.closeWriterFile();

        double [] accuracy = Resource.calculateAccuracy(studyMap, solutionListH0, solutionListH1, cluster, 95);

        result = String.format("Accuracy Average: %.4f; Accuracy Standard Deviation: %.4f; Accuracy Standard Error: %.4f.",
                Function.average(accuracy), Function.standardDeviation(accuracy), Function.standardError(accuracy));
        System.out.println(result);

        writerFile.createWriterFile(prefix + "Accuracy_" + label + ".txt");
        writerFile.printWriterFile(accuracy);
        writerFile.closeWriterFile();

        double [] fScore = Resource.calculateFScore(studyMap, solutionListH0, solutionListH1, cluster, 95);

        result = String.format("F-Score Average: %.4f; F-Score Standard Deviation: %.4f; F-Score Standard Error: %.4f.",
                Function.average(fScore), Function.standardDeviation(fScore), Function.standardError(fScore));
        System.out.println(result);

        writerFile.createWriterFile(prefix + "FScore_" + label + ".txt");
        writerFile.printWriterFile(fScore);
        writerFile.closeWriterFile();

    }

    public static void main (String [] args) {

        Locale.setDefault(Locale.US);
        System.out.println("Simulations MBPSO Artificial Clusters NE Map.");

        String prefix = "../data/";
        ReadFile manageFile = new ReadFile(prefix + "Map_NE.txt");

        StudyMap studyMap = manageFile.generateStudyMap();
        studyMap.setCases(600);

        int numberSimulationsH0 = 10000;
        int numberSimulationsH1 = 5000;

        // Null Hypothesis Simulations.
        String fileCases = prefix + "NE_H0.txt";
        List<Solution> solutionListH0 = executeNullHypothesisSimulations(studyMap, fileCases, numberSimulationsH0);

        // Alternative Hypothesis Simulations: Cluster A.
        fileCases = prefix + "NE_A.txt";
        int [] clusterA = {2, 4, 18, 19, 20, 69, 70, 71, 76, 234, 236, 240, 244, 245};
        executeAlternativeHypothesisSimulations(studyMap, fileCases, numberSimulationsH1, solutionListH0, clusterA, "A");

        // Alternative Hypothesis Simulations: Cluster B.
        fileCases = prefix + "NE_B.txt";
        int [] clusterB = {98, 108, 111, 115, 117, 118, 133, 137, 139, 141, 142, 144, 153, 154, 155, 157};
        executeAlternativeHypothesisSimulations(studyMap, fileCases, numberSimulationsH1, solutionListH0, clusterB, "B");

        // Alternative Hypothesis Simulations: Cluster C.
        fileCases = prefix + "NE_C.txt";
        int [] clusterC = {103, 120, 125, 129, 134, 135, 156};
        executeAlternativeHypothesisSimulations(studyMap, fileCases, numberSimulationsH1, solutionListH0, clusterC, "C");

        // Alternative Hypothesis Simulations: Cluster D.
        fileCases = prefix + "NE_D.txt";
        int [] clusterD = {34, 39, 170, 173, 176, 177, 180, 181, 195, 200, 208, 209, 214, 219, 226};
        executeAlternativeHypothesisSimulations(studyMap, fileCases, numberSimulationsH1, solutionListH0, clusterD, "D");

        // Alternative Hypothesis Simulations: Cluster E.
        fileCases = prefix + "NE_E.txt";
        int [] clusterE = {34, 39, 101, 151, 167, 170, 173, 176, 177, 180, 181, 195, 199, 200, 206, 208, 209, 214, 219, 225, 226};
        executeAlternativeHypothesisSimulations(studyMap, fileCases, numberSimulationsH1, solutionListH0, clusterE, "E");

        // Alternative Hypothesis Simulations: Cluster F.
        fileCases = prefix + "NE_F.txt";
        int [] clusterF = {1, 4, 5, 6, 13, 15, 16, 17, 22, 23, 24, 25, 53, 55, 57, 58, 62, 64, 65, 66, 74, 229, 231};
        executeAlternativeHypothesisSimulations(studyMap, fileCases, numberSimulationsH1, solutionListH0, clusterF, "F");

        // Alternative Hypothesis Simulations: Cluster BOS.
        fileCases = prefix + "NE_BOS.txt";
        int [] clusterBOS = {13, 15, 17, 19, 21, 23, 24, 25, 26, 230};
        executeAlternativeHypothesisSimulations(studyMap, fileCases, numberSimulationsH1, solutionListH0, clusterBOS, "BOS");

        // Alternative Hypothesis Simulations: Cluster NYC.
        fileCases = prefix + "NE_NYC.txt";
        int [] clusterNYC = {1, 5, 78, 79, 83, 85, 88, 89, 90, 91, 92, 96, 100, 121, 127, 128, 138, 140, 149, 157};
        executeAlternativeHypothesisSimulations(studyMap, fileCases, numberSimulationsH1, solutionListH0, clusterNYC, "NYC");

        // Alternative Hypothesis Simulations: Cluster WAS.
        fileCases = prefix + "NE_WAS.txt";
        int [] clusterWAS = {9, 28, 29, 30, 31, 33, 35, 37, 40, 42, 43, 46, 48};
        executeAlternativeHypothesisSimulations(studyMap, fileCases, numberSimulationsH1, solutionListH0, clusterWAS, "WAS");

    }

}
