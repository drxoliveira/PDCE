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

public class MainPSOScanSimulationsPopulationPattern01 {

    public static List<Solution> executeNullHypothesisSimulations(StudyMap studyMap, String fileCases, int numberSimulations) {

        System.out.println("Null Hypothesis Simulations.");

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

        double gMax =  8.00;
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

        String result = String.format("Time Average: %.4f; Time Standard Deviation: %.4f",
                Function.average(runTimeArray), Function.standardDeviation(runTimeArray) );
        System.out.println(result);

        double [] power = Resource.calculatePowerDetection(solutionListH0, solutionListH1, 95);

        result = String.format("Power Average: %.4f; Power Standard Deviation: %.4f; Power Standard Error: %.4f.",
                Function.average(power), Function.standardDeviation(power), Function.standardError(power));
        System.out.println(result);

        WriterFile writerFile = new WriterFile();
        writerFile.createWriterFile("Power_Model02_" + label + ".txt");
        writerFile.printWriterFile(power);
        writerFile.closeWriterFile();

        double [] precision = Resource.calculatePrecision(studyMap, solutionListH0, solutionListH1, cluster, 95);

        result = String.format("Precision Average: %.4f; Precision Standard Deviation: %.4f; Precision Standard Error: %.4f.",
                Function.average(precision), Function.standardDeviation(precision), Function.standardError(precision));
        System.out.println(result);

        writerFile.createWriterFile("Precision_Model02_" + label + ".txt");
        writerFile.printWriterFile(precision);
        writerFile.closeWriterFile();

        double [] recall = Resource.calculateRecall(studyMap, solutionListH0, solutionListH1, cluster, 95);

        result = String.format("Recall Average: %.4f; Recall Standard Deviation: %.4f; Recall Standard Error: %.4f.",
                Function.average(recall), Function.standardDeviation(recall), Function.standardError(recall));
        System.out.println(result);

        writerFile.createWriterFile("Recall_Model02_" + label + ".txt");
        writerFile.printWriterFile(recall);
        writerFile.closeWriterFile();

        double [] accuracy = Resource.calculateAccuracy(studyMap, solutionListH0, solutionListH1, cluster, 95);

        result = String.format("Accuracy Average: %.4f; Accuracy Standard Deviation: %.4f; Accuracy Standard Error: %.4f.",
                Function.average(accuracy), Function.standardDeviation(accuracy), Function.standardError(accuracy));
        System.out.println(result);

        writerFile.createWriterFile("Accuracy_Model02_" + label + ".txt");
        writerFile.printWriterFile(accuracy);
        writerFile.closeWriterFile();

        double [] fScore = Resource.calculateFScore(studyMap, solutionListH0, solutionListH1, cluster, 95);

        result = String.format("F-Score Average: %.4f; F-Score Standard Deviation: %.4f; F-Score Standard Error: %.4f.",
                Function.average(fScore), Function.standardDeviation(fScore), Function.standardError(fScore));
        System.out.println(result);

        writerFile.createWriterFile("FScore_Model02_" + label + ".txt");
        writerFile.printWriterFile(fScore);
        writerFile.closeWriterFile();

    }

    public static void main (String [] args) {

        Locale.setDefault(Locale.US);
        System.out.println("Simulations MBPSO Artificial Clusters Population Pattern 02.");

        String prefix = "../data/";
        ReadFile manageFile = new ReadFile(prefix + "Map_Model02.txt");

        StudyMap studyMap = manageFile.generateStudyMap();
        studyMap.setCases(900);

        int numberSimulationsH0 = 10000;
        int numberSimulationsH1 = 5000;

        // Null Hypothesis Simulations: Cluster A.
        String fileCases = prefix + "Null_Hypothesis_Model02.txt";
        List<Solution> solutionListH0 = executeNullHypothesisSimulations(studyMap, fileCases, numberSimulationsH0);

        // Alternative Hypothesis Simulations: Cluster A.
        fileCases = prefix + "Cluster_A_Model02.txt";
        int [] clusterA = {28, 33, 34, 40, 45, 46, 52};
        executeAlternativeHypothesisSimulations(studyMap, fileCases, numberSimulationsH1, solutionListH0, clusterA, "A");

        // Alternative Hypothesis Simulations: Cluster B.
        fileCases = prefix + "Cluster_B_Model02.txt";
        int [] clusterB = {16, 21, 22, 28, 33, 34, 40, 45, 46, 52};
        executeAlternativeHypothesisSimulations(studyMap, fileCases, numberSimulationsH1, solutionListH0, clusterB, "B");

        // Alternative Hypothesis Simulations: Cluster C.
        fileCases = prefix + "Cluster_C_Model02.txt";
        int [] clusterC = {16, 28, 40, 52, 64, 76};
        executeAlternativeHypothesisSimulations(studyMap, fileCases, numberSimulationsH1, solutionListH0, clusterC, "C");

        // Alternative Hypothesis Simulations: Cluster D.
        fileCases = prefix + "Cluster_D_Model02.txt";
        int [] clusterD = {23, 29, 34, 40, 45, 51};
        executeAlternativeHypothesisSimulations(studyMap, fileCases, numberSimulationsH1, solutionListH0, clusterD, "D");

        // Alternative Hypothesis Simulations: Cluster E.
        fileCases = prefix + "Cluster_E_Model02.txt";
        int [] clusterE = {21, 23, 28, 29, 34, 40, 41, 45, 47};
        executeAlternativeHypothesisSimulations(studyMap, fileCases, numberSimulationsH1, solutionListH0, clusterE, "E");

        // Alternative Hypothesis Simulations: Cluster F.
        fileCases = prefix + "Cluster_F_Model02.txt";
        int [] clusterF = {28, 29, 34, 35, 40, 42, 52};
        executeAlternativeHypothesisSimulations(studyMap, fileCases, numberSimulationsH1, solutionListH0, clusterF, "F");

        // Alternative Hypothesis Simulations: Cluster G.
        fileCases = prefix + "Cluster_G_Model02.txt";
        int [] clusterG = {28, 33, 35, 40, 41, 45, 47, 53};
        executeAlternativeHypothesisSimulations(studyMap, fileCases, numberSimulationsH1, solutionListH0, clusterG, "G");

        // Alternative Hypothesis Simulations: Cluster H.
        fileCases = prefix + "Cluster_H_Model02.txt";
        int [] clusterH = {27, 30, 33, 35, 39, 42, 45, 47};
        executeAlternativeHypothesisSimulations(studyMap, fileCases, numberSimulationsH1, solutionListH0, clusterH, "H");

    }

}
