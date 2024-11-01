import numpy as np
import matplotlib.pyplot as plt
import cv2
import random as rd

from model import StudyMap
from model import Solution
from model import Point
from model import Unit 
from functions import Function

class ReadFile:  

    def __init__(self, file_path=None):        
        self.file_path = file_path
        self.study_map = StudyMap()
    
    def open_file(self):
        self.file = open(self.file_path, "r")
    
    def read_file(self):
        
        total_units = int(self.file.readline())        
        self.study_map.set_total_units(total_units)

        units_adjacency_list = list()

        for i in range(total_units):

            adjacency_list = list()
            s = self.file.readline().split(" ")

            for j in range(1, len(s) -1):
                adjacency_list.append(int(s[j]) -1)

            units_adjacency_list.append(adjacency_list)
        
        self.study_map.set_units_adjacency_list(units_adjacency_list)

        population = list()
        cases = list()
        total_population = 0
        total_cases = 0

        for i in range(total_units):

            s = self.file.readline().split(" ")
            population.append(int(s[1]))
            cases.append(int(s[2]))
            total_population = total_population + population[i]
            total_cases = total_cases + cases[i]
        
        self.study_map.set_total_population(total_population)
        self.study_map.set_total_cases(total_cases)

        centroids = list()

        for i in range(total_units):

            s = self.file.readline().split(" ")
            x = float(s[1])
            y = float(s[2])

            centroid = Point(x, y)
            centroids.append(centroid)

        frontiers = list()

        for i in range(total_units):

            frontier = list()
            s = self.file.readline().split(" ")

            for j in range(1, len(s) -1):
                point = Point(float(s[j]), .0)
                frontier.append(point)

            frontiers.append(frontier)
        
        for i in range(total_units):

            s = self.file.readline().split(" ")

            for j in range(1, len(s) -1):
                frontiers[i][j-1].set_y(float(s[j]))    
        
        units = list()

        for i in range(total_units):
            unit = Unit(population[i], cases[i], centroids[i], frontiers[i])
            units.append(unit)
        
        self.study_map.set_units(units)

        units_distance_list = list()

        for i in range(total_units):

            x = np.zeros((total_units, 2), dtype=float)

            for j in range(total_units):
                x[j, 0] = j
                if i != j:
                    x[j, 1] = Function.distance_two_points(units[i].get_centroid(), units[j].get_centroid())

            x_sorted = x[np.argsort(x[:, 1])]
            units_distance_list.append(x_sorted[:,0].astype(int))
        
        self.study_map.set_units_distance_list(units_distance_list)  
        self.study_map.set_array_units(np.arange(total_units))
        self.study_map.set_maximum_dispersion(-1 * Function.disperion_penalty(self.study_map, self.study_map.get_array_units()))
    
    def close_file(self):
        self.file.close()
    
    def get_study_map(self):
        
        self.open_file()
        self.read_file()
        self.close_file

        return self.study_map

class Resource:
    
    @staticmethod
    def print_solution(solution=Solution()):

        print()
        
        solution.get_vertices().sort()
        
        print("units = [", end="")

        for i in range(len(solution.get_vertices())):

            v = solution.get_vertices()[i] + 1        

            if i != len(solution.get_vertices()) -1:
                print(f"{v},", end=" ")
            else:
                print(f"{v}", end="")
                    
        print("]", end=" ")
        print()
        print(f'size = {len(solution.get_vertices())}')
        print(f'fo1 = {solution.get_scan_statistics_value():.4f}')
        print(f'fo2 = {solution.get_penalty_value():.4f}')
        print(f'fo1/N(fo2) = {solution.get_scan_statistics_value_penalized():.4f}')
        
    @staticmethod
    def plot_zone(study_map=StudyMap(), zone=list(),  name="cluster", save_figure=False):
    
        plt.clf()
        plt.matplotlib.rcParams["figure.dpi"] = 120
        plt.figure(facecolor='white')

        for i in range(study_map.get_total_units()):
        
            x = list()
            y = list()
            
            if i in zone:      
                for point in study_map.get_units()[i].get_frontier():        
                    x.append(point.get_x())
                    y.append(point.get_y())
                plt.fill(x, y, color='#9900cc', linewidth=0)
                
            else:    
                for point in study_map.get_units()[i].get_frontier():        
                    x.append(point.get_x())
                    y.append(point.get_y())
                    
            plt.plot(x, y, 'k', linewidth = 0.5)    
        
        plt.gca().get_xaxis().set_visible(False)
        plt.gca().get_yaxis().set_visible(False)

        if save_figure:
            plt.savefig(name + ".png", dpi=600, format="png")   
            plt.savefig(name + ".eps", dpi=600,  format="eps")    

    
    @staticmethod
    def calc_detection_power(results_h0, results_h1, p=95):

        values_h0 = np.array([sol.get_scan_statistics_value() for sol in results_h0])
        values_h1 = np.array([sol.get_scan_statistics_value() for sol in results_h1])

        critical_value = np.percentile(values_h0, p)
        
        power = np.array([1.0 if value > critical_value else 0.0 for value in values_h1])

        return round(np.mean(power), 4), round(np.std(power), 4)
    
    @staticmethod
    def calc_ppv(study_map, results_h0, results_h1, simulations_h1, cluster, p=95):

        real_cluster = list()
        for u in cluster:
            real_cluster.append(u -1)

        values_h0 = np.array([sol.get_scan_statistics_value() for sol in results_h0])
        values_h1 = np.array([sol.get_scan_statistics_value() for sol in results_h1])

        critical_value = np.percentile(values_h0, p)     
             
        solutions = list()
        
        for i in range(simulations_h1):

            if values_h1[i] > critical_value:
                solution = Solution(list(), .0, .0)
                solution.copy(results_h1[i])
                solutions.append(solution)

        ppv_array = np.zeros(len(solutions), dtype=float)

        i = 0
        for solution in solutions:

            population_detected_cluster = .0
            for u in solution.get_vertices():
                population_detected_cluster = population_detected_cluster + study_map.get_units()[u].get_population()

            intersection = list()

            for u in solution.get_vertices():
                if u in real_cluster:
                    intersection.append(u)

            population_intersection = .0

            for u in intersection:
                population_intersection = population_intersection + study_map.get_units()[u].get_population()
            
            ppv_array[i] = population_intersection/population_detected_cluster
            i = i +1

        return round(np.mean(ppv_array), 4), round(np.std(ppv_array), 4) 
    
    @staticmethod
    def calc_sensitivity( study_map, results_h0, results_h1, simulations_h1, cluster, p=95):

        real_cluster = list()
        for u in cluster:
            real_cluster.append(u -1)

        values_h0 = np.array([sol.get_scan_statistics_value() for sol in results_h0])
        values_h1 = np.array([sol.get_scan_statistics_value() for sol in results_h1])

        critical_value = np.percentile(values_h0, p)     
             
        solutions = list()
        
        for i in range(simulations_h1):

            if values_h1[i] > critical_value:
                solution = Solution(list(), .0, .0)
                solution.copy(results_h1[i])
                solutions.append(solution)
              
        population_real_cluster = .0

        sensitivity_array = np.zeros(len(solutions), dtype=float)

        for u in real_cluster:
            population_real_cluster = population_real_cluster + study_map.get_units()[u].get_population()       

        i = 0
        for solution in solutions:
            
            intersection = list()

            for u in solution.get_vertices():
                if u in real_cluster:   
                    intersection.append(u)
            
            population_intersection = .0

            for u in intersection:
                population_intersection = population_intersection + study_map.get_units()[u].get_population()

            sensitivity_array[i] = population_intersection/population_real_cluster
            i = i + 1

        return round(np.mean(sensitivity_array), 4), round(np.std(sensitivity_array), 4)
    
    @staticmethod
    def calc_accuracy( study_map, results_h0, results_h1, simulations_h1, cluster, p=95):
    
        real_cluster = list()
        for u in cluster:
            real_cluster.append(u -1)

        values_h0 = np.array([sol.get_scan_statistics_value() for sol in results_h0])
        values_h1 = np.array([sol.get_scan_statistics_value() for sol in results_h1])

        critical_value = np.percentile(values_h0, p)     
             
        solutions = list()
        
        for i in range(simulations_h1):

            if values_h1[i] > critical_value:
                solution = Solution(list(), .0, .0)
                solution.copy(results_h1[i])
                solutions.append(solution)
            
        accuracy_array = np.zeros(len(solutions), dtype=float)

        i = 0
        for solution in solutions:        

            intersection1 = list()

            for u in solution.get_vertices():
                if u in real_cluster:   
                    intersection1.append(u)
        
            nao_real_cluster = list()
            nao_solution = list()

            for u in range(study_map.get_total_units()):
                if u not in real_cluster:   
                    nao_real_cluster.append(u)
            
            for u in range(study_map.get_total_units()):
                if u not in solution.get_vertices():
                    nao_solution.append(u)
            
            intersection2 = list()

            for u in nao_real_cluster:
                if u in nao_solution:   
                    intersection2.append(u)

            uniao = list()

            for u in intersection1:
                if u not in uniao:
                    uniao.append(u)
            
            for u in intersection2:
                if u not in uniao:
                    uniao.append(u)

            population = .0            
            for u in uniao:
                population = population + study_map.get_units()[u].get_population()

            accuracy_array[i] = population/study_map.get_total_population()   
            i = i + 1
                        
        return round(np.mean(accuracy_array), 4), round(np.std(accuracy_array), 4)

    @staticmethod
    def calc_f_score(study_map, results_h0, results_h1, simulations_h1, cluster, p=95):

        real_cluster = list()
        for u in cluster:
            real_cluster.append(u -1)

        values_h0 = np.array([sol.get_scan_statistics_value() for sol in results_h0])
        values_h1 = np.array([sol.get_scan_statistics_value() for sol in results_h1])

        critical_value = np.percentile(values_h0, p)     
             
        solutions = list()
        
        for i in range(simulations_h1):

            if values_h1[i] > critical_value:
                solution = Solution(list(), .0, .0)
                solution.copy(results_h1[i])
                solutions.append(solution)

        f_scrore_array = np.zeros(len(solutions), dtype=float)    

        population_real_cluster = .0
        for u in real_cluster:
            population_real_cluster = population_real_cluster + study_map.get_units()[u].get_population()
        
        i = 0
        for solution in solutions:

            population_detected_cluster = .0
            for u in solution.get_vertices():
                population_detected_cluster = population_detected_cluster + study_map.get_units()[u].get_population()
            
            intersection = list()

            for u in solution.get_vertices():
                  if u in real_cluster:
                      intersection.append(u)

            population_intersection = .0
            for u in intersection:
                population_intersection = population_intersection + study_map.get_units()[u].get_population() 
            
            recall = (population_intersection / population_real_cluster)
            precision = (population_intersection / population_detected_cluster)

            if (precision + recall) > .0:
                f_scrore_array[i] = 2.0 * ((precision * recall) / (precision + recall))

            i = i + 1

        return round(np.mean(f_scrore_array), 4), round(np.std(f_scrore_array), 4)
        
    @staticmethod
    def load_cases_map(study_map, cases, j):

        total_units = study_map.get_total_units()    
        total_population = study_map.get_total_population()
        total_cases = study_map.get_total_cases()
        array_units = study_map.get_array_units().copy()
        units_adjacency_list = study_map.get_units_adjacency_list().copy()
        units_distance_list = study_map.get_units_distance_list().copy()
        maximum_dispersion = study_map.get_maximum_dispersion()        

        units = list()

        for i in range(total_units):

            population = study_map.get_units()[i].get_population()
            centroid = study_map.get_units()[i].get_centroid()
            frontier = study_map.get_units()[i].get_frontier()
            
            unit = Unit(population, 0, centroid, frontier)
            units.append(unit)
        
        study_map_out = StudyMap(units=units, 
                                array_units=array_units, 
                                units_adjacency_list=units_adjacency_list, 
                                units_distance_list=units_distance_list, 
                                total_units=total_units,
                                total_population=total_population, 
                                total_cases=total_cases,
                                maximum_dispersion=maximum_dispersion)

        file = open(cases, "r")
    
        for _ in range(j):
            file.readline()

        s = file.readline().split(" ") 

        total_cases = 0
        
        for j in range(total_units):
            study_map_out.get_units()[j].set_cases(int(s[j]))  
            total_cases = total_cases + int(s[j])    

        study_map_out.set_total_cases(total_cases)

        file.close()

        return study_map_out

class WriteFile:

    def __init__(self, file_name=None):
        self.file_name = file_name
    
    def open_file(self):
        self.file = open(self.file_name, "w")

    def write_result(self, solutions=list(), runtime=.0):

        self.file.write(str(len(solutions)))
        self.file.write("\n")

        for solution in solutions:
            self.file.write(str(solution.get_scan_statistics_value()) + " \t " + str(solution.get_penalty_value()))
            self.file.write("\n")        

        for solution in solutions:
            for v in solution.get_vertices():
                self.file.write(str(v+1) + " ")
            self.file.write("\n")
        
        self.file.write(str(round(runtime, 4)))
        self.file.write("\n")
            
    def close_file(self):
        self.file.close()
    
