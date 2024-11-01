package pdcerl.reinforcementlearning.singleobjective;

import pdcerl.model.Solution;
import pdcerl.model.StudyMap;
import pdcerl.model.Unit;
import pdcerl.resources.Function;
import pdcerl.resources.IConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// -----------------------------------------------------------------------------
//  SARSA for Irregular Spatial Cluster Detection
// -----------------------------------------------------------------------------

public class SARSA implements IConstant {

    public StudyMap studyMap;
    public Unit unit;

    public List<Integer> states;

    private final double [][] q;
    private final double [] rewards;
    private double reward;

    public int episodes;
    public int steps;
    public double alpha;
    public double gamma;
    public double epsilon;

    public Solution environment;

    public int numberStates;
    public double currentEpsilon;

    public boolean verbose;
    public int improv;

    public double runTime;

    public SARSA (StudyMap studyMap, Unit unit, int episodes, int steps, double alpha, double gamma, double epsilon,
                      boolean verbose) {

        this.studyMap = studyMap;
        this.unit = unit;

        this.episodes = episodes;
        this.alpha = alpha;
        this.steps = steps;
        this.gamma = gamma;
        this.epsilon = epsilon;

        this.numberStates = this.studyMap.getTotalUnits();
        this.currentEpsilon = IConstant.MAX_EPSILON;

        this.q = new double[this.numberStates][IConstant.NUMBER_ACTIONS];
        this.rewards = new double[this.episodes];

        this.verbose = verbose;
        this.improv = 0;

        this.initStates(-1);

    }

    public int initStates(int sign) {

        if (sign < 0) {

            this.states = new ArrayList<>();

            for (int i = 0; i < this.steps + 1; i++) {
                this.states.add(this.studyMap.getMapOfUnitsShortestDistanceByUnit().get(this.unit.getId()).get(i));
            }

            return -1;

        } else {
            return this.states.get(sign);
        }
    }

    public void training() {

        long startTime = System.currentTimeMillis();

        this.initEnvironment();

        for (int episode = 0; episode < this.episodes; episode++) {

            int state = this.returnState(0);
            int action = this.epsilonGreedyActionSelectionMethod(state);

            for (int step = 1; step < this.steps + 1; step++) {

                int nstate = this.nextState(state, action, step, true);
                int naction = this.epsilonGreedyActionSelectionMethod(state);

                this.q[state][action] = this.q[state][action] + this.alpha *
                        (this.reward + this.gamma * (this.q[nstate][naction]) -this.q[state][action]);

                state  = nstate;
                action = naction;
            }

            this.rewards[episode] = this.environment.getScanStatisticsValue();

            if (this.verbose) {
                System.out.println("Episode: " + (episode + 1) + " LLR: " + this.rewards[episode]);
            }

            if (episode > 0) {
                for (int i = 0; i < episode; i++) {
                    if (this.rewards[episode] > this.rewards[i]) {
                        this.improv = this.improv + 1;
                    }
                }
            }

        }

        this.setRunTime((double)(System.currentTimeMillis() - startTime)/ 1000.0);

        if(this.verbose) {
            System.out.println(this.getEnvironment());
            System.out.println("Runtime Training: " + this.getRunTime() + " seconds.");
        }

    }

    public void initEnvironment() {
        this.environment = new Solution();
    }

    public int returnState(int sign) {
        return initStates(sign);
    }

    public int epsilonGreedyActionSelectionMethod(int state) {

        Random random = new Random();
        int action;

        if (random.nextDouble() < this.currentEpsilon) {
            action = random.nextInt(2);
        } else {
            action = (this.q[state][IConstant.ACTION_ZERO] >= this.q[state][IConstant.ACTION_ONE])
                    ? IConstant.ACTION_ZERO
                    : IConstant.ACTION_ONE;
        }

        if (this.currentEpsilon > this.epsilon) {
            this.currentEpsilon *= IConstant.EPSILON_DECAY;
        }

        return action;

    }

    public int nextState(int state, int action, int step, boolean training) {

        this.reward = 0.0;

        this.updateVertices(state, action);

        if (!this.environment.getVertices().isEmpty()) {

            double scanStatisticsValue = this.environment.getScanStatisticsValue();

            this.environment.setScanStatisticsValue(
                    Function.scanStatisticsEvaluation(this.studyMap, this.environment.getVertices()));

            if (this.environment.getScanStatisticsValue() < scanStatisticsValue) {

                if (action == IConstant.ACTION_ONE) {
                    this.updateVertices(state, IConstant.ACTION_ZERO);
                } else {
                    this.updateVertices(state, IConstant.ACTION_ONE);
                }

                this.environment.setScanStatisticsValue(scanStatisticsValue);

                if (training) {
                    this.reward = IConstant.PENALTY_VALUE;
                }

            } else {
                if (training) {
                    this.reward = this.environment.getScanStatisticsValue();
                }
            }


        } else {
            if (training) {
                this.reward = IConstant.PENALTY_VALUE;
            }
        }

        return this.returnState(step);
    }

    public void updateVertices(int state, int action) {

        if (action == IConstant.ACTION_ONE) {
            if (!this.environment.getVertices().contains(state)) {
                this.environment.getVertices().add(state);
            }
        } else {
            if (this.environment.getVertices().contains(state)) {
                this.environment.getVertices().remove((Integer) state);
            }
        }
    }

    public void test() {

        this.initEnvironment();

        int state = this.returnState(0);
        int action;

        for (int step = 1; step < this.steps + 1; step++) {

            action = (this.q[state][IConstant.ACTION_ZERO] >= this.q[state][IConstant.ACTION_ONE])
                    ? IConstant.ACTION_ZERO
                    : IConstant.ACTION_ONE;

            updateVertices(state, action);

            this.environment.setScanStatisticsValue(
                    Function.scanStatisticsEvaluation(this.studyMap, this.environment.getVertices()));

            state = this.returnState(step);
        }

        if(this.verbose) {
            System.out.println(this.getEnvironment());
        }

    }

    public void executeTrainingTest() {
        this.training();
        this.test();
    }

    public static Solution returnBestEnvironment(List<SARSA> sarsaList) {

        int index = 0;
        double maxValue = 0;
        int indexMaxValue = 0;

        for (SARSA sarsa: sarsaList) {
            if (sarsa.getEnvironment().getScanStatisticsValue() > maxValue) {
                maxValue = sarsa.getEnvironment().getScanStatisticsValue();
                indexMaxValue = index;
            }
            index++;
        }

        Solution solution = new Solution();
        solution.copy(sarsaList.get(indexMaxValue).getEnvironment());

        return solution;
    }

    public static int returnIndexBestEnvironment(List<SARSA> sarsaList) {

        int index = 0;
        double maxValue = 0;
        int indexMaxValue = 0;

        for (SARSA sarsa: sarsaList) {
            if (sarsa.getEnvironment().getScanStatisticsValue() > maxValue) {
                maxValue = sarsa.getEnvironment().getScanStatisticsValue();
                indexMaxValue = index;
            }
            index++;
        }

        return indexMaxValue;
    }

    public Solution getEnvironment() {
        return environment;
    }

    public int getImprov() {
        return improv;
    }

    public void setImprov(int improv) {
        this.improv = improv;
    }

    public double getRunTime() {
        return runTime;
    }

    public void setRunTime(double runTime) {
        this.runTime = runTime;
    }

}
