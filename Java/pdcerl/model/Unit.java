package pdcerl.model;

import java.util.List;

public class Unit {

    private int id;
    private int population;
    private int cases;
    private Point centroid = new Point();
    private List<Point> frontier;

    public Unit() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Point getCentroid() {
        return centroid;
    }

    public void setCentroid(Point centroid) {
        this.centroid = centroid;
    }

    public List<Point> getFrontier() {
        return frontier;
    }

    public void setFrontier(List<Point> frontier) {
        this.frontier = frontier;
    }

}
