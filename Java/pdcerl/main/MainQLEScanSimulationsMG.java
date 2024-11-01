package pdcerl.main;

import pdcerl.model.Solution;
import pdcerl.model.StudyMap;
import pdcerl.model.Unit;
import pdcerl.reinforcementlearning.singleobjective.QLearning;
import pdcerl.resources.ReadFile;
import pdcerl.resources.Resource;
import pdcerl.resources.WriterFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainQLEScanSimulationsMG {

    public static void main (String [] args) {

        Locale.setDefault(Locale.US);
        System.out.println("QLESCAN for Irregular Spatial Cluster Detection");

        int numberSimulationsH0 = 999;

        String prefix = "../data/";
        ReadFile manageFile = new ReadFile(prefix + "Map_MG_Chagas.txt");
        StudyMap studyMap = manageFile.generateStudyMap();

        int episodes = 1000;
        int steps = 171;

        double alpha = 0.8000;
        double gamma = 0.1000;
        double epsilon = 0.0075;

        System.out.println("Simulation H1");

        List<QLearning> qLScan = new ArrayList<>();

        for (Unit unit: studyMap.getUnits()) {
            QLearning qLearning = new QLearning(studyMap, unit, episodes, steps, alpha, gamma, epsilon, false);
            qLScan.add(qLearning);
        }

        long startTime = System.currentTimeMillis();
        qLScan.parallelStream().forEach(QLearning::executeTrainingTest);
        double runTime = (double)(System.currentTimeMillis() - startTime) / 1000.0;

        Solution solution = QLearning.returnBestEnvironment(qLScan);

        System.out.println(solution);

        WriterFile writerFile = new WriterFile();
        writerFile.createWriterFile("Map_MG_Chagas_Simulations.txt");

        writerFile.printWriterFile(solution.getVertices());
        writerFile.printWriterFile(solution.getScanStatisticsValue());
        writerFile.printWriterFile(runTime);

        String fileCases = prefix + "Map_MG_Chagas_H0_999.txt";

        for (int i = 0; i < numberSimulationsH0; i++) {

            System.out.println("Simulation H0: " + (i + 1));

            StudyMap studyMapH0 = Resource.loadFileCases(studyMap, fileCases, i);

            qLScan = new ArrayList<>();

            for (Unit unit: studyMapH0.getUnits()) {
                QLearning qLearning = new QLearning(studyMapH0, unit, episodes, steps, alpha, gamma, epsilon, false);
                qLScan.add(qLearning);
            }

            startTime = System.currentTimeMillis();
            qLScan.parallelStream().forEach(QLearning::executeTrainingTest);
            runTime = (double)(System.currentTimeMillis() - startTime) / 1000.0;

            solution = QLearning.returnBestEnvironment(qLScan);

            writerFile.printWriterFile(solution.getVertices());
            writerFile.printWriterFile(solution.getScanStatisticsValue());
            writerFile.printWriterFile(runTime);

        }

        writerFile.closeWriterFile();
    }

}
