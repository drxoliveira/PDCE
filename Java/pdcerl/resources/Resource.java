package pdcerl.resources;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import pdcerl.model.Solution;
import pdcerl.model.StudyMap;
import pdcerl.model.Unit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Resource {


    public static StudyMap loadFileCases(StudyMap studyMapX, String fileCases, int simulation) {

        StudyMap studyMapY = new StudyMap();
        studyMapY.copy(studyMapX);

        try {

            FileReader fileReader = new FileReader(fileCases);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String [] stringArrayTemp = new String[studyMapY.getTotalUnits()];

            int i = 0;
            while (i <= simulation) {
                stringArrayTemp = bufferedReader.readLine().split(" ");
                i++;
            }
            int totalCases = 0;

            for (int j = 0; j < stringArrayTemp.length; j++) {
                int cases = Integer.parseInt(stringArrayTemp[j]);
                studyMapY.getUnits().get(j).setCases(cases);
                totalCases += cases;
            }

            studyMapY.setCases(totalCases);

            fileReader.close();
            bufferedReader.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Double> unitsScanStatisticsEvaluation = new ArrayList<>();

        for (Unit unit: studyMapY.getUnits()) {

            List<Integer> vertices = new LinkedList<>();
            vertices.add(unit.getId());

            unitsScanStatisticsEvaluation.add(Function.scanStatisticsEvaluation(studyMapY, vertices));

        }
        studyMapY.setUnitsScanStatisticsEvaluation(unitsScanStatisticsEvaluation);

        return studyMapY;
    }

    public static double [] calculatePowerDetection(List<Solution> solutionListH0, List<Solution> solutionListH1,
                                                 double p) {

        int numberSimulationsH0 = solutionListH0.size();
        int numberSimulationsH1 = solutionListH1.size();

        double [] valuesH0 = new double [numberSimulationsH0];
        double [] valuesH1 = new double [numberSimulationsH1];

        for (int i = 0; i < numberSimulationsH0; i++) {
            valuesH0[i] = solutionListH0.get(i).getScanStatisticsValue();
        }

        for (int i = 0; i < numberSimulationsH1; i++) {
            valuesH1[i] = solutionListH1.get(i).getScanStatisticsValue();
        }

        Percentile percentile = new Percentile();
        double criticalValue = percentile.evaluate(valuesH0, p);

        double [] power = new double[numberSimulationsH1];

        for (int i = 0; i < numberSimulationsH1; i++) {
            if (valuesH1[i] > criticalValue) {
                power[i] = 1.0;
            } else {
                power[i] = 0;
            }
        }

        return power;

    }

    public static List<Solution> returnSignificantSolutions (List<Solution> solutionListH0, List<Solution> solutionListH1,
                                                             double p) {

        int numberSimulationsH0 = solutionListH0.size();
        int numberSimulationsH1 = solutionListH1.size();

        double [] valuesH0 = new double [numberSimulationsH0];
        double [] valuesH1 = new double [numberSimulationsH1];

        for (int i = 0; i < numberSimulationsH0; i++) {
            valuesH0[i] = solutionListH0.get(i).getScanStatisticsValue();
        }

        for (int i = 0; i < numberSimulationsH1; i++) {
            valuesH1[i] = solutionListH1.get(i).getScanStatisticsValue();
        }

        Percentile percentile = new Percentile();
        double criticalValue = percentile.evaluate(valuesH0, p);

        List<Solution> solutionList = new LinkedList<>();

        for (int i = 0; i < numberSimulationsH1; i++) {
            if (valuesH1[i] > criticalValue) {
                Solution solution = new Solution();
                solution.copy(solutionListH1.get(i));
                solutionList.add(solution);
            }
        }

        return solutionList;
    }


    public static double [] calculatePrecision(StudyMap studyMap, List<Solution> solutionListH0,
                                               List<Solution> solutionListH1, int [] cluster, double p) {

        List<Solution> solutionList = Resource.returnSignificantSolutions(solutionListH0, solutionListH1, p);

        double [] precision = new double[solutionList.size()];

        int index = 0;
        for (Solution solution: solutionList) {

            double populationDetectedCluster = 0.0;
            for (Integer u: solution.getVertices())
                populationDetectedCluster += studyMap.getUnits().get(u).getPopulation();

            List<Integer> intersection = new LinkedList<>();

            for (Integer u: solution.getVertices())
                for (int v : cluster)
                    if (u == (v - 1))
                        intersection.add(u);

            double populationIntersection = 0.0;
            for (Integer u: intersection)
                populationIntersection += studyMap.getUnits().get(u).getPopulation();

            precision[index++] = populationIntersection/populationDetectedCluster;
        }

        return precision;
    }


    public static double [] calculateRecall(StudyMap studyMap, List<Solution> solutionListH0,
                                            List<Solution> solutionListH1, int [] cluster, double p) {

        List<Solution> solutionList = Resource.returnSignificantSolutions(solutionListH0, solutionListH1, p);

        double [] recall = new double[solutionList.size()];

        double populationRealCluster = 0.0;
        for (int v : cluster)
            populationRealCluster += studyMap.getUnits().get(v -1).getPopulation();

        int index = 0;
        for (Solution solution: solutionList) {

            List<Integer> intersection = new LinkedList<>();

            for (Integer u: solution.getVertices())
                for (int v : cluster)
                    if (u == (v - 1))
                        intersection.add(u);

            double populationIntersection = 0.0;
            for (Integer u: intersection)
                populationIntersection += studyMap.getUnits().get(u).getPopulation();

            recall[index++] = populationIntersection/populationRealCluster;
        }

        return recall;
    }



    public static double [] calculateAccuracy(StudyMap studyMap, List<Solution> solutionListH0,
                                              List<Solution> solutionListH1, int [] cluster, double p) {

        List<Solution> solutionList = Resource.returnSignificantSolutions(solutionListH0, solutionListH1, p);

        double [] accuracy = new double[solutionList.size()];

        int index = 0;
        for(Solution solution: solutionList) {

            Set<Integer> intersection1 = new HashSet<>();
            for (Integer u : solution.getVertices()) {
                for (int v : cluster) {
                    if (u == (v - 1)) {
                        intersection1.add(u);
                    }
                }
            }

            Set<Integer> notRealCluster = new HashSet<>();
            for (Unit unit : studyMap.getUnits()) {
                if (Arrays.stream(cluster).noneMatch(v -> unit.getId() == (v - 1))) {
                    notRealCluster.add(unit.getId());
                }
            }

            Set<Integer> notDetectedCluster = new HashSet<>();
            for (Unit unit : studyMap.getUnits()) {
                if (!solution.getVertices().contains(unit.getId())) {
                    notDetectedCluster.add(unit.getId());
                }
            }

            Set<Integer> intersection2 = new HashSet<>();
            for (Integer u: notRealCluster) {
                if (notDetectedCluster.contains(u)) {
                    intersection2.add(u);
                }
            }

            Set<Integer> union = new HashSet<>(intersection1);
            union.addAll(intersection2);

            double population = union.stream()
                    .mapToDouble(u -> studyMap.getUnits().get(u).getPopulation())
                    .sum();

            accuracy[index++] = population / studyMap.getPopulation();
        }

        return accuracy;
    }

    public static double [] calculateFScore(StudyMap studyMap, List<Solution> solutionListH0,
                                                List<Solution> solutionListH1, int [] cluster, double p) {

        List<Solution> solutionList = Resource.returnSignificantSolutions(solutionListH0, solutionListH1, p);

        double [] fScore = new double[solutionList.size()];

        double populationRealCluster = 0.0;
        for (int v : cluster)
            populationRealCluster += studyMap.getUnits().get(v -1).getPopulation();

        int index = 0;
        for (Solution solution: solutionList) {

            double populationDetectedCluster = 0.0;
            for (Integer u: solution.getVertices())
                populationDetectedCluster += studyMap.getUnits().get(u).getPopulation();

            List<Integer> intersection = new LinkedList<>();

            for (Integer u: solution.getVertices()) {
                for (int v : cluster)
                    if (u == (v - 1))
                        intersection.add(u);
            }

            double populationIntersection = 0.0;
            for (Integer u: intersection)
                populationIntersection += studyMap.getUnits().get(u).getPopulation();

            double recall = populationIntersection / populationRealCluster;
            double precision = populationIntersection / populationDetectedCluster;

            if (precision + recall != 0.0)
                fScore[index++] = 2.0 * ((precision * recall) / (precision + recall));
            else
                fScore[index++] = 0.0;
        }

        return fScore;
    }

}
