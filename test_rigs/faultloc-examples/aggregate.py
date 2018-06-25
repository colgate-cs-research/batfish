import os

top_level_dir = "containers"

# Open master file

# List every experiment
experiments = os.listdir(top_level_dir)
for experiment in experiments:
    experiment_dir = os.path.join(top_level_dir, experiment)

    # List every network
    networks = os.listdir(experiment_dir)
    for network in networks:
        network_dir = os.path.join(experiment_dir, network)
    
        # List every scenario
        scenarios = os.listdir(os.path.join(network_dir, 'testrigs'))
        for scenario in scenarios:
            scenario_file = os.path.join(network_dir, 'testrigs', scenario, 'experiments')
            # TODO: Open scenario file
            # TODO: For each line in scenario file
            # TODO: Add experiment, network, and scenario to line
            # TODO: Add line to master file

# Close master file
