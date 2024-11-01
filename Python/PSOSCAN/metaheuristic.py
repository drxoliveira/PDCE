import timeit as tm
import random as rd
import numpy as nm

from model import StudyMap
from model import Solution
from heuristic import Greedy_Algorithm

from functions import Function
   
# ------------------------------------------------------------------------------------------
#  Modified Binary Particle Swarm Optimization for Spatial Clusters Detection
# ------------------------------------------------------------------------------------------

class ModifiedBinaryParticleSwarmOptimization:

    def __init__(self, 
                 study_map=StudyMap(), 
                 ni=0, 
                 np=0, 
                 nd=0, 
                 w=.0,                  
                 g_max=.0, 
                 g_min=.0, 
                 c1=.0, 
                 c2=.0, 
                 gr=.0,
                 lm=.0,
                 t_max=.0,
                 t_min=.0,
                 s_max =.0,
                 verbose=False):

        self.study_map = study_map

        self.ni = ni # number of iterations.
        self.np = np # number of particles.
        self.nd = nd # number of dimensions.
        
        self.w = w  # inertia component.
       
        self.g_max  = g_max  # maximum genotype.
        self.g_min  = g_min  # minimum genotype.

        self.c1 = c1 # cognitive component.
        self.c2 = c2 # social component.

        self.gr = gr # maximum size of greedy solutions.
        self.lm = lm # limit of non-dominated solutions for g_best.
        
        self.t_max = t_max # maximum turbulence coverage.
        self.t_min = t_min # minimum turbulence coverage.
        self.t = t_max     # turbulence coverage.
        
        self.s_max = round(self.study_map.get_total_units() * s_max)
        

        self.swarm = list() # population of particles.
        self.velocities = nm.zeros((self.np, self.nd), dtype=float) # velocities of particles.
        self.genotypes  = nm.zeros((self.np, self.nd), dtype=float) # genotypes of particles.
        
        self.p_best  = list() # p_best guides
        self.g_best  = Solution()
        
        self.best_solution = Solution()
                
        self.DECAY = 0.99
        
        self.verbose = verbose
        self.max_solutions = 0

        self.run_time = .0

    def execute(self):

        start_time = tm.default_timer()

        self.initialize()
        
        for i in range(self.ni):     
                        
            self.update_velocities()    
            
            self.update_position()
            
            self.turbulence_operator() 
            
            self.evaluate()
                      
            self.update_guides()   
            
            if self.verbose:
                
                print(f"iteration: {i +1}", end="; ")  
                print(f"size: {len(self.g_best.get_vertices())}", end="; ")
                print(f"llr: {self.g_best.get_scan_statistics_value():.4f}")
                

        self.set_runtime(tm.default_timer() - start_time)
        
        self.best_solution.copy(self.g_best)
    
    def execute_maximum_solutions(self, max_solutions=0):

        start_time = tm.default_timer()

        self.max_solutions = 0    

        self.initialize()       
        
        i = 0
        
        while self.max_solutions <= max_solutions:     
                        
            self.update_velocities()
            
            self.update_position()
            
            self.turbulence_operator() 
            
            self.evaluate()
                      
            self.update_guides()   
            
            if self.verbose:
                
                print(f"iteration: {i +1}", end="; ")  
                print(f"size: {len(self.g_best.get_vertices())}", end="; ")
                print(f"llr: {self.g_best.get_scan_statistics_value():.4f}", end="; ")
                print(f"solutions: {self.max_solutions}")
                
            i = i + 1    

        self.set_runtime(tm.default_timer() - start_time)
        
        self.get_best_solution().copy(self.g_best)
    
    def initialize(self):

        self.initialize_swarm()        
        self.initialize_velocities()
        self.initialize_genotypes()                
        self.initialize_guides()
    
    def initialize_swarm(self):
        
        greedy_Algorithm = Greedy_Algorithm(study_map=self.study_map, gr=self.gr)
        greedy_Algorithm.execute()
        
        id_solution = 0
        
        for solution_x in greedy_Algorithm.get_solutions():           
            
            solution_y = Solution()
            solution_y.copy(solution_x)             
            
            solution_y.set_id_solution(id_solution) 
            self.swarm.append(solution_y)  
            
            id_solution = id_solution + 1
            
            self.max_solutions = self.max_solutions + 1

    def initialize_velocities(self):
        
        for p in range(self.np):              
            for d in range(self.nd):                        
                self.velocities[p][d] = rd.uniform(self.g_min, self.g_max)
    
    def initialize_genotypes(self):
        
        for p in range(self.np):                 
            for d in range(self.nd):                        
                self.genotypes[p][d] = rd.uniform(self.g_min, self.g_max)
   
    def initialize_guides(self):        
        
        best_solution, updated = self.update_p_best(init=True)
        self.update_g_best(best_solution, updated)        
    
    def update_p_best(self, init=False):
        
        updated = False        
        best_solution = Solution()
        
        if init:        
            
           for solution_x in self.swarm:      
               
               solution_y = Solution()
               solution_y.copy(solution_x)
               
               self.p_best.append(solution_y)
               
               if solution_y.get_scan_statistics_value() > best_solution.get_scan_statistics_value():
                   best_solution.copy(solution_y)
           
           updated = True
               
        else:
            
            for i in range(self.np):   
                
                if self.swarm[i].get_scan_statistics_value() > self.p_best[i].get_scan_statistics_value():                
                    
                    self.p_best[i].copy(self.swarm[i])
                    
                    if self.p_best[i].get_scan_statistics_value() > best_solution.get_scan_statistics_value():
                        best_solution.copy(self.p_best[i])
                        
                    updated = True                 
            
        return best_solution, updated
        
    def update_g_best(self, best_solution=Solution(), updated=True): 
        
        if updated:
              
            if best_solution.get_scan_statistics_value() > self.g_best.get_scan_statistics_value():
                self.g_best.copy(best_solution)                
                                                               
    def update_velocities(self):       
       
        for p in range(self.np):            
            for d in range(self.nd):      
                
                velocity = self.velocities[p][d]
                r1 = rd.uniform(0, 1)
                r2 = rd.uniform(0, 1)
                
                position = float(self.swarm[p].get_variables()[d])
                p_best   = float(self.p_best[p].get_variables()[d])
                g_best   = float(self.g_best.get_variables()[d])
                                               
                self.velocities[p][d] = self.w * velocity + self.c1 * r1 * (p_best -position) + self.c2 * r2 * (g_best -position)
                           
    def update_position(self):
        
        for p in range(self.np):            
            for d in range(self.nd):    
                
                self.genotypes[p][d] = self.genotypes[p][d] + self.velocities[p][d]
                
                if self.genotypes[p][d] > self.g_max:
                   self.genotypes[p][d] = self.g_max
               
                if self.genotypes[p][d] < self.g_min:
                   self.genotypes[p][d] = self.g_min 
                
                if rd.uniform(0, 1) < Function.sigmoid(self.genotypes[p][d]):     
                    
                    if d not in self.swarm[p].get_vertices():
                        self.swarm[p].get_vertices().append(d)
                        self.swarm[p].get_variables()[d] = 1    
                        
                else:                    
                    if d in self.swarm[p].get_vertices():
                       self.swarm[p].get_vertices().remove(d)
                       self.swarm[p].get_variables()[d] = 0 

        self.max_solutions = self.max_solutions + self.np    
                       
    def turbulence_operator(self):     
                
        samples = rd.sample(range(self.np), round(self.np * self.t))
                
        for i in samples:               
                  
            lv = len(self.swarm[i].get_vertices()) 
            
            if lv > 0:     
                
                v = self.swarm[i].get_vertices()[rd.randint(0, lv -1)]
                
                if rd.randint(0, 1) == 1:

                    u = self.study_map.get_units_adjacency_list()[v][rd.randint(0, len(self.study_map.get_units_adjacency_list()[v]) -1)]
                    
                    if u not in self.swarm[i].get_vertices():
                        self.swarm[i].get_vertices().append(u)
                        self.swarm[i].get_variables()[u] = 1                                          
                    
                else:
                    
                    self.swarm[i].get_vertices().remove(v)
                    self.swarm[i].get_variables()[v] = 0
                    
                    # if Function.check_connection_dfs(self.study_map, self.swarm[i].get_vertices()):
                        # self.swarm[i].get_vertices().append(v)
                        # self.swarm[i].get_variables()[v] = 1
                
            else:    
                
                self.swarm[i].copy(self.g_best)
                
                # v = rd.randint(0, self.nd -1)                
                # self.swarm[i].get_vertices().append(v)
                # self.swarm[i].get_variables()[v] = 1 
                                   
        if self.t > self.t_min:               
            self.t = self.t * self.DECAY   
  
    def evaluate(self):
        
        for solution_x in self.swarm:           
                       
            if len(solution_x.get_vertices()) > self.s_max:
                solution_x.set_scan_statistics_value(Function.PENALTY_VALUE)
            else:
                solution_x.set_scan_statistics_value(Function.scan_statistics(study_map=self.study_map, zone=solution_x.get_vertices()))
                    
    def update_guides(self):
        
        best_solution, updated = self.update_p_best(init=False)
        self.update_g_best(best_solution, updated) 
        
    def set_best_solution(self, best_solution=Solution()):
        self.best_solution = best_solution
    
    def get_best_solution(self):
        return self.best_solution
                    
    def set_runtime(self, runtime):
        self.runtime = runtime

    def get_runtime(self):
        return self.runtime
