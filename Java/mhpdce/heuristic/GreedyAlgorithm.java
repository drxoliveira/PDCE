package mhpdce.heuristic;

import mhpdce.model.Solution;
import mhpdce.model.StudyMap;
import mhpdce.model.Unit;
import mhpdce.resources.Function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GreedyAlgorithm {

    private final StudyMap studyMap;
    private final double gr;
    private List<Solution>  solutions;
    private Solution bestSolution;
    private double runTime;
    private final boolean verbose;

    public GreedyAlgorithm(StudyMap studyMap, double gr, boolean verbose) {

        this.studyMap = studyMap;
        this.gr = gr;
        this.verbose = verbose;

        this.bestSolution = new Solution();
        this.solutions = new ArrayList<>();

    }

    public void execute() {

        long startTime = System.currentTimeMillis();

        this.buildGreedySolutions();

        this.setRunTime((double)(System.currentTimeMillis() - startTime)/ 1000.0);

        if (this.verbose) {
            System.out.println(this.bestSolution);
            System.out.println("Runtime: "+ this.getRunTime() +" seconds");
        }

    }

    public void buildGreedySolutions() {

        double maximumSize = Math.round(this.studyMap.getTotalUnits() * this.gr);

        for (Unit unit: this.studyMap.getUnits()) {

            int u = unit.getId();
            List<Integer> vertices = new LinkedList<>();
            vertices.add(u);

            List<Integer> variables = new ArrayList<>(Collections.nCopies(this.studyMap.getTotalUnits(), 0));
            variables.set(u, 1);

            Solution solution = new Solution();
            solution.setVertices(vertices);
            solution.setVariables(variables);
            solution.setScanStatisticsValue(Function.scanStatisticsEvaluation(this.studyMap, solution.getVertices()));

            int x = 0;
            boolean check = true;

            while (check) {

                double maxScanStatisticsValue = Double.NEGATIVE_INFINITY;

                x = (x > -1) ? -1 : x -1;

                for (Integer v: this.studyMap.getMapOfAdjacentUnitsByUnit().get(u)) {
                    if (!solution.getVertices().contains(v)) {
                        solution.getVertices().add(v);
                        solution.getVariables().set(v, 1);
                        solution.setScanStatisticsValue(Function.scanStatisticsEvaluation(this.studyMap,
                                solution.getVertices()));

                        if (maxScanStatisticsValue < solution.getScanStatisticsValue()) {
                            maxScanStatisticsValue = solution.getScanStatisticsValue();
                            x = v;
                        }

                        solution.getVertices().remove((Integer)v);
                        solution.getVariables().set(v, 0);
                    }

                }

                if (x > -1) {
                    if (solution.getVertices().size() < maximumSize) {

                        solution.getVertices().add(x);
                        solution.getVariables().set(x, 1);
                        solution.setScanStatisticsValue(Function.scanStatisticsEvaluation(this.studyMap,
                                solution.getVertices()));
                        u = x;

                    } else
                        check = false;
                } else
                    u = solution.getVertices().get(solution.getVertices().size() + x);

            }

            if (bestSolution.getScanStatisticsValue() < solution.getScanStatisticsValue())
                bestSolution.copy(solution);

            this.solutions.add(solution);
        }

    }

    public List<Solution> getSolutions() {
        return solutions;
    }

    public void setSolutions(List<Solution> solutions) {
        this.solutions = solutions;
    }

    public Solution getBestSolution() {
        return bestSolution;
    }

    public void setBestSolution(Solution bestSolution) {
        this.bestSolution = bestSolution;
    }

    public double getRunTime() {
        return runTime;
    }

    public void setRunTime(double runTime) {
        this.runTime = runTime;
    }

}
