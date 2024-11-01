from resources import ReadFile
from resources import Resource
from method import ScanElliptic

def main():

    print("Scan Elliptic for Spatial Cluster Detection")

    read_file = ReadFile(file_path="../data/NE.txt")
    study_map = read_file.get_study_map()

    eccentricity  = [1, 1.5, 2, 3, 4]
    number_angles = [1, 4, 6, 9, 12]
    
    size_window = 0.20

    scanElliptic = ScanElliptic(study_map=study_map, size_window=size_window, number_angles=number_angles, eccentricity=eccentricity)
    scanElliptic.execute()

    Resource.print_solution(scanElliptic.get_best_solution())
    print(f"Runtime= {scanElliptic.get_run_time():.4f} \n")
    Resource.plot_zone(scanElliptic.get_best_solution().get_vertices(), study_map, name="QLearning", save_figure=False)

if __name__ == '__main__':    
    main()
