import math as mt
from resources import ReadFile
from resources import Resource

from metaheuristic import ModifiedBinaryParticleSwarmOptimization

def main():

    print("Modified Binary Particle Swarm Optimization for Spatial Clusters Detection")

    read_file = ReadFile(file_path="../data/NE.txt")
    study_map = read_file.get_study_map()

    ni = 1000

    np = study_map.get_total_units()
    nd = study_map.get_total_units()

    phi = 2.01
    w = 1.00 / ((phi -1.00) + mt.sqrt(phi * phi -2.00 * phi))
        
    g_max =  8.00
    g_min = -8.00
    
    c1 = phi * w
    c2 = phi * w
        
    gr = 0.03
    lm = 1.00
    
    t_max = 0.10
    t_min = 0.10 
    
    s_max = 0.20
        
    mbpso = ModifiedBinaryParticleSwarmOptimization(study_map=study_map, # study map.
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
                                               t_max=t_max, # maximum turbulence coverage.
                                               t_min=t_min, # minimum turbulence coverage.
                                               s_max = s_max, # solution max size.
                                               verbose=True) 
    mbpso.execute()
   
    Resource.plot_zone(study_map=study_map, zone=mbpso.get_best_solution().get_vertices(),  name="cluster", save_figure=False)
    Resource.print_solution(solution=mbpso.get_best_solution())
    print(f"runtime= {mbpso.get_runtime():.4f} \n")   
    
    return mbpso
    
if __name__ == '__main__':    
    y = main()
