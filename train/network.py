"""Class that represents the network to be evolved."""
import random
import logging
from train import train_and_score
import sys

class Network():
    """Represent a network and let us operate on it.

    Currently only works for an MLP.
    """

    def __init__(self, nn_param_choices=None):
        """Initialize our network.

        Args:
            nn_param_choices (dict): Parameters for the network, includes:
                nb_neurons (list): [64, 128, 256]
                nb_layers (list): [1, 2, 3, 4]
                activation (list): ['relu', 'elu']
                optimizer (list): ['rmsprop', 'adam']
        """
        self.accuracy = 0.
        self.loss = 1.
        self.model = None
        self.fit = 0.
        self.nn_param_choices = nn_param_choices
        self.network = {}  # (dic): represents MLP network parameters


    def fitOrZero(self):
        if self.model is not None:
            return self.fit
        else:
            return 0

    def create_random(self):
        """Create a random network."""
        self.network['epochs'] = random.randint(1, self.nn_param_choices['epochs'] - 1)
        self.network['activation'] = random.choice(self.nn_param_choices['activation'])
        self.network['optimizer'] = random.choice(self.nn_param_choices['optimizer'])
        self.network['nb_layers'] = random.randint(1, self.nn_param_choices['nb_layers'] - 1)
        self.network['nb_neurons'] =[]

        for layer in range(0, self.network['nb_layers']):
            self.network['nb_neurons'].append(random.randint(1, self.nn_param_choices['nb_neurons'] - 1))

    def create_set(self, network):
        """Set network properties.

        Args:
            network (dict): The network parameters

        """
        self.network = network

    def train(self, dataset, count, total):
        """Train the network and record the accuracy.

        Args:
            dataset (str): Name of dataset to use.

        """
        if self.accuracy == 0.:
            print('Training Network (%2d/%2d) %s'  % (count, total, self.print()))
            self.loss, self.accuracy, self.model = train_and_score(self.network, dataset)
            self.fit = 1 / self.loss if self.loss != 0 else sys.maxint
        else:
            print('   Using Network (%2d/%2d) %s - accuracy: %.2f%%,  - loss: %.4f' % (count, total, self.print(), self.accuracy * 100, self.loss))

    def print_network(self):
        """Print out a network."""
        logging.info(self.print())
        logging.info("Network accuracy: %.2f%%, loss: %.4f  " % (self.accuracy * 100, self.loss))

    def print(self):
        res = ""

        res += "{ "

        res += (" activation: '" + self.network['activation'] + "'").ljust(22, " ")+", "
        res += (" optimizer: '" + self.network['optimizer'] + "'").ljust(22, " ")+", "
        res += " epochs: " + str(self.network['epochs']).rjust(5, " ")+", "
        res += (" layers: " + str(self.network['nb_neurons']))

        res += " }"

        return res
