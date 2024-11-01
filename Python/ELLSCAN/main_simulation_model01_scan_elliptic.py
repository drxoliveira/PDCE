import timeit as tm
import numpy as np

from method import ScanElliptic
from model import Solution
from resources import ReadFile
from resources import Resource

def execute_null_hypothesis_simulations(study_map, cases, simulations_h0):

    print("H0 - Null Hypothesis Simulations")

    eccentricity  = [1, 1.5, 2, 3, 4]
    number_angles = [1, 4, 6, 9, 12]
    size_window = 0.20

    results_h0 = list()
    time_array = np.zeros(simulations_h0, dtype=float)    

    for s in range(simulations_h0):        
        
        if s % 1000 == 0:
            print( f"Simulation: {s + 1}")
        
        study_map_h0 = Resource.load_cases_map(study_map, cases, s)      
    
        start_time = tm.default_timer() 

        scanElliptic = ScanElliptic(study_map=study_map_h0, size_window=size_window, number_angles=number_angles, eccentricity=eccentricity)
        scanElliptic.execute()        
    
        time_array[s] = tm.default_timer() - start_time

        best_solution = Solution() 
        best_solution.copy(scanElliptic.get_best_solution())

        # Resource.plot_zone(best_solution.get_vertices(), study_map, name="QLearning"+str(s), save_figure=True)
        # Resource.print_solution(best_solution)           

        best_solution.set_vertices(None)
        results_h0.append(best_solution)       
                
    print(f"Time Mean: {np.mean(time_array)}, Time Standard Deviation: {np.std(time_array)}")     
    print() 

    return results_h0

def execute_alternative_hypothesis_simulations(study_map, cases, label, cluster, simulations_h1, results_h0):
    
    print(f"Cluster {label} - Alternative Hypothesis Simulations")

    eccentricity  = [1, 1.5, 2, 3, 4]
    number_angles = [1, 4, 6, 9, 12]
    size_window = 0.20

    results_h1 = list()
    time_array = np.zeros(simulations_h1, dtype=float)
    
    for s in range(simulations_h1):        
        
        if s % 1000 == 0:
            print( f"Simulation: {s + 1}")
        
        study_map_h1 = Resource.load_cases_map(study_map, cases, s)     
        
        start_time = tm.default_timer()

        scanElliptic = ScanElliptic(study_map=study_map_h1, size_window=size_window, number_angles=number_angles, eccentricity=eccentricity)
        scanElliptic.execute() 

        time_array[s] = tm.default_timer() - start_time    
        
        best_solution = Solution() 
        best_solution.copy(scanElliptic.get_best_solution())  

        # Resource.plot_zone(best_solution.get_vertices(), study_map, name="QLearning"+str(s), save_figure=True)
        # Resource.print_solution(best_solution)  
        
        results_h1.append(best_solution)        

    print(f"Time Mean: {np.mean(time_array)}, Time Standard Deviation: {np.std(time_array)}")  

    mean, std = Resource.calc_detection_power(results_h0, results_h1, simulations_h1, p=95)
    print(f"Power Mean: {mean}, Power Standard Deviation: {std}")

    mean, std = Resource.calc_ppv(study_map, results_h0, results_h1, simulations_h1, cluster, p=95)
    print(f"PPV Mean: {mean}, PPV Standard Deviation: {std}") 

    mean, std = Resource.calc_sensitivity(study_map, results_h0, results_h1, simulations_h1, cluster, p=95)
    print(f"Sensitivity Mean: {mean}, Sensitivity Standard Deviation: {std}")  

    mean, std = Resource.calc_accuracy(study_map, results_h0, results_h1, simulations_h1, cluster, p=95)
    print(f"Accuracy Mean: {mean}, Accuracy Standard Deviation: {std}")

    mean, std = Resource.calc_f_score(study_map, results_h0, results_h1, simulations_h1, cluster, p=95)
    print(f"F-Score Mean: {mean}, F-Score Standard Deviation: {std}")
    print()

def main():

    prefix_path = "../data/"

    read_file = ReadFile(file_path= prefix_path + "Map_Model02.txt")
    study_map = read_file.get_study_map()    

    simulations_h0 = 10000
    simulations_h1 = 5000

    # H0 - Null Hypothesis Simulations
    
    study_map.set_total_cases(900)
    cases = prefix_path +"Null_Hypothesis_Model02.txt"

    results_h0 = execute_null_hypothesis_simulations(study_map, cases, simulations_h0)       
    
    # Cluster A - Alternative Hypothesis Simulations       
    cluster = [28, 33, 34, 40, 45, 46, 52] 
    cases = prefix_path +"Cluster_A_Model02.txt"        
  
    execute_alternative_hypothesis_simulations(study_map, cases, "A", cluster, simulations_h1, results_h0)

    # Cluster B - Alternative Hypothesis Simulations
    cluster = [16, 21, 22, 28, 33, 34, 40, 45, 46, 52]
    cases = prefix_path +"Cluster_B_Model02.txt"

    execute_alternative_hypothesis_simulations(study_map, cases, "B", cluster, simulations_h1, results_h0)

    # Cluster C - Alternative Hypothesis Simulations
    cluster = [16, 28, 40, 52, 64, 76]
    cases = prefix_path +"Cluster_C_Model02.txt"

    execute_alternative_hypothesis_simulations(study_map, cases, "C", cluster, simulations_h1, results_h0)

    # Cluster D - Alternative Hypothesis Simulations
    cluster = [23, 29, 34, 40, 45, 51]
    cases = prefix_path +"Cluster_D_Model02.txt"

    execute_alternative_hypothesis_simulations(study_map, cases, "D", cluster, simulations_h1, results_h0)

    # Cluster E - Alternative Hypothesis Simulations
    cluster = [21, 23, 28, 29, 34, 40, 41, 45, 47]
    cases = prefix_path +"Cluster_E_Model02.txt"

    execute_alternative_hypothesis_simulations(study_map, cases, "E", cluster, simulations_h1, results_h0)

    # Cluster F - Alternative Hypothesis Simulations
    cluster = [28, 29, 34, 35, 40, 42, 52]
    cases = prefix_path +"Cluster_F_Model02.txt"

    execute_alternative_hypothesis_simulations(study_map, cases, "F", cluster, simulations_h1, results_h0)

    # Cluster G - Alternative Hypothesis Simulations
    cluster = [28, 33, 35, 40, 41, 45, 47, 53]
    cases = prefix_path +"Cluster_G_Model02.txt"

    execute_alternative_hypothesis_simulations(study_map, cases, "G", cluster, simulations_h1, results_h0)

    # Cluster H - Alternative Hypothesis Simulations
    cluster = [27, 30, 33, 35, 39, 42, 45, 47]
    cases = prefix_path +"Cluster_H_Model02.txt"

    execute_alternative_hypothesis_simulations(study_map, cases, "H", cluster, simulations_h1, results_h0)

if __name__ == '__main__':   
    main()    
