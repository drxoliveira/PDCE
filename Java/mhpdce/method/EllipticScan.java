package mhpdce.method;

import mhpdce.model.Point;
import mhpdce.model.Solution;
import mhpdce.model.StudyMap;
import mhpdce.model.Unit;
import mhpdce.resources.Function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class EllipticScan {

    private final StudyMap studyMap;

    private final double [] eccentricity;
    private final int [] numberAngles;

    private final double windowSize;

    private List<Solution> solutionList;
    private Solution bestSolution;

    private double [][]  angles;

    private final boolean verbose;
    private double runTime;

    public record Pair(int index, double value) implements Comparable<Pair> {

        @Override
            public int compareTo(Pair other) {
                return Double.compare(this.value, other.value);
            }

        }

     public EllipticScan(StudyMap studyMap, double [] eccentricity,  int [] numberAngles, double windowSize,
                         boolean verbose) {

         this.studyMap = studyMap;
         this.eccentricity = eccentricity;
         this.numberAngles = numberAngles;
         this.windowSize = windowSize;
         this.verbose = verbose;
         this.bestSolution = new Solution();
         this.solutionList = new ArrayList<>();
     }

    public void execute() {

        long startTime = System.currentTimeMillis();

        this.generateAngles();

        this.generateEllipticWindowSolution();

        this.setRunTime((double)(System.currentTimeMillis() - startTime)/ 1000.0);

        if (verbose) {
            System.out.println(this.bestSolution);
            System.out.println("Runtime: " + this.getRunTime() + " seconds");
        }

    }

    private void generateAngles() {

         int e = this.eccentricity.length;
         int a = Arrays.stream(this.numberAngles).max().getAsInt();

        this.angles = new double[e][a];

        for (int i = 0; i < e; i++ ) {
            for (int j = 0; j < this.numberAngles[i]; j++)
                this.angles[i][j] = (Math.PI * j) / this.numberAngles[i];
        }

    }

    private void generateEllipticWindowSolution() {

        double [] bar = {0.0, 0.0};

        for (Unit unit: this.studyMap.getUnits()) {
            bar[0] += unit.getCentroid().getX();
            bar[1] += unit.getCentroid().getY();
        }

        bar[0] /= this.studyMap.getTotalUnits();
        bar[1] /= this.studyMap.getTotalUnits();

        double [] coordinateX = new double[this.studyMap.getTotalUnits()];
        double [] coordinateY = new double[this.studyMap.getTotalUnits()];

        for (int i = 0; i < this.eccentricity.length; i++ ) {
            for (int j = 0; j < this.numberAngles[i] ; j++) {
                for (int k = 0; k < this.studyMap.getTotalUnits(); k++) {

                    double cx = this.studyMap.getUnits().get(k).getCentroid().getX();
                    double cy = this.studyMap.getUnits().get(k).getCentroid().getY();
                    coordinateX[k]= bar[0] + Math.cos(this.angles[i][j]) * (cx -bar[0]) + Math.sin(this.angles[i][j]) * (cy -bar[1]);
                    coordinateY[k]= bar[1] - Math.sin(this.angles[i][j]) * (cx -bar[0]) + Math.cos(this.angles[i][j]) * (cy -bar[1]);
                    coordinateX[k]= bar[0] + (coordinateX[k] -bar[0]) * this.eccentricity[i];
                    coordinateY[k]= bar[1] + (coordinateY[k] - bar[1]);

                }

                double [][] matrix = new double [this.studyMap.getTotalUnits()][this.studyMap.getTotalUnits()];

                for (int l =0; l < this.studyMap.getTotalUnits(); l++) {
                    for (int m = 0; m < this.studyMap.getTotalUnits(); m++) {
                        if (l == m) continue;
                        matrix[l][m] = Function.euclideanDistance(new Point(coordinateX[l], coordinateY[l]),
                                new Point(coordinateX[m], coordinateY[m]));
                    }
                }

                int size = (int) Math.round(this.studyMap.getTotalUnits() * this.windowSize);

                int [][] indexesSelected = new int [this.studyMap.getTotalUnits()][size];

                for (int l =0; l < this.studyMap.getTotalUnits(); l++) {

                    Pair[] arrayTemp = new Pair[this.studyMap.getTotalUnits()];

                    for (int m =0; m < this.studyMap.getTotalUnits(); m++)
                        arrayTemp[m] = new Pair(m, matrix[l][m]);

                    Arrays.sort(arrayTemp);

                    for (int m =0; m < size; m++)
                        indexesSelected[l][m] = arrayTemp[m].index();
                }

                for (int l =0; l < this.studyMap.getTotalUnits(); l++) {

                    Solution solution = new Solution();
                    List<Integer> vertices = new LinkedList<>();
                    List<Integer> variables = new ArrayList<>(Collections.nCopies(this.studyMap.getTotalUnits(), 0));

                    for (int m =0; m < size; m++) {
                        vertices.add(indexesSelected[l][m]);
                        variables.set(indexesSelected[l][m], 1);
                    }

                    solution.setId(l);
                    solution.setVertices(vertices);
                    solution.setVariables(variables);

                    solution.setScanStatisticsValue(Function.scanStatisticsEvaluation(this.studyMap, solution.getVertices()));

                    if (bestSolution.getScanStatisticsValue() < solution.getScanStatisticsValue())
                        bestSolution.copy(solution);

                    this.solutionList.add(solution);
                }

            }
        }

    }

    public List<Solution> getSolutionList() {
        return solutionList;
    }

    public void setSolutionList(List<Solution> solutionList) {
        this.solutionList = solutionList;
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
