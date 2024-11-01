import timeit as tm
import multiprocessing as mp

from reinforcement_learning import SARSA
from resources import ReadFile
from resources import Resource

def main():

    print("SARSA for Irregular Spatial Cluster Detection")

    read_file = ReadFile(file_path="../data/NE.txt")
    study_map = read_file.get_study_map()

    episodes = 1000    
    steps = 25   

    alpha   = 0.4000
    gamma   = 0.5000
    epsilon = 0.0065
    
    sarsa_list = list()
    
    for unit in study_map.get_array_units():        

        sarsa = SARSA(study_map=study_map, unit=unit, episodes=episodes, steps=steps, alpha=alpha, gamma=gamma, epsilon=epsilon, verbose=False)        
    
        sarsa_list.append(sarsa)        
        
    start_time = tm.default_timer()
    
    pool = mp.Pool(processes=mp.cpu_count())    
    result_list = pool.map(Resource.execute_training_test, ((sarsa) for sarsa in sarsa_list))
    pool.close()
    pool.join()
        
    runtime = tm.default_timer() - start_time

    best_solution = Resource.return_best_solution(result_list)
    
    Resource.print_solution(best_solution)
    print(f"Runtime= {runtime:.4f} \n")
    Resource.plot_zone(best_solution.get_vertices(), study_map, name="QLearning", save_figure=False)

    return best_solution

if __name__ == '__main__':   
    y = main()
