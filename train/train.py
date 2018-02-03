"""
Utility used by the Network class to actually train.

Based on:
    https://github.com/fchollet/keras/blob/master/examples/mnist_mlp.py

"""
from keras.models import Sequential
from keras.layers import Dense
from keras.callbacks import EarlyStopping
from pbar import PLogger

# Helper: Early stopping.
#early_stopper = EarlyStopping(patience=5)

def compile_model(network, nb_classes, input_shape):
    """Compile a sequential model.

    Args:
        network (dict): the parameters of the network

    Returns:
        a compiled network.

    """
    # Get our network parameters.
    nb_layers = network['nb_layers']
    nb_neurons = network['nb_neurons']
    activation = network['activation']
    optimizer = network['optimizer']

    model = Sequential()

    # Add each layer.
    for i in range(nb_layers):

        # Need input shape for first layer.
        if i == 0:
            model.add(Dense(nb_neurons[i], activation=activation, input_dim=input_shape, name="input"))
        else:
            model.add(Dense(nb_neurons[i], activation=activation))

    # Output layer.
    model.add(Dense(nb_classes, activation='softmax', name="output"))

    model.compile(loss='categorical_crossentropy', optimizer=optimizer, metrics=['accuracy'])

    return model

def train_and_score(network, dataset):
    """Train the model, return test loss.

    Args:
        network (dict): the parameters of the network
        dataset (str): Dataset to use for training/evaluating

    """
    x_train, x_test, y_train, y_test = dataset
    model = compile_model(network, 15, 90)

    class_weight = {
        0: 1.,
        1: 71.,
        2: 74.,
        3: 73.,
        4: 73.,
        5: 66.,
        6: 61.,
        7: 57.,
        8: 57.,
        9: 63.,
        10: 66.,
        11: 66.,
        12: 66.,
        13: 66.,
        14: 64.
    }

    model.fit(x_train, y_train,
              batch_size=100000,
              epochs=network['epochs'],  # using early stopping, so no real limit
              verbose=0,
              class_weight = class_weight,
              validation_data=(x_test, y_test),
              callbacks=[PLogger(step=1)])

    score = model.evaluate(x_test, y_test, verbose=0)

    return score + [model] # 1 is accuracy. 0 is loss.
