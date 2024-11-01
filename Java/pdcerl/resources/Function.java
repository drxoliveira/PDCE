package pdcerl.resources;

import pdcerl.model.Point;
import pdcerl.model.StudyMap;

import java.util.Arrays;
import java.util.List;

public class Function implements IConstant{

    public static double euclideanDistance(Point a, Point b) {
        return Math.sqrt((a.getX() -b.getX()) * (a.getX() -b.getX()) + (a.getY() -b.getY()) * (a.getY() -b.getY()));
    }

    public static double logarithmLikelihoodRatio(double P, double C, double pz, double cz) {
        if (cz > ((C/P) * pz)) {
            return cz*Math.log(cz/(pz*C/P))+(C-cz)* Math.log((C-cz)/(C-(pz*C/P)));
        } else {
            return IConstant.PENALTY_VALUE;
        }
    }

    public static double scanStatisticsEvaluation(StudyMap studyMap, List<Integer> zone) {

        double pz = 0d;
        double cz = 0d;

        for (Integer unit : zone) {
            pz = studyMap.getUnits().get(unit).getPopulation() + pz;
            cz = studyMap.getUnits().get(unit).getCases() + cz;
        }

        return Function.logarithmLikelihoodRatio(studyMap.getPopulation(), studyMap.getCases(), pz, cz);
    }

    public static double average (double [] values) {
        return Arrays.stream(values).average().orElse(0.0);
    }

    public static double standardDeviation (double [] values) {
        double average = Function.average(values);
        return Math.sqrt(Arrays.stream(values)
                .map(runTime -> Math.pow(runTime - average, 2))
                .average().orElse(0.0));
    }

    public static double standardError (double [] values) {
        return standardDeviation(values)/Math.sqrt(values.length);
    }

}
