"""
Class that holds a genetic algorithm for evolving a network.

Credit:
    A lot of those code was originally inspired by:
    http://lethain.com/genetic-algorithms-cool-name-damn-simple/
"""
from functools import reduce
from operator import add
import random
from network import Network
from roulette import choose


class Optimizer():
    """Class that implements genetic algorithm for MLP optimization."""

    def __init__(self, nn_param_choices, crossover_probability=0.2, mutate_chance=0.1):
        """Create an optimizer.

        Args:
            nn_param_choices (dict): Possible network paremters
            retain (float): Percentage of population to retain after
                each generation
            random_select (float): Probability of a rejected network
                remaining in the population
            mutate_chance (float): Probability a network will be
                randomly mutated

        """
        self.mutate_chance = mutate_chance
        self.crossover_probability = crossover_probability
        self.nn_param_choices = nn_param_choices
        self.accuracy = 0.
        self.loss = 1.
        self.model = None
        self.fit = 0.


    def create_population(self, count):
        """Create a population of random networks.

        Args:
            count (int): Number of networks to generate, aka the
                size of the population

        Returns:
            (list): Population of network objects

        """
        pop = []
        for _ in range(0, count):
            # Create a random network.
            network = Network(self.nn_param_choices)
            network.create_random()

            # Add the network to our population.
            pop.append(network)

        return pop

    @staticmethod
    def fitness(network):
        """Return the accuracy, which is our fitness function."""
        return network.fit

    def grade(self, pop):
        """Find average fitness for a population.

        Args:
            pop (list): The population of networks

        Returns:
            (float): The average accuracy of the population

        """
        summed = reduce(add, (self.fitness(network) for network in pop))
        return summed / float((len(pop)))

    def breed(self, mother, father):
        """Make two children as parts of their parents.

        Args:
            mother (dict): Network parameters
            father (dict): Network parameters

        Returns:
            (list): Two network objects

        """
        children = []
        for _ in range(2):

            child = {}

            child['activation'] = random.choice([mother.network['activation'], father.network['activation']])
            child['optimizer'] = random.choice([mother.network['optimizer'], father.network['optimizer']])
            child['epochs'] = random.choice([mother.network['epochs'], father.network['epochs']])
            layer_parent = random.choice([mother, father])
            child['nb_layers'] = layer_parent.network['nb_layers']
            child['nb_neurons'] = []
            child['nb_neurons'].extend(layer_parent.network['nb_neurons'])


            # Now create a network object.
            network = Network(self.nn_param_choices)
            network.create_set(child)

            if self.mutate_chance > random.random():
                network = self.mutate(network)

            children.append(self.mutate(network))

        return children

    def mutate(self, network):
        """Randomly mutate one part of the network.

        Args:
            network (dict): The network parameters to mutate

        Returns:
            (Network): A randomly mutated network object

        """
        # Reset training.
        self.accuracy = 0.
        self.loss = 1.
        self.model = None
        self.fit = 0.

        # Choose a random key.
        mutation = random.choice(list(self.nn_param_choices.keys()))
        # Mutate one of the params.
        if mutation == 'nb_layers':
            network.network[mutation] = random.randint(1, self.nn_param_choices[mutation] - 1)
            network.network['nb_neurons'] = []
            for layer in range(0, network.network['nb_layers']):
                network.network['nb_neurons'].append(random.randint(1, self.nn_param_choices['nb_neurons'] - 1))
        elif  mutation == 'nb_neurons':
            l = random.randint(0, network.network['nb_layers'] - 1)
            network.network['nb_neurons'][l] = random.randint(1, self.nn_param_choices['nb_neurons'] - 1)
        elif  mutation == 'epochs':
            network.network['epochs'] = random.randint(1, self.nn_param_choices['epochs'] - 1)
        else:
            network.network[mutation] = random.choice(self.nn_param_choices[mutation])

        return network

    def evolve(self, pop):
        """Evolve a population of networks.

        Args:
            pop (list): A list of network parameters

        Returns:
            (list): The evolved population of networks

        """

        # Get scores for each network and save best model.
        graded = [(self.fitness(network), network) for network in pop]
        graded = [x[1] for x in sorted(graded, key=lambda x: x[0], reverse=True)]
        print("***************saving model %s - accuracy: %.2f%%,  - loss: %.4f"  %(graded[0].print(), graded[0].accuracy * 100, graded[0].loss))
        graded[0].model.save("ga/model.h5")

        # roulette
        fitness=[]
        for network in pop:
            fitness.append(network.fit)
        parents_idx = choose(fitness)

        temp_parents=[]
        for i in parents_idx:
            temp_parents.append(pop[i])

        parents=[]
        parents_length=len(temp_parents)

        # create next generation
        i = 0
        while len(parents) < parents_length:
            inc = 1
            if random.random() > self.crossover_probability or i == parents_length - 1:
                parents.append(temp_parents[i])
            else:
                # crossover
                # Get current and next population
                inc = 2
                male = temp_parents[i]
                female = temp_parents[i+1]

                # Breed them.
                babies = self.breed(male, female)
                parents.extend(babies)

            i += inc


        return parents
