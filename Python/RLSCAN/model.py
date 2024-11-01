import math as mt

class Solution:

    def __init__(self, vertices=list(), scan_statistics_value=.0, penalty_value=.0):
        self.vertices = vertices
        self.scan_statistics_value = scan_statistics_value
        self.penalty_value = penalty_value

    def set_vertices(self, vertices):
        self.vertices = vertices

    def get_vertices(self):
        return self.vertices
    
    def set_scan_statistics_value(self, scan_statistics_value):
        self.scan_statistics_value = scan_statistics_value
    
    def get_scan_statistics_value(self):
        return self.scan_statistics_value
    
    def set_penalty_value (self, penalty_value):
        self.penalty_value = penalty_value
    
    def get_penalty_value (self):
        return self.penalty_value

    def copy(self, solution):
        self.vertices = solution.get_vertices().copy()       
        self.scan_statistics_value = solution.get_scan_statistics_value()
        self.penalty_value = solution.get_penalty_value()
    
    def is_equal(self, solution):
        
        if not mt.isclose(self.scan_statistics_value, solution.get_scan_statistics_value()):
            if not mt.isclose(self.penalty_value, solution.get_penalty_value()):
                return False        
            
        for v in self.vertices:
            if v not in solution.get_vertices():
                return False
            
        return True

class Point:

    def __init__(self, x=.0, y=.0):
        self.x = x # x coordinate
        self.y = y # y coordinate 

    def set_x(self, x):
        self.x = x

    def get_x(self):
        return self.x

    def set_y(self, y):
        self.y = y

    def get_y(self):
        return self.y

class Unit:

    def __init__(self, population=0, cases=0, centroid=Point(.0, .0), frontier=list()):
        self.population = population # Population unit
        self.cases = cases           # Cases unit
        self.centroid = centroid     # Centroid unit
        self.frontier = frontier     # Set of points (frontier) of unit

    def set_population(self, population):
        self.population = population

    def get_population(self):
        return self.population

    def set_cases(self, cases):
        self.cases = cases

    def get_cases(self):
        return self.cases  

    def set_centroid(self, centroid):
        self.centroid = centroid

    def get_centroid(self):
        return self.centroid
    
    def set_frontier(self, frontier):
        self.frontier = frontier

    def get_frontier(self):
        return self.frontier

class StudyMap:

    def __init__(self, units=list(), array_units=list(), units_adjacency_list=list(), units_distance_list=list(), total_units=0, total_population=0, total_cases=0):
        self.units = units                               # list units
        self.total_units = total_units                   # total units         
        self.units_adjacency_list = units_adjacency_list # adjacency map list
        self.units_distance_list = units_distance_list   # unit distance units                
        self.total_population = total_population         # total population
        self.total_cases = total_cases                   # totaol cases
        self.array_units = array_units            
    
    def set_units(self, units):
        self.units = units

    def get_units(self):
        return self.units    

    def set_total_units(self, total_units):
        self.total_units = total_units

    def get_total_units(self):
        return self.total_units

    def set_units_adjacency_list(self, units_adjacency_list):
        self.units_adjacency_list = units_adjacency_list

    def get_units_adjacency_list(self):
        return self.units_adjacency_list
    
    def set_units_distance_list(self, units_distance_list):
        self.units_distance_list = units_distance_list    

    def get_units_distance_list(self):
        return self.units_distance_list

    def set_total_population(self, total_population):
        self.total_population = total_population

    def get_total_population(self):
        return self.total_population

    def set_total_cases(self, total_cases):
        self.total_cases = total_cases

    def get_total_cases(self):
        return self.total_cases
    
    def set_array_units(self, array_units):
        self.array_units = array_units

    def get_array_units(self):
        return self.array_units    
    