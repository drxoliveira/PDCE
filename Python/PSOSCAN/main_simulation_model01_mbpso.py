import timeit as tm
import math as mt
import numpy as ny

from resources import ReadFile
from resources import Resource
from model import Solution
from metaheuristic import ModifiedBinaryParticleSwarmOptimization

def execute_null_hypothesis_simulations(study_map, cases, simulations_h0, file):    
    
    print("H0 - Null Hypothesis Simulations")
    
    file.write("H0 - Null Hypothesis Simulations")
    file.write("\n")
    
    ni = 1000

    np = study_map.get_total_units()
    nd = study_map.get_total_units()

    phi = 2.01
    w = 1.00/((phi -1.00) + mt.sqrt(phi * phi -2.00 * phi))
        
    g_max =  8.00
    g_min = -8.00
    
    c1 = phi * w
    c2 = phi * w
        
    gr = 0.03
    lm = 1.00
    
    t_max = 0.10
    t_min = 0.10      
    
    s_max = 0.20
    
    results_h0 = list()
    time_array = ny.zeros(simulations_h0, dtype=float)    

    for s in range(simulations_h0):        
        
        if s % 1000 == 0:
            print( f"Simulation: {s + 1}")
            file.write( f"Simulation: {s + 1}")
            file.write("\n")
        
        study_map_h0 = Resource.load_cases_map(study_map, cases, s)      

        start_time = tm.default_timer()

        mbpso = ModifiedBinaryParticleSwarmOptimization(study_map=study_map_h0, # study map
                                                    ni=ni, # number of iterations.
                                                    np=np, # number of particles.
                                                    nd=nd, # number of dimensions.
                                                    w=w,   # inertia component.                                              
                                                    g_max=g_max, # maximum genotype.
                                                    g_min=g_min, # minimum genotype.
                                                    c1=c1, # cognitive component.
                                                    c2=c2, # social component.
                                                    gr=gr, # maximum size of greedy solutions.
                                                    lm=lm, # limit of non-dominated solutions for g_best.
                                                    t_max=t_max, # maximum turbulence coverage
                                                    t_min=t_min, # minimum turbulence coverage
                                                    s_max = s_max, # solution max size.
                                                    verbose=False) 
        mbpso.execute()            
    
        time_array[s] = tm.default_timer() - start_time  
        
        best_solution = Solution()
        best_solution.copy(mbpso.get_best_solution())

        # Resource.plot_zone(best_solution.get_vertices(), study_map, name="QLearning"+str(s), save_figure=True)
        # Resource.print_solution(best_solution)           

        best_solution.set_vertices(None)
        results_h0.append(best_solution)       
    
    print(f"Time Mean: {round(ny.mean(time_array), 4)}, Time Standard Deviation: {round(ny.std(time_array), 4)}")     
    print() 
            
    file.write(f"Time Mean: {round(ny.mean(time_array), 4)}, Time Standard Deviation: {round(ny.std(time_array), 4)}")     
    file.write("\n") 

    return results_h0

def execute_alternative_hypothesis_simulations(study_map, cases, label, cluster, simulations_h1, results_h0, file):
    
    print(f"Cluster {label} - Alternative Hypothesis Simulations")
    
    file.write(f"Cluster {label} - Alternative Hypothesis Simulations")
    file.write("\n")

    ni = 1000

    np = study_map.get_total_units()
    nd = study_map.get_total_units()

    phi = 2.01
    w = 1.00/((phi -1.00) + mt.sqrt(phi * phi -2.00 * phi))
        
    g_max =  8.00
    g_min = -8.00
    
    c1 = phi * w
    c2 = phi * w
        
    gr = 0.03
    lm = 1.00
    
    t_max = 0.10
    t_min = 0.10      
    
    s_max = 0.20

    results_h1 = list()
    time_array = ny.zeros(simulations_h1, dtype=float)
    
    for s in range(simulations_h1):        
        
        if s % 1000 == 0:
            print( f"Simulation: {s + 1}")            
            file.write( f"Simulation: {s + 1}")
            file.write("\n")
        
        study_map_h1 = Resource.load_cases_map(study_map, cases, s)
        
        start_time = tm.default_timer()

        mbpso = ModifiedBinaryParticleSwarmOptimization(study_map=study_map_h1, # study map
                                                    ni=ni, # number of iterations.
                                                    np=np, # number of particles.
                                                    nd=nd, # number of dimensions.
                                                    w=w,   # inertia component.                                              
                                                    g_max=g_max, # maximum genotype.
                                                    g_min=g_min, # minimum genotype.
                                                    c1=c1, # cognitive component.
                                                    c2=c2, # social component.
                                                    gr=gr, # maximum size of greedy solutions.
                                                    lm=lm, # limit of non-dominated solutions for g_best.
                                                    t_max=t_max, # maximum turbulence coverage
                                                    t_min=t_min, # minimum turbulence coverage
                                                    s_max = s_max, # solution max size.
                                                    verbose=False) 
        mbpso.execute()    
            
        time_array[s] = tm.default_timer() - start_time            
        
        best_solution = Solution()
        best_solution.copy(mbpso.get_best_solution()) 

        # Resource.plot_zone(best_solution.get_vertices(), study_map, name="QLearning"+str(s), save_figure=True)
        # Resource.print_solution(best_solution)  
        
        results_h1.append(best_solution)        

    print(f"Time Mean: {round(ny.mean(time_array), 4)}, Time Standard Deviation: {round(ny.std(time_array), 4)}")      

    file.write(f"Time Mean: {round(ny.mean(time_array), 4)}, Time Standard Deviation: {round(ny.std(time_array), 4)}")  
    file.write("\n")

    mean, std = Resource.calc_detection_power(results_h0, results_h1, p=95)
    print(f"Power Mean: {mean}, Power Standard Deviation: {std}")
    
    file.write(f"Power Mean: {mean}, Power Standard Deviation: {std}")
    file.write("\n")

    mean, std = Resource.calc_ppv(study_map, results_h0, results_h1, simulations_h1, cluster, p=95)
    print(f"PPV Mean: {mean}, PPV Standard Deviation: {std}") 
    
    file.write(f"PPV Mean: {mean}, PPV Standard Deviation: {std}") 
    file.write("\n")

    mean, std = Resource.calc_sensitivity(study_map, results_h0, results_h1, simulations_h1, cluster, p=95)
    print(f"Sensitivity Mean: {mean}, Sensitivity Standard Deviation: {std}")  
    
    file.write(f"Sensitivity Mean: {mean}, Sensitivity Standard Deviation: {std}")  
    file.write("\n")

    mean, std = Resource.calc_accuracy(study_map, results_h0, results_h1, simulations_h1, cluster, p=95)
    print(f"Accuracy Mean: {mean}, Accuracy Standard Deviation: {std}")
    
    file.write(f"Accuracy Mean: {mean}, Accuracy Standard Deviation: {std}")
    file.write("\n")

    mean, std = Resource.calc_f_score(study_map, results_h0, results_h1, simulations_h1, cluster, p=95)
    print(f"F-Score Mean: {mean}, F-Score Standard Deviation: {std}")
    
    file.write(f"F-Score Mean: {mean}, F-Score Standard Deviation: {std}")
    file.write("\n")
    
def main():
   
    prefix_path = "../"    
    
    file = open(prefix_path + "main_simulation_model02_mbpso20.txt", "w")

    read_file = ReadFile(file_path= prefix_path + "data/artificial/Map_Model02.txt")
    study_map = read_file.get_study_map()    

    simulations_h0 = 10000
    simulations_h1 = 5000

    # H0 - Null Hypothesis Simulations    
    study_map.set_total_cases(900)
    cases = prefix_path + "data/artificial/Null_Hypothesis_Model02.txt"

    results_h0 = execute_null_hypothesis_simulations(study_map, cases, simulations_h0, file)       
    
    # Cluster A - Alternative Hypothesis Simulations       
    cluster = [28, 33, 34, 40, 45, 46, 52] 
    cases = prefix_path + "data/artificial/Cluster_A_Model02.txt"        
  
    execute_alternative_hypothesis_simulations(study_map, cases, "A", cluster, simulations_h1, results_h0, file)

    # Cluster B - Alternative Hypothesis Simulations
    cluster = [16, 21, 22, 28, 33, 34, 40, 45, 46, 52]
    cases = prefix_path + "data/artificial/Cluster_B_Model02.txt"

    execute_alternative_hypothesis_simulations(study_map, cases, "B", cluster, simulations_h1, results_h0, file)

    # Cluster C - Alternative Hypothesis Simulations
    cluster = [16, 28, 40, 52, 64, 76]
    cases = prefix_path + "data/artificial/Cluster_C_Model02.txt"

    execute_alternative_hypothesis_simulations(study_map, cases, "C", cluster, simulations_h1, results_h0, file)

    # Cluster D - Alternative Hypothesis Simulations
    cluster = [23, 29, 34, 40, 45, 51]
    cases = prefix_path + "data/artificial/Cluster_D_Model02.txt"

    execute_alternative_hypothesis_simulations(study_map, cases, "D", cluster, simulations_h1, results_h0, file)

    # Cluster E - Alternative Hypothesis Simulations
    cluster = [21, 23, 28, 29, 34, 40, 41, 45, 47]
    cases = prefix_path + "data/artificial/Cluster_E_Model02.txt"

    execute_alternative_hypothesis_simulations(study_map, cases, "E", cluster, simulations_h1, results_h0, file)

    # Cluster F - Alternative Hypothesis Simulations
    cluster = [28, 29, 34, 35, 40, 42, 52]
    cases = prefix_path + "data/artificial/Cluster_F_Model02.txt"

    execute_alternative_hypothesis_simulations(study_map, cases, "F", cluster, simulations_h1, results_h0, file)

    # Cluster G - Alternative Hypothesis Simulations
    cluster = [28, 33, 35, 40, 41, 45, 47, 53]
    cases = prefix_path + "data/artificial/Cluster_G_Model02.txt"

    execute_alternative_hypothesis_simulations(study_map, cases, "G", cluster, simulations_h1, results_h0, file)

    # Cluster H - Alternative Hypothesis Simulations
    cluster = [27, 30, 33, 35, 39, 42, 45, 47]
    cases = prefix_path + "data/artificial/Cluster_H_Model02.txt"

    execute_alternative_hypothesis_simulations(study_map, cases, "H", cluster, simulations_h1, results_h0, file)

    file.close()

if __name__ == '__main__':   
    main()    
