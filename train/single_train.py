from keras.layers import Dense
from pbar import PLogger
from autosaver import AutoSaver
from dataset import getDataset
from keras.models import Sequential, load_model
from keras.losses import categorical_crossentropy

import pickle
import os


FILENAME='model/model.h5'
EPOCHS_FILENAME='model/epochs.pkl'

TOTAL_EPOCHS=844

x_train, x_test, y_train, y_test = getDataset()
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

if os.path.exists(FILENAME) and os.path.isfile(FILENAME) and os.path.exists(EPOCHS_FILENAME) and os.path.isfile(EPOCHS_FILENAME):
    #Load previous model and continue training
    model = load_model(FILENAME)
    score = model.evaluate(x_test, y_test, verbose=0)
    initial_epochs = pickle._load(open( EPOCHS_FILENAME, "rb" ))
    print("Initial network accuracy: %.2f%%, loss: %.4f, epochs: %5d " % (score[1] * 100, score[0], initial_epochs))
else:
    # Create new model
    model = Sequential()
    model.add(Dense(3032, activation="sigmoid", input_dim=90, name="input"))
    model.add(Dense(15, activation='softmax', name="output"))
    model.compile(loss='categorical_crossentropy', optimizer="adadelta", metrics=['accuracy'])
    initial_epochs = 0

model.fit(x_train, y_train,
          batch_size=100000,
          epochs=TOTAL_EPOCHS,
          verbose=0,
          validation_data=(x_test, y_test),
          class_weight = class_weight,
          callbacks=[PLogger(step=1), AutoSaver(model_filename=FILENAME, epochs_filename=EPOCHS_FILENAME, initial_epochs=initial_epochs, every=10)])

score = model.evaluate(x_test, y_test, verbose=0)

final_epochs = initial_epochs + TOTAL_EPOCHS

print("Network accuracy: %.2f%%, loss: %.4f, epochs: %5d " % (score[1] * 100, score[0], final_epochs ))

model.save(FILENAME)
pickle.dump(final_epochs, open( EPOCHS_FILENAME, "wb" ))


