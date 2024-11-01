package pdcerl.resources;

import pdcerl.model.StudyMap;
import pdcerl.model.Unit;
import pdcerl.model.Point;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReadFile {

    private FileReader fileReader;
    private BufferedReader bufferedReader;
    private final String file;
    private StudyMap studyMap = new StudyMap();

    private static class DistanceValue implements Comparable <DistanceValue>{

        private int id;
        private double value;

        public DistanceValue() {
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        @Override
        public int compareTo(DistanceValue distanceValue) {

            if (Double.compare(this.getValue(), distanceValue.getValue()) < 0d) {
                return -1;
            }

            if (Double.compare(this.getValue(), distanceValue.getValue()) > 0d) {
                return 1;
            }

            return 0;
        }

    }

    public ReadFile(String file) {
        this.file = file;
    }

    public void openFile() {
        try {
            this.fileReader = new FileReader(this.file);
            this.bufferedReader = new BufferedReader(this.fileReader);
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
    }

    public void readFile() {

        this.studyMap = new StudyMap();
        String [] stringArrayTemp;

        try {

            stringArrayTemp = this.bufferedReader.readLine().split("\n");
            this.studyMap.setTotalUnits(Integer.parseInt(stringArrayTemp[0]));

            Map<Integer, List<Integer>> mapOfAdjacentUnitsByUnit = new HashMap<>();

            for (int i = 0; i < this.studyMap.getTotalUnits(); i++ ) {

                stringArrayTemp = this.bufferedReader.readLine().split(" ");

                List<Integer> adjacentUnits = new ArrayList<>();

                for (int j = 1; j < stringArrayTemp.length -1; j++) {
                    adjacentUnits.add(Integer.parseInt(stringArrayTemp[j] ) -1);
                }

                mapOfAdjacentUnitsByUnit.put(i, adjacentUnits);
            }

            this.studyMap.setMapOfAdjacentUnitsByUnit(mapOfAdjacentUnitsByUnit);

            List<Unit> units = new ArrayList<>( this.studyMap.getTotalUnits() );

            int totalPopulation = 0;
            int totalCases = 0;

            for (int i = 0; i < this.studyMap.getTotalUnits(); i++ ) {

                stringArrayTemp = this.bufferedReader.readLine().split(" ");

                Unit unit = new Unit();
                unit.setId(i);
                unit.setPopulation(Integer.parseInt(stringArrayTemp[1]));
                unit.setCases(Integer.parseInt(stringArrayTemp[2]));

                totalPopulation = totalPopulation + unit.getPopulation();
                totalCases = totalCases + unit.getCases();

                units.add(unit);
            }

            this.studyMap.setUnits(units);
            this.studyMap.setPopulation(totalPopulation);
            this.studyMap.setCases(totalCases);

            for (Unit unit: this.studyMap.getUnits()) {

                stringArrayTemp = this.bufferedReader.readLine().split(" ");

                Point centroid = new Point ();
                centroid.setX(Double.parseDouble(stringArrayTemp[1]));
                centroid.setY(Double.parseDouble(stringArrayTemp[2]));

                unit.setCentroid(centroid);
            }

            for (Unit unit: this.studyMap.getUnits())
                unit.setFrontier(new ArrayList<>());

            for (Unit unit: this.studyMap.getUnits()) {

                List<Point> frontier = unit.getFrontier();
                stringArrayTemp = this.bufferedReader.readLine().split(" ");

                for (int i = 1; i < stringArrayTemp.length -1; i++ ) {
                    Point point = new Point();
                    point.setX(Double.parseDouble(stringArrayTemp[i]));
                    frontier.add(point);
                }
            }

            for (Unit unit: this.studyMap.getUnits()) {

                List<Point> frontier = unit.getFrontier();
                stringArrayTemp = this.bufferedReader.readLine().split(" ");

                for (int i = 1; i < stringArrayTemp.length -1; i++ ) {
                    Point point = frontier.get(i - 1);
                    point.setY(Double.parseDouble(stringArrayTemp[i])) ;
                }
            }

            Map<Integer, List<Integer>> mapOfUnitsShortestDistanceByUnit = new HashMap<>();

            for (Unit ux: this.studyMap.getUnits()) {

                List<DistanceValue> distanceValuesList = new  ArrayList<>();

                for (Unit uy: this.studyMap.getUnits()) {

                    DistanceValue distanceValues = new DistanceValue();

                    distanceValues.setId(uy.getId());
                    distanceValues.setValue(Function.euclideanDistance(ux.getCentroid(), uy.getCentroid()));

                    distanceValuesList.add(distanceValues);
                }

                Collections.sort(distanceValuesList);

                List<Integer> unitsShortestDistanceByUnit = new ArrayList<>();

                for(DistanceValue distanceValue: distanceValuesList)
                    unitsShortestDistanceByUnit.add(distanceValue.getId());

                mapOfUnitsShortestDistanceByUnit.put(ux.getId(), unitsShortestDistanceByUnit);
            }

            this.studyMap.setMapOfUnitsShortestDistanceByUnit(mapOfUnitsShortestDistanceByUnit);

            this.studyMap.setPath(this.file);

            List<Double> unitsScanStatisticsEvaluation = new ArrayList<>();

            for (Unit unit: this.studyMap.getUnits()) {
                List<Integer> vertices = new LinkedList<>();
                vertices.add(unit.getId());
                unitsScanStatisticsEvaluation.add(Function.scanStatisticsEvaluation(this.studyMap, vertices));
            }

            this.studyMap.setUnitsScanStatisticsEvaluation(unitsScanStatisticsEvaluation);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void closeFile() {
        try {
            this.fileReader.close();
            this.bufferedReader.close();
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
    }

    public StudyMap generateStudyMap () {
        this.openFile();
        this.readFile();
        this.closeFile();

        return this.studyMap;
    }

}
