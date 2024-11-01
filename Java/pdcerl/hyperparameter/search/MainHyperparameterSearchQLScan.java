package pdcerl.hyperparameter.search;

import pdcerl.model.StudyMap;
import pdcerl.model.Unit;
import pdcerl.reinforcementlearning.singleobjective.QLearning;
import pdcerl.resources.ReadFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainHyperparameterSearchQLScan {

    public static void main (String [] args) {

        System.out.println("Hyperparameter Search Q-Learning");

        Locale.setDefault(Locale.US);

        ReadFile manageFile = new ReadFile("data/...");
        StudyMap studyMap = manageFile.generateStudyMap();

        int episodes = 1000;
        int steps = 171;

        double [] alphaArray  = {0.0001, 0.0500, 0.1000, 0.1500, 0.2000, 0.2500, 0.3000, 0.3500, 0.4000, 0.4500, 0.5000,
                0.5500, 0.6000, 0.6500, 0.7000, 0.7500, 0.8000, 0.8500, 0.9000, 0.9500, 1.0000};

        double [] gammaArray   = {0.0001, 0.0500, 0.1000, 0.1500, 0.2000, 0.2500, 0.3000, 0.3500, 0.4000, 0.4500, 0.5000,
                0.5500, 0.6000, 0.6500, 0.7000, 0.7500, 0.8000, 0.8500, 0.9000, 0.9500, 1.0000};

        double [] epsilonArray = {0.0001, 0.0005, 0.0010, 0.0015, 0.0020, 0.0025, 0.0030, 0.0035, 0.0040, 0.0045, 0.0050,
                0.0055, 0.0060, 0.0065, 0.0070, 0.0075, 0.0080, 0.0085, 0.0090, 0.0095, 0.0100};

        int i = 0;

        for (double alpha: alphaArray) {
            for (double gamma : gammaArray) {
                for (double epsilon : epsilonArray) {

                    List<QLearning> qLScan = new ArrayList<>();

                    for (Unit unit : studyMap.getUnits()) {
                        if (studyMap.getUnitsScanStatisticsEvaluation().get(unit.getId()) != -1.0) {
                            QLearning qLearning = new QLearning(studyMap, unit, episodes, steps, alpha, gamma, epsilon,
                                    false);
                            qLScan.add(qLearning);
                        }
                    }

                    qLScan.parallelStream().forEach(QLearning::executeTrainingTest);

                    int index = QLearning.returnIndexBestEnvironment(qLScan);

                    System.out.printf(i + "; ");
                    System.out.printf("episodes: %d; ", episodes);
                    System.out.printf("steps: %d; ", steps);
                    System.out.printf("alpha: %.4f; ", alpha);
                    System.out.printf("gamma: %.4f; ", gamma);
                    System.out.printf("epsilon: %.4f; ", epsilon);
                    System.out.printf("improvement: %d ", qLScan.get(index).getImprov());
                    System.out.println();
                    i++;

                }
            }
        }
    }
}
