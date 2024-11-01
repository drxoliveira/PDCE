import timeit as tm
import numpy as np
import sys as ss

from model import StudyMap
from model import Solution
from functions import Function

class Greedy_Algorithm:

    def __init__(self, study_map=StudyMap(), gr=0):
    
        self.study_map = study_map
        self.gr = gr
                
        self.solutions = list()
        self.best_solution = Solution(list(), list(), .0, .0)
        
        self.runtime = .0
    
    def execute(self): 
      
        start_time = tm.default_timer()
        
        self.generate_greedy_solutions()
                
        self.set_runtime(tm.default_timer() - start_time)
    
    def generate_greedy_solutions(self):
        
        maximum_size = round(self.study_map.get_total_units() * self.gr)    

        for u in self.study_map.get_array_units():   
            
            vertices = list()
            vertices.append(u)
            
            variables = np.zeros((self.study_map.get_total_units(),), dtype=int)
            variables[u] = 1
            
            solution = Solution(vertices, variables, Function.scan_statistics(self.study_map, vertices), .0)                         
                
            x = 0
            
            check = True             
            
            while check:
               
                max_scan_statistics = -ss.float_info.max                                 
                x = -1 if x > -1 else x -1
                
                for v in self.study_map.get_units_adjacency_list()[u]:
                    
                    if v not in solution.get_vertices():                      
                        
                        solution.get_vertices().append(v)
                        solution.get_variables()[v] = 1
                        
                        solution.set_scan_statistics_value(Function.scan_statistics(self.study_map, solution.get_vertices()))
                        
                        if max_scan_statistics < solution.get_scan_statistics_value():
                            max_scan_statistics = solution.get_scan_statistics_value()
                            x = v
                        solution.get_vertices().remove(v)                                          
                        solution.get_variables()[v] = 0
                                
                if x > -1:

                    if len(solution.get_vertices()) < maximum_size:                        
                        solution.get_vertices().append(x)
                        solution.get_variables()[x] = 1
                        solution.set_scan_statistics_value(max_scan_statistics)
                        u = x                      
                    else:
                        check = False      
                        
                else:                    
                   u = solution.get_vertices()[len(solution.get_vertices()) + x]         
                       
            self.solutions.append(solution)
                    
            if self.best_solution.get_scan_statistics_value() < solution.get_scan_statistics_value():
                self.best_solution.copy(solution)
                           
    def set_solutions(self, solutions):
        self.solutions = solutions

    def get_solutions(self):
        return self.solutions    
    
    def set_best_solution(self, best_solution):
        self.best_solution = best_solution

    def get_best_solution(self):
        return self.best_solution     
    
    def set_runtime(self, runtime):
        self.runtime = runtime

    def get_runtime(self):
        return self.runtime       
