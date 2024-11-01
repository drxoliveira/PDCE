package mhpdce.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Solution {

    private int id;
    private List<Integer> vertices;
    private List<Integer> variables;
    private double scanStatisticsValue;
    private double penaltyFunctionValue;
    private double crowdingDistanceValue;

    public Solution() {
        this.id = -1;
        this.vertices = new LinkedList<>();
        this.variables = new ArrayList<>();
        this.scanStatisticsValue = 0.0;
        this.penaltyFunctionValue = 0.0;
        this.crowdingDistanceValue = 0.0;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getVertices() {
        return vertices;
    }

    public void setVertices(List<Integer> vertices) {
        this.vertices = vertices;
    }

    public List<Integer> getVariables() {
        return variables;
    }

    public void setVariables(List<Integer> variables) {
        this.variables = variables;
    }

    public double getScanStatisticsValue() {
        return scanStatisticsValue;
    }

    public void setScanStatisticsValue(double scanStatisticsValue) {
        this.scanStatisticsValue = scanStatisticsValue;
    }

    public double getPenaltyFunctionValue() {
        return penaltyFunctionValue;
    }

    public void setPenaltyFunctionValue(double penaltyFunctionValue) {
        this.penaltyFunctionValue = penaltyFunctionValue;
    }

    public double getCrowdingDistanceValue() {
        return crowdingDistanceValue;
    }

    public void setCrowdingDistanceValue(double crowdingDistanceValue) {
        this.crowdingDistanceValue = crowdingDistanceValue;
    }

    public void copy(Solution solution) {

        this.vertices.clear();
        this.vertices.addAll(solution.vertices);

        this.variables.clear();
        this.variables.addAll(solution.variables);

        this.scanStatisticsValue = solution.scanStatisticsValue;
        this.penaltyFunctionValue = solution.penaltyFunctionValue;
        this.crowdingDistanceValue = solution.crowdingDistanceValue;

    }

    public String toString() {

        Collections.sort(this.vertices );

        return "{\n" +
                "\"Solution\": { \n" +
                "\"Size:\": " + this.vertices.size() + ",\n" +
                "\"Vertices\": " + this.vertices + ",\n" +
               // "\"variables\": " + this.variables + ",\n" +
                "\"Scan Statistics Value\": " +
                String.format("%.4f", this.scanStatisticsValue).replace(",", ".") + ",\n" +
                "\"Penalty Function Value\": " +
                String.format("%.4f", this.penaltyFunctionValue).replace(",", ".") + " }\n" +
                "}";
    }

}
