package pdcerl.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Solution {

    private int id;
    private List<Integer> vertices;
    private double scanStatisticsValue;
    private double penaltyFunctionValue;

    public Solution() {
        this.id = -1;
        this.vertices = new LinkedList<>();
        this.scanStatisticsValue = 0.0;
        this.penaltyFunctionValue = 0.0;

    }

    public List<Integer> getVertices() {
        return vertices;
    }

    public void setVertices(List<Integer> vertices) {
        this.vertices = vertices;
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

    public void copy(Solution solution) {
        this.vertices.clear();
        this.vertices.addAll(solution.vertices);
        this.scanStatisticsValue = solution.scanStatisticsValue;
        this.penaltyFunctionValue = solution.penaltyFunctionValue;
    }

    public String toString() {

        Collections.sort(this.vertices );

        return "{\n" +
                "\"Solution\": { \n" +
                "\"Size:\": " + this.vertices.size() + ",\n" +
                "\"Vertices\": " + this.vertices + ",\n" +
                "\"Scan Statistics Value\": " + String.format("%.4f", this.scanStatisticsValue)+ ",\n" +
                "\"Penalty Function Value\": " + String.format("%.4f", this.penaltyFunctionValue) + " }\n" +
                "}";

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
