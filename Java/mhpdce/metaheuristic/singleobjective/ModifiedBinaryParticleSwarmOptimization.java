package mhpdce.metaheuristic.singleobjective;

import mhpdce.heuristic.GreedyAlgorithm;
import mhpdce.model.Solution;
import mhpdce.model.StudyMap;
import mhpdce.resources.Function;
import mhpdce.resources.IConstant;
import mhpdce.resources.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// ------------------------------------------------------------------------------------------
// Modified Binary Particle Swarm Optimization for Irregular Spatial Clusters Detection
// ------------------------------------------------------------------------------------------

public class ModifiedBinaryParticleSwarmOptimization implements IConstant {

    private final StudyMap studyMap;

    private final int ni; // number of iterations.
    private final int np; // number of particles.
    private final int nd; // number of dimensions.

    private final double w; // inertia component.

    private final double gMax; // maximum genotype.
    private final double gMin; // minimum genotype.

    private final double c1; // cognitive component.
    private final double c2; // social component.

    private final double gr; // maximum size of greedy solutions.
    private final int sMax; // maximum solution size in map.

    private final double trMin; // minimum turbulence coverage.
    private double tr;    // current turbulence coverage.

    private final List<Solution> swarm; // population of particles.
    private final List<Solution> pBest; // p_best guides.
    private final Solution gBest; // g_best guide.
    private final double [][] velocities; // velocities of particles.
    private final double [][] genotypes; // genotypes of particles.

    private static final double DECAY  = 0.9900;

    private final boolean verbose;
    private double runTime;

    public ModifiedBinaryParticleSwarmOptimization(StudyMap studyMap, int ni, int np, int nd, double w, double gMax,
             double gMin, double c1, double c2, double gr, double trMax, double trMin, double sMax, boolean verbose) {

        this.studyMap = studyMap;

        this.ni = ni;
        this.np = np;
        this.nd = nd;

        this.w = w;

        this.gMax  = gMax;
        this.gMin  = gMin;

        this.c1 = c1;
        this.c2 = c2;

        this.gr = gr;

        this.trMin = trMin;
        this.tr = trMax;

        this.sMax = (int) Math.round(this.studyMap.getTotalUnits() * sMax);

        this.verbose = verbose;

        this.swarm = new ArrayList<>(this.np);
        this.pBest = new ArrayList<>(this.np);
        this.gBest = new Solution();

        this.velocities = new double[this.np][this.nd];
        this.genotypes = new double[this.np][this.nd];

    }

    public void execute () {

        long startTime = System.currentTimeMillis();

        this.initializeSwarm();

        for (int i = 0; i < this.ni; i++ ) {

            if (verbose) {
                System.out.printf("Iteration: %d; size: %d; llr: %.4f%n", (i + 1),
                        this.gBest.getVertices().size(), this.gBest.getScanStatisticsValue());
            }

            this.updateVelocities();
            this.updatePosition();
            this.turbulenceOperator();
            this.evaluate();
            this.updateGuides(false);

        }

        this.setRunTime((double)(System.currentTimeMillis() - startTime)/ 1000.0);

        if(this.verbose) {
            System.out.println(this.gBest);
            System.out.println("Runtime: " + this.getRunTime() + " seconds");
        }

    }

    public void initializeSwarm() {

        Random random = new Random();

        GreedyAlgorithm greedyAlgorithm = new GreedyAlgorithm(this.studyMap, this.gr, false);
        greedyAlgorithm.execute();

        int id = 0;
        for (Solution solutionX: greedyAlgorithm.getSolutions()) {
            Solution solutionY = new Solution();
            solutionY.setId(id++);
            solutionY.copy(solutionX);
            this.swarm.add(solutionY);
        }

        for (int p = 0; p < this.np; p++) {
            for (int d = 0; d < this.nd; d++) {
                this.velocities[p][d] = this.gMin + (this.gMax - this.gMin) * random.nextDouble();
                this.genotypes[p][d]  = this.gMin + (this.gMax - this.gMin) * random.nextDouble();
            }
        }

        this.updateGuides(true);
    }

    public void updateGuides(boolean init) {
        Solution bestSolution = this.updatePBest(init);
        this.updateGBest(bestSolution);
    }

    public Solution updatePBest (boolean init) {

        Solution bestSolution = new Solution();

        if (init) {
            for (Solution solutionX: this.swarm) {

                Solution solutionY = new Solution();
                solutionY.copy(solutionX);

                this.pBest.add(solutionY);

                if (bestSolution.getScanStatisticsValue() < solutionY.getScanStatisticsValue()) {
                    bestSolution.copy(solutionY);
                }
            }
        } else {

            for (int p =0; p < np; p++) {
                if (this.pBest.get(p).getScanStatisticsValue() < this.swarm.get(p).getScanStatisticsValue()) {

                    this.pBest.get(p).copy(this.swarm.get(p));

                    if (bestSolution.getScanStatisticsValue() < this.pBest.get(p).getScanStatisticsValue()) {
                        bestSolution.copy(this.pBest.get(p));
                    }
                }
            }
        }

        return bestSolution;
    }

    public void updateGBest(Solution solution) {

        if (this.gBest.getScanStatisticsValue() < solution.getScanStatisticsValue()) {
            this.gBest.copy(solution);
        }

    }

    public void updateVelocities() {

        Random random = new Random();

        for (int p =0; p < this.np; p++) {
            for (int d =0; d < this.nd; d++) {

                double velocity = this.velocities[p][d];
                double r1 = random.nextDouble();
                double r2 = random.nextDouble();

                double position = this.swarm.get(p).getVariables().get(d);
                double pBest = this.pBest.get(p).getVariables().get(d);
                double gBest = this.gBest.getVariables().get(d);

                this.velocities[p][d] = this.w * velocity + this.c1 * r1 * (pBest -position) +  this.c2 * r2
                        * (gBest -position);

            }
        }

    }

    public void updatePosition() {

        Random random = new Random();

        for (int p =0; p < this.np; p++) {
            for (int d=0; d < this.nd; d++) {

                this.genotypes[p][d] = this.genotypes[p][d] + this.velocities[p][d];

                if (this.genotypes[p][d] > this.gMax) {
                    this.genotypes[p][d] = this.gMax;
                }

                if (this.genotypes[p][d] < this.gMin) {
                    this.genotypes[p][d] = this.gMin;
                }

                if (random.nextDouble() < Function.sigmoid(this.genotypes[p][d])) {
                    if (!this.swarm.get(p).getVertices().contains(d)) {
                        this.swarm.get(p).getVertices().add(d);
                        this.swarm.get(p).getVariables().set(d, 1);
                    }
                } else {
                    if (this.swarm.get(p).getVertices().contains(d)) {
                        this.swarm.get(p).getVertices().remove((Integer) d);
                        this.swarm.get(p).getVariables().set(d, 0);
                    }
                }

            }
        }
    }

    public void turbulenceOperator() {

        Random random = new Random();

        int size = (int) (this.np * this.tr);

        List<Integer> samples = Resource.getRandomSample(this.np, size);

        for(Integer index: samples) {

            int nd = this.swarm.get(index).getVertices().size();

            if ( nd > 0) {

                int v = this.swarm.get(index).getVertices().get(random.nextInt(nd));

                if (random.nextInt(2) == 1) {

                    List<Integer> listAdjacentUnitsByUnit = this.studyMap.getMapOfAdjacentUnitsByUnit().get(v);

                    int u = listAdjacentUnitsByUnit.get(random.nextInt(listAdjacentUnitsByUnit.size()));

                    if (!this.swarm.get(index).getVertices().contains(u)) {
                        this.swarm.get(index).getVertices().add(u);
                        this.swarm.get(index).getVariables().set(u, 1);
                    }

                } else {
                    this.swarm.get(index).getVertices().remove((Integer) v);
                    this.swarm.get(index).getVariables().set(v, 0);
                }

            } else {
                this.swarm.get(index).copy(this.gBest);
            }

        }

        if (this.tr > this.trMin) {
            this.tr *= ModifiedBinaryParticleSwarmOptimization.DECAY;
        }

    }

    public void evaluate() {
        this.swarm.parallelStream().forEach((particle) ->
                ModifiedBinaryParticleSwarmOptimization.evaluationSolutionWithSizeRestriction(this.studyMap, particle,
                 this.sMax));
    }

    public static void evaluationSolutionWithSizeRestriction(StudyMap studyMap, Solution solution, int sMax) {

        if (Function.checkSolutionConstraints(solution, sMax)) {
            solution.setScanStatisticsValue(Function.scanStatisticsEvaluation(studyMap, solution.getVertices()));
        } else {
            solution.setScanStatisticsValue(Function.PENALTY_VALUE);
        }

    }

    public Solution getGBest() {
        return gBest;
    }

    public double getRunTime() {
        return runTime;
    }

    public void setRunTime(double runTime) {
        this.runTime = runTime;
    }

}
