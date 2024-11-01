import math as mt
import sys as ss

from model import StudyMap
from model import Solution
from model import Point

class Function:

    PENALTY_VALUE = -1.0

    @staticmethod
    def distance_two_points(a=Point(.0,.0), b=Point(.0,.0)):
        return mt.sqrt(((b.get_x() -a.get_x()) * (b.get_x() -a.get_x())) + ((b.get_y() -a.get_y()) * (b.get_y() -a.get_y())))
    
    @staticmethod
    def depth_first_search(study_map=StudyMap(), visited={}, zone=list(), v=0):
        
        visited[v] = 1
    
        for u in study_map.get_units_adjacency_list()[v]:    
            if u in zone:    
                if visited[u] == -1:    
                    Function.depth_first_search(study_map, visited, zone, u)
   
    @staticmethod
    def check_connection_dfs(study_map=StudyMap(), zone=list()):
        
        if not zone:            
            return True 
        
        visited = {}
        for v in zone:
            visited[v] = -1
            
        Function.depth_first_search(study_map, visited, zone, zone[0])
        
        check = False

        for v in zone:
            if visited[v] == -1:
                check = True            
                break

        return check
    
    @staticmethod
    def scan_statistics(study_map=StudyMap(), zone=list()):

        if not zone:            
            return Function.PENALTY_VALUE        
        
        if Function.check_connection_dfs(study_map, zone):
            return Function.PENALTY_VALUE
                   
        pz = 0
        cz = 0         
        
        for v in zone:
            pz +=  study_map.get_units()[v].get_population()
            cz += study_map.get_units()[v].get_cases()
        
        P = study_map.get_total_population()
        C = study_map.get_total_cases()

        if cz > (C / P) * pz:
            return cz * mt.log(cz / (pz * C / P)) + (C - cz) * mt.log((C - cz) / (C - (pz * C / P)))
        else:
            return Function.PENALTY_VALUE
        
    @staticmethod
    def check_solution_restrictions(solution=Solution()):        
        
        if len(solution.get_vertices()) == 0:
            return False
        
        if solution.get_scan_statistics_value() == Function.PENALTY_VALUE:          
            return False       
                    
        return True
    
    @staticmethod
    def check_solution_not_in_list(solution_x=Solution(), solutions=list()):
        
        check = True
        
        x1 = solution_x.get_scan_statistics_value()
        y1 = solution_x.get_penalty_value()
        
        for solution_y in solutions:
            
            x2 = solution_y.get_scan_statistics_value()
            y2 = solution_y.get_penalty_value()
            
            if mt.isclose(x1, x2) and mt.isclose(y1, y2):
                
                check = False
                
                for v in solution_x.get_vertices():
                    if v not in solution_y.get_vertices():
                        check = True
                        break
                
                for v in solution_y.get_vertices():
                    if v not in solution_x.get_vertices():
                        check = True
                        break
                        
        return check
        
    @staticmethod
    def disperion_penalty(study_map=StudyMap(), zone=list()):
        
        max_x = -ss.float_info.max
        min_x =  ss.float_info.max
    
        max_y = -ss.float_info.max
        min_y =  ss.float_info.max
    
        for v in zone:  
            
            centroid = study_map.get_units()[v].get_centroid()

            if centroid.get_x() > max_x:
                max_x = centroid.get_x()
                
            if centroid.get_x() < min_x:
                min_x = centroid.get_x()
                
            if centroid.get_y() > max_y:
                max_y = centroid.get_y()
            
            if centroid.get_y() < min_y:
                min_y = centroid.get_y()
                
        d1 = max_x - min_x
        d2 = max_y - min_y

        if (d1 + d2) != .0:
            return  -1.0 * ((2.0 * (d1 * d2)) / (d1 + d2))
        else: 
            return .0
    
    @staticmethod
    def sigmoid(x):
        return 1.0 / (1.0 + mt.exp(-x))
    
    @staticmethod
    def check_dominance_relation(solution_x=Solution(), solution_y=Solution()):

        if solution_x.get_scan_statistics_value() > solution_y.get_scan_statistics_value():
            if solution_x.get_penalty_value() >= solution_y.get_penalty_value():
                return True
        
        return False
    
    @staticmethod
    def normalization(x=.0, x_max=.0, x_min=.0):        
        return  (x - x_min) / (x_max - x_min)
    