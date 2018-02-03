"""Entry point to evolving the neural network. Start here."""
import logging
from optimizer import Optimizer
from dataset import getDataset
import numpy as np

# Setup logging.
logging.basicConfig(
    format='%(asctime)s - %(levelname)s - %(message)s',
    datefmt='%m/%d/%Y %I:%M:%S %p',
    level=logging.DEBUG,
    filename='log.txt',
    filemode='w'
)

def train_networks(networks, dataset):
    """Train each network.

    Args:
        networks (list): Current population of networks
        dataset (str): Dataset to use for training/evaluating
    """
    count=0
    for network in networks:
        count+=1
        network.train(dataset, count, len(networks))

def get_averages(networks):
    """Get the average accuracy for a group of networks.

    Args:
        networks (list): List of networks

    Returns:
        float: The average accuracy of a population of networks.

    """
    total_accuracy = 0
    total_loss = 0
    for network in networks:
        total_accuracy += network.accuracy
        total_loss += network.loss

    return [total_accuracy / len(networks), total_loss / len(networks)]

def generate(generations, population, nn_param_choices, dataset):
    """Generate a network with the genetic algorithm.

    Args:
        generations (int): Number of times to evole the population
        population (int): Number of networks in each generation
        nn_param_choices (dict): Parameter choices for networks
        dataset (str): Dataset to use for training/evaluating

    """
    optimizer = Optimizer(nn_param_choices)
    networks = optimizer.create_population(population)

    # Evolve the generation.
    for i in range(generations):
        logging.info("")
        logging.info("")
        logging.info("***Doing generation %d of %d***" % (i + 1, generations))

        print("\n\n\n**************************************")
        print("***Generation %d/%d" % (i + 1, generations))
        print("**************************************\n\n")

        # Train and get accuracy for networks.
        train_networks(networks, dataset)

        # Get the average accuracy for this generation.
        average_accuracy, average_loss = get_averages(networks)

        # Print out the average accuracy each generation.
        logging.info("Generation average: %.2f%% (%.4f)" % (average_accuracy * 100, average_loss ))
        logging.info('-'*80)

        # Evolve, except on the last iteration.
        if i != generations - 1:
            # Do the evolution.
            networks = optimizer.evolve(networks)
            copy_accuracies(networks)


    # Sort our final population.
    networks = sorted(networks, key=lambda x: x.accuracy, reverse=True)

    # Print out the top 5 networks.
    print_networks(networks[:5])

def copy_accuracies(networks):
    for network in networks:
        if(network.accuracy == 0.):
            copy_accuracy_from_trained(network, networks)

def lists_equal(a, b):
    if len(a) != len(b):
        return False

    for i in range(len(a)):
        if a[i] != b[i]:
            return False

    return True


def copy_accuracy_from_trained(network, networks):
    for n in  networks:
        if n.accuracy > 0.:
            if (n.network['epochs'] == network.network['epochs'] and
                    n.network['activation'] == network.network['activation'] and
                    n.network['optimizer'] == network.network['optimizer'] and
                    lists_equal(n.network['nb_neurons'], network.network['nb_neurons'])):
                network.accuracy = n.accuracy
                network.model = n.model
                network.loss = n.loss
                network.fit = n.fit
                break



def print_networks(networks):
    """Print a list of networks.

    Args:
        networks (list): The population of networks

    """
    logging.info('-'*80)
    for network in networks:
        network.print_network()

def main():
    """Evolve a network."""
    generations = 1000  # Number of times to evole the population.
    population = 25  # Number of networks in each generation.

    nn_param_choices = {
        'epochs': 3000,
        'nb_neurons': 4000,
        'nb_layers': 2,
        'activation': ['relu', 'elu', 'tanh', 'sigmoid'],
        'optimizer': ['rmsprop', 'adam', 'sgd', 'adagrad',
                      'adadelta', 'adamax', 'nadam'],
    }

    logging.info("***Evolving %d generations with population %d***" %
                 (generations, population))

    data = getDataset()
    generate(generations, population, nn_param_choices, data)

if __name__ == '__main__':
    main()
