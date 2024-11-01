import numpy as np
import timeit as tm
import random as rd

from model import StudyMap
from model import Solution
from functions import Function

class QLearning:
    
    def __init__(self, study_map=StudyMap(), unit=-1, episodes=0, steps=0, alpha=.0, gamma=.0, epsilon=.0, verbose=False):
        
        self.study_map = study_map
        self.unit = unit

        self.NUMBER_ACTIONS =  2
        self.ACTION_ZERO    =  0
        self.ACTION_ONE     =  1

        self.Q = np.zeros((study_map.get_total_units(), self.NUMBER_ACTIONS), dtype=float)
        
        self.rewards_training   = list()
        self.penalties_training = list()

        self.rewards_test   = list()
        self.penalties_test = list()

        self.verbose = verbose
        self.improv  = .0

        self.episodes = episodes
        self.steps    = steps
        self.alpha    = alpha
        self.gamma    = gamma
        
        self.epsilon  = 1.0000
        self.epsilon_min   = epsilon
        self.epsilon_decay = 0.9950  
                 
        self.init_states(-1)

        self.run_time = .0   

    def init_states(self, sign=0):

        if sign < 0:

            self.states = list()
                        
            for v in self.study_map.get_units_distance_list()[self.unit][0:self.steps + 1]:                
                self.states.append(v)        
        
        else:
            return self.states[sign] 

    def train(self):

        if self.verbose:
                print("Training")

        start_time = tm.default_timer()

        self.init_environment()

        self.penalties_training = np.zeros(self.episodes, dtype=int)
        self.rewards_training = np.zeros(self.episodes, dtype=float)

        for episode in range(self.episodes):

            state = self.init_states(0)
            penalty = 0
            
            if self.verbose:
                print(f" Episode: {episode + 1} ")

            for step in range(1, self.steps + 1):

                action = self.epsilon_greedy_action_selection(state) 
                nstate = self.next_state(state, action, episode, step, True)                      
            
                self.Q[state][action] = self.Q[state][action] + self.alpha * (self.rewards_training[episode] + self.gamma * (max(self.Q[nstate][0], self.Q[nstate][1])) - self.Q[state][action])

                state = nstate                               

            self.penalties_training[episode] = penalty            

            if self.verbose:
                print(f"episode: {episode + 1} - {self.episodes}, ", end="")
                print(f"score: {self.environment.get_scan_statistics_value():.4f}, ", end="")
                print(f"epsilon: {self.epsilon:.4f} ", end="")
                print(f"penalty: {self.penalties_training[episode]}")                  

            if episode > 0:               
                for i in range(episode):                   
                    if self.rewards_training[episode] > self.rewards_training[i]:                       
                        self.improv = self.improv + 1.0        

        self.set_runtime(tm.default_timer() - start_time)
    
    def test(self, episodes=1):

        if self.verbose:
                print("Test")

        self.rewards_test = np.zeros(episodes, dtype=float)
        self.penalties_test = np.zeros(episodes, dtype=int)

        self.init_environment()

        for episode in range(episodes):

            state = self.init_states(0)
            penalty = 0

            if self.verbose:
                print(f" Episode: {episode + 1} ")
            
            for step in range(1, self.steps+1):

                if self.Q[state][0] >= self.Q[state][1]:
                    action = self.ACTION_ZERO
                else:
                    action = self.ACTION_ONE 
                
                state = self.next_state(state, action, episode, step, False)               

                if self.rewards_test[episode] == Function.PENALTY_VALUE:
                    penalty = penalty + 1
            
            self.penalties_test[episode] = penalty
    
    def init_environment(self):       
        self.environment = Solution(list(), .0, .0)

    def epsilon_greedy_action_selection(self, state):

        if rd.uniform(0, 1) < self.epsilon:
            action = rd.randint(0, 1)
        else:
            if self.Q[state][0] >= self.Q[state][1]:
                action = self.ACTION_ZERO
            else:
                action = self.ACTION_ONE   

        if self.epsilon > self.epsilon_min:               
                self.epsilon = self.epsilon * self.epsilon_decay    

        return action

    def return_next_state(self, sign):
        return self.init_states(sign)

    def next_state(self, state, action, episode, step, training):
 
        if action == self.ACTION_ONE:            
            if state not in self.environment.get_vertices():
                self.environment.get_vertices().append(state)
        else:            
            if state in self.environment.get_vertices():
                self.environment.get_vertices().remove(state)

        if self.environment.get_vertices():

            y = self.environment.get_scan_statistics_value()

            self.environment.set_scan_statistics_value(
                Function.scan_statistics(study_map=self.study_map, zone=self.environment.get_vertices()))
               
            if self.environment.get_scan_statistics_value() < y:

                if action == self.ACTION_ONE:
                    self.environment.get_vertices().remove(state)
                else:
                    self.environment.get_vertices().append(state)

                self.environment.set_scan_statistics_value(y)
                
                if training:
                    self.rewards_training[episode] = Function.PENALTY_VALUE
                else:
                    self.rewards_test[episode] = Function.PENALTY_VALUE         

            else:                     
                if training:
                    self.rewards_training[episode] = self.environment.get_scan_statistics_value()
                else:
                    self.rewards_test[episode] = self.environment.get_scan_statistics_value()  

        else:            
            if training:
                self.rewards_training[episode] = Function.PENALTY_VALUE
            else:
                self.rewards_test[episode] = Function.PENALTY_VALUE

        return self.return_next_state(step)
    
    def set_unit(self, unit):
        self.unit = unit

    def get_unit(self):
        return self.unit
    
    def set_Q(self, Q):
        self.Q = Q

    def get_Q(self):
        return self.Q
    
    def set_states_init(self, states_init):
        self.states_init = states_init
    
    def get_states_init(self):
        return self.states_init

    def set_rewards_training(self, rewards_training):
        self.rewards_training = rewards_training

    def get_rewards_training(self):
        return self.rewards_training
    
    def set_penalties_training(self, penalties_training):
        self.penalties_training = penalties_training

    def get_penalties_training(self):
        return self.penalties_training
    
    def set_environment(self, environment):
        self.environment = environment

    def get_environment(self):
        return self.environment
    
    def set_improv(self, improv):
        self.improv = improv

    def get_improv(self):
        return self.improv

    def set_runtime(self, runtime):
        self.runtime = runtime

    def get_runtime(self):
        return self.runtime
    
    def set_episodes(self, episodes):
        self.episodes = episodes  

    def get_episodes(self):
        return self.episodes
    
    def set_steps(self, steps):
        self.steps = steps

    def get_steps(self):
        return self.steps
    
    def set_alpha(self, alpha):
        self.alpha = alpha

    def get_alpha(self):
        return self.alpha
    
    def set_gamma(self, gamma):
        self.gamma = gamma

    def get_gamma(self):
        return self.gamma
    
    def set_epsilon(self, epsilon):
        self.epsilon = epsilon

    def get_epsilon(self):
        return self.epsilon

    def set_rewards_test(self, rewards_test):
        self.rewards_test = rewards_test

    def get_rewards_test(self):
        return self.rewards_test

    def set_penalties_test(self, penalties_test):
        self.penalties_test = penalties_test

    def get_penalties_test(self):
        return self.penalties_test

    def copy(self, qLearning): 
        
        self.unit = qLearning.get_unit()

        self.Q = qLearning.get_Q().copy() 
        self.states_init = qLearning.get_states_init().copy()       

        self.episodes = qLearning.get_episodes()
        self.steps    = qLearning.get_steps()
        self.alpha    = qLearning.get_alpha()
        self.gamma    = qLearning.get_gamma()
        self.epsilon  = qLearning.get_epsilon()

        self.improv = qLearning.get_improv()

        self.rewards_training   =  qLearning.get_rewards_training().copy() 
        self.penalties_training = qLearning.get_penalties_training().copy() 

        self.rewards_test   = qLearning. get_rewards_test().copy() 
        self.penalties_test = qLearning.get_penalties_test().copy()

class SARSA:
    
    def __init__(self, study_map=StudyMap(), unit=-1, episodes=0, steps=0, alpha=.0, gamma=.0, epsilon=.0, verbose=False):
        
        self.study_map = study_map
        self.unit = unit

        self.NUMBER_ACTIONS =  2
        self.ACTION_ZERO    =  0
        self.ACTION_ONE     =  1

        self.Q = np.zeros((study_map.get_total_units(), self.NUMBER_ACTIONS), dtype=float)
        
        self.rewards_training   = list()
        self.penalties_training = list()

        self.rewards_test   = list()
        self.penalties_test = list()

        self.verbose = verbose
        self.improv  = .0

        self.episodes = episodes
        self.steps    = steps
        self.alpha    = alpha
        self.gamma    = gamma

        self.epsilon  = 1.0000
        self.epsilon_min   = epsilon
        self.epsilon_decay = 0.9950  

        self.init_states(-1)

        self.run_time = .0   

    def init_states(self, sign=0):

        if sign < 0:

            self.states = list()
                        
            for v in self.study_map.get_units_distance_list()[self.unit][0:self.steps + 1]:                
                self.states.append(v)        
        
        else:
            return self.states[sign] 

    def train(self):

        if self.verbose:
                print("Training")

        start_time = tm.default_timer()

        self.init_environment()

        self.penalties_training = np.zeros(self.episodes, dtype=int)
        self.rewards_training = np.zeros(self.episodes, dtype=float)

        for episode in range(self.episodes):

            state = self.init_states(0)
            action = self.epsilon_greedy_action_selection(state)
            penalty = 0
            
            if self.verbose:
                print(f" Episode: {episode + 1} ")

            for step in range(1, self.steps +1):

                nstate  = self.next_state(state, action, episode, step, True) 
                naction = self.epsilon_greedy_action_selection( nstate)    
                
                self.Q[state][action] = self.Q[state][action] + self.alpha * (self.rewards_training[episode] + self.gamma * self.Q[nstate][naction] - self.Q[state][action])

                state  = nstate
                action = naction                           

            self.penalties_training[episode] = penalty

            if self.verbose:
                print(f"episode: {episode + 1}-{self.episodes}, ", end="")
                print(f"score: {self.environment.get_scan_statistics_value():.4f}, ", end="")
                print(f"epsilon: {self.epsilon:.4f} ", end="")
                print(f"penalty: {self.penalties_training[episode]}")                  

            if episode > 0:               
                for i in range(episode):                   
                    if self.rewards_training[episode] > self.rewards_training[i]:                       
                        self.improv = self.improv + 1.0

        self.set_runtime(tm.default_timer() - start_time)
    
    def test(self, episodes=1):

        if self.verbose:
                print("Test")

        self.rewards_test = np.zeros(episodes, dtype=float)
        self.penalties_test = np.zeros(episodes, dtype=int)

        self.init_environment()

        for episode in range(episodes):

            state = self.init_states(0)
            penalty = 0

            if self.verbose:
                print(f" Episode: {episode + 1} ")
            
            for step in range(1, self.steps + 1):

                if self.Q[state][0] >= self.Q[state][1]:
                    action = self.ACTION_ZERO
                else:
                    action = self.ACTION_ONE 
                
                state = self.next_state(state, action, episode, step, False)               

                if self.rewards_test[episode] == Function.PENALTY_VALUE:
                    penalty = penalty + 1
            
            self.penalties_test[episode] = penalty
    
    def init_environment(self):       
        self.environment = Solution(list(), .0, .0)

    def epsilon_greedy_action_selection(self, state):

        if rd.uniform(0, 1) < self.epsilon:
            action = rd.randint(0, 1)
        else:
            if self.Q[state][0] >= self.Q[state][1]:
                action = self.ACTION_ZERO
            else:
                action = self.ACTION_ONE

        if self.epsilon > self.epsilon_min:               
            self.epsilon = self.epsilon * self.epsilon_decay

        return action

    def return_next_state(self, sign):
        return self.init_states(sign)

    def next_state(self, state, action, episode, step, training):
 
        if action == self.ACTION_ONE:            
            if state not in self.environment.get_vertices():
                self.environment.get_vertices().append(state)
        else:            
            if state in self.environment.get_vertices():
                self.environment.get_vertices().remove(state)

        if self.environment.get_vertices():

            y = self.environment.get_scan_statistics_value()

            self.environment.set_scan_statistics_value(
                Function.scan_statistics(study_map=self.study_map,zone=self.environment.get_vertices()))
               
            if self.environment.get_scan_statistics_value() < y:

                if action == self.ACTION_ONE:
                    self.environment.get_vertices().remove(state)
                else:
                    self.environment.get_vertices().append(state)

                self.environment.set_scan_statistics_value(y)
                
                if training:
                    self.rewards_training[episode] = Function.PENALTY_VALUE
                else:
                    self.rewards_test[episode] = Function.PENALTY_VALUE         

            else:                     
                if training:
                    self.rewards_training[episode] = self.environment.get_scan_statistics_value()
                else:
                    self.rewards_test[episode] = self.environment.get_scan_statistics_value()  

        else:            
            if training:
                self.rewards_training[episode] = Function.PENALTY_VALUE
            else:
                self.rewards_test[episode] = Function.PENALTY_VALUE

        return self.return_next_state(step)
    
    def set_unit(self, unit):
        self.unit = unit

    def get_unit(self):
        return self.unit
    
    def set_Q(self, Q):
        self.Q = Q

    def get_Q(self):
        return self.Q
    
    def set_states_init(self, states_init):
        self.states_init = states_init
    
    def get_states_init(self):
        return self.states_init

    def set_rewards_training(self, rewards_training):
        self.rewards_training = rewards_training

    def get_rewards_training(self):
        return self.rewards_training
    
    def set_penalties_training(self, penalties_training):
        self.penalties_training = penalties_training

    def get_penalties_training(self):
        return self.penalties_training
    
    def set_environment(self, environment):
        self.environment = environment

    def get_environment(self):
        return self.environment
    
    def set_improv(self, improv):
        self.improv = improv

    def get_improv(self):
        return self.improv  

    def set_runtime(self, runtime):
        self.runtime = runtime

    def get_runtime(self):
        return self.runtime
    
    def set_episodes(self, episodes):
        self.episodes = episodes  

    def get_episodes(self):
        return self.episodes
    
    def set_steps(self, steps):
        self.steps = steps

    def get_steps(self):
        return self.steps
    
    def set_alpha(self, alpha):
        self.alpha = alpha

    def get_alpha(self):
        return self.alpha
    
    def set_gamma(self, gamma):
        self.gamma = gamma

    def get_gamma(self):
        return self.gamma
    
    def set_epsilon(self, epsilon):
        self.epsilon = epsilon

    def get_epsilon(self):
        return self.epsilon

    def set_rewards_test(self, rewards_test):
        self.rewards_test = rewards_test

    def get_rewards_test(self):
        return self.rewards_test

    def set_penalties_test(self, penalties_test):
        self.penalties_test = penalties_test

    def get_penalties_test(self):
        return self.penalties_test
    
    def copy(self, sarsa): 
    
        self.unit = sarsa.get_unit()

        self.Q = sarsa.get_Q().copy() 
        self.states_init = sarsa.get_states_init().copy()       

        self.episodes = sarsa.get_episodes()
        self.steps    = sarsa.get_steps()
        self.alpha    = sarsa.get_alpha()
        self.gamma    = sarsa.get_gamma()
        self.epsilon  = sarsa.get_epsilon()

        self.improv = sarsa.get_improv()

        self.rewards_training   =  sarsa.get_rewards_training().copy() 
        self.penalties_training = sarsa.get_penalties_training().copy() 

        self.rewards_test   = sarsa. get_rewards_test().copy() 
        self.penalties_test = sarsa.get_penalties_test().copy()