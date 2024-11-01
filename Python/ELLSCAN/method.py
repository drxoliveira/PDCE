import numpy as np
import timeit as tm
import math as mt
import sys

from model import StudyMap
from model import Solution
from functions import Function

from sklearn.metrics.pairwise import euclidean_distances

class ScanElliptic:
    
    def __init__(self, study_map=StudyMap(), size_window=.0, number_angles=.0, eccentricity=.0):    
        
        self.study_map = study_map     
       
        self.max_solution_size = round(size_window * self.study_map.get_total_units()) 
        
        self.number_angles = number_angles       
        self.eccentricity = eccentricity        
        self.best_solution = Solution(list(), .0, .0)
        self.solutions = list()
        self.run_time = .0
    
    def execute(self):
        
        start_time = tm.default_timer()
        
        self.generate_angles()        
        
        self.generate_elliptic_window_solution()        
        
        self.set_run_time(tm.default_timer() -start_time)
    
    def generate_angles(self):    
        
        e = len(self.eccentricity)
        a = max(self.number_angles)
        
        self.angles = np.zeros((e, a), dtype=float)
        
        for i in range(e):
            for j in range(self.number_angles[i] + 1):
                self.angles[i][j -1] = (mt.pi * j)/(self.number_angles[i])
                
    def generate_elliptic_window_solution(self):        
          
        bar = [.0, .0]
        
        for u in self.study_map.get_units():
            
            bar[0] = bar[0] + u.get_centroid().get_x() 
            bar[1] = bar[1] + u.get_centroid().get_y()
            
        bar[0] = bar[0] / self.study_map.get_total_units()
        bar[1] = bar[1] / self.study_map.get_total_units()
        
        coordx = np.zeros((self.study_map.get_total_units(), 1), dtype=float)
        coordy = np.zeros((self.study_map.get_total_units(), 1), dtype=float)
        
        for i in range(len(self.eccentricity)):            
            for j in range(self.number_angles[i] + 1):
                
                cosa = mt.cos(self.angles[i][j -1])  
                sina = mt.sin(self.angles[i][j -1])  
                
                for k in range(self.study_map.get_total_units()):
                    
                    cx = self.study_map.get_units()[k].get_centroid().get_x()
                    cy = self.study_map.get_units()[k].get_centroid().get_y()                   
                    coordx[k]= bar[0] + cosa * (cx -bar[0]) + sina * (cy -bar[1])
                    coordy[k]= bar[1] - sina * (cx -bar[0]) + cosa * (cy -bar[1])
                    coordx[k]= bar[0] + (coordx[k] -bar[0]) * self.eccentricity[i]
                    coordy[k]= bar[1] + (coordy[k] -bar[1]) * 1.0
                                
                edc = euclidean_distances(np.concatenate((coordx, coordy), axis=1))                
                
                units_index = np.argsort(edc)            
                
                units_select = units_index[:, 0:self.max_solution_size]                
                        
                for k in range(self.study_map.get_total_units()):                                          
                    
                    vertices = list()
                    
                    for l in units_select[k,:]:
                        vertices.append(l)                        
                                        
                    solution = Solution(vertices, Function.scan_statistics(self.study_map, vertices), .0)
                    self.solutions.append(solution)
                    
                    if solution.get_scan_statistics_value() > self.best_solution.get_scan_statistics_value():
                        self.best_solution.copy(solution)                               

    def set_solutions(self, solutions):
        self.solutions = solutions

    def get_solutions(self):
        return self.solutions
    
    def set_best_solution(self, best_solution):
        self.best_solution = best_solution

    def get_best_solution(self):
        return self.best_solution
    
    def set_run_time(self, run_time):        
        self.run_time = run_time

    def get_run_time(self):        
        return self.run_time

class Greedy_Algorithm:

    def __init__(self, study_map=StudyMap(), size_solution=0):

        self.study_map = study_map
        self.size_solution = size_solution

        self.best_solution = Solution(list(), .0, .0)
        self.distance_units = list()

        self.unit = -1
        self.run_time = .0
    
    def execute(self):

        start_time = tm.default_timer() 

        self.generate_solutions()

        self.set_run_time(tm.default_timer() -start_time)
    
    def generate_solutions(self):

        max_size = round(self.study_map.get_total_units() * self.size_solution)    

        for unit in self.study_map.get_array_units():    

            vertices = list()
            vertices.append(unit)

            solution = Solution(vertices, Function.scan_statistics(self.study_map, vertices), .0)           

            if Function.check_solution_restrictions(solution):              
                
                u = unit
                x = 0

                check = True 

                while check:

                    max_scan_statistics = -sys.float_info.max
                    x = -1 if x > -1 else x -1

                    for v in self.study_map.get_units_adjacency_list()[u]:
                        
                        if v not in solution.get_vertices():
                            
                            solution.get_vertices().append(v)
                            solution.set_scan_statistics_value(Function.scan_statistics(self.study_map, solution.get_vertices()))
                            
                            if max_scan_statistics < solution.get_scan_statistics_value():
                                max_scan_statistics = solution.get_scan_statistics_value()
                                x = v

                            solution.get_vertices().remove(v)

                    if x > -1:

                        if len(solution.get_vertices()) < max_size:                        
                            solution.get_vertices().append(x)
                            solution.set_scan_statistics_value(max_scan_statistics)
                            u = x
                        else:
                            check = False
                    
                    else:
                        u = solution.get_vertices()[len(solution.get_vertices()) + x]              
            
            if self.best_solution.get_scan_statistics_value() < solution.get_scan_statistics_value():
                self.best_solution.copy(solution)
    
    def set_unit(self, unit):
        self.unit = unit

    def get_unit(self):
        return self.unit

    def set_best_solution(self, best_solution):
        self.best_solution = best_solution

    def get_best_solution(self):
        return self.best_solution    

    def set_run_time(self, run_time):        
        self.run_time = run_time

    def get_run_time(self):        
        return self.run_time
  