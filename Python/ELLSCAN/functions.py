import math as mt
import numpy as np
from model import StudyMap
from model import Solution
from model import Point

class Function:

    PENALTY_VALUE = -1.0

    @staticmethod
    def distance_two_points(a=Point(.0,.0), b=Point(.0,.0)):
        return mt.sqrt(((b.get_x() -a.get_x()) * (b.get_x() -a.get_x())) + ((b.get_y() -a.get_y()) * (b.get_y() -a.get_y())))
    
    @staticmethod
    def scan_statistics(study_map=StudyMap(), zone=list()):

        if not zone:            
            return Function.PENALTY_VALUE        
        
        else:            
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

        if solution.get_scan_statistics_value() > Function.PENALTY_VALUE:          
            return True
           
        return False