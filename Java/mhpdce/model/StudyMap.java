package mhpdce.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudyMap {

    private List<Unit> units;
    private int population;
    private int cases;
    private int totalUnits;
    private String path;
    private List<Double> unitsScanStatisticsEvaluation;

    private Map<Integer, List<Integer>> mapOfAdjacentUnitsByUnit;
    private Map<Integer, List<Integer>> mapOfUnitsShortestDistanceByUnit;

    public StudyMap() {
    }

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getCases() {
        return cases;
    }

    public void setCases(int cases) {
        this.cases = cases;
    }

    public int getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(int totalUnits) {
        this.totalUnits = totalUnits;
    }

    public Map<Integer, List<Integer>> getMapOfAdjacentUnitsByUnit() {
        return mapOfAdjacentUnitsByUnit;
    }

    public void setMapOfAdjacentUnitsByUnit(Map<Integer, List<Integer>> mapOfAdjacentUnitsByUnit) {
        this.mapOfAdjacentUnitsByUnit = mapOfAdjacentUnitsByUnit;
    }

    public Map<Integer, List<Integer>> getMapOfUnitsShortestDistanceByUnit() {
        return mapOfUnitsShortestDistanceByUnit;
    }

    public void setMapOfUnitsShortestDistanceByUnit(Map<Integer, List<Integer>> mapOfUnitsShortestDistanceByUnit) {
        this.mapOfUnitsShortestDistanceByUnit = mapOfUnitsShortestDistanceByUnit;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    public List<Double> getUnitsScanStatisticsEvaluation() {
        return unitsScanStatisticsEvaluation;
    }

    public void setUnitsScanStatisticsEvaluation(List<Double> unitsScanStatisticsEvaluation) {
        this.unitsScanStatisticsEvaluation = unitsScanStatisticsEvaluation;
    }

    public void copy (StudyMap studyMap) {

        this.setTotalUnits(studyMap.getTotalUnits());
        this.setPopulation(studyMap.getPopulation());
        this.setCases(studyMap.getCases());
        this.setPath(studyMap.getPath());

        Map<Integer, List<Integer>> mapOfAdjacentUnitsByUnit = new HashMap<>();
        for (int i = 0; i < studyMap.getTotalUnits(); i++) {
            List<Integer> adjacentUnitsByUnit = new ArrayList<>(studyMap.getMapOfAdjacentUnitsByUnit().get(i));
            mapOfAdjacentUnitsByUnit.put(i, adjacentUnitsByUnit);
        }
        this.setMapOfAdjacentUnitsByUnit(mapOfAdjacentUnitsByUnit);

        Map<Integer, List<Integer>> mapOfUnitsShortestDistanceByUnit = new HashMap<>();
        for (int i = 0; i < studyMap.getTotalUnits(); i++) {
            List<Integer> unitsShortestDistanceByUnit;
            unitsShortestDistanceByUnit = new ArrayList<>(studyMap.getMapOfUnitsShortestDistanceByUnit().get(i));
            mapOfUnitsShortestDistanceByUnit.put(i, unitsShortestDistanceByUnit);        }

        this.setMapOfUnitsShortestDistanceByUnit(mapOfUnitsShortestDistanceByUnit);

        List<Unit> unitsY = new ArrayList<>(studyMap.getTotalUnits());
        for (Unit unitX: studyMap.getUnits()){

            Unit unitY = new Unit();
            unitY.setId(unitX.getId());
            unitY.setPopulation(unitX.getPopulation());
            unitY.setCases(unitX.getCases());
            unitY.setCentroid(new Point(unitX.getCentroid().getX(), unitX.getCentroid().getY()));

            List<Point> frontierY = new ArrayList<>();
            for (Point pointX: unitX.getFrontier()) {
                Point pointY = new Point();
                pointY.setX(pointX.getX());
                pointY.setY(pointX.getY());
                frontierY.add(pointY);
            }
            unitY.setFrontier(frontierY);

            unitsY.add(unitY);
        }
        this.setUnits(unitsY);

        List<Double> unitsScanStatisticsEvaluation = new ArrayList<>(studyMap.getTotalUnits());
        for (Unit unit: studyMap.getUnits()) {
            unitsScanStatisticsEvaluation.add(studyMap.getUnitsScanStatisticsEvaluation().get(unit.getId()));
        }
        this.setUnitsScanStatisticsEvaluation(unitsScanStatisticsEvaluation);

    }

    public String toString() {

        return  "{ \n"+
                "\"Study Map\": { \n" +
                "\"Total units\": " + this.totalUnits + ", \n" +
                "\"Population\": " + this.population + ", \n" +
                "\"Cases:\" " + this.cases + ", \n" +
                "\"Path:\" " + this.path + " } \n" +
                "}";
    }

}
