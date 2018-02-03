from keras.callbacks import Callback
import pickle

class AutoSaver(Callback):
    def __init__(self, model_filename, epochs_filename, every=50, initial_epochs=0):
        super(AutoSaver, self).__init__()
        self.model_filename=model_filename
        self.epochs_filename=epochs_filename
        self.every=every
        self.initial_epochs=initial_epochs

    def on_epoch_end(self, epoch, logs=None):
        if (epoch + 1) % self.every == 0:
            self.model.save(self.model_filename)
            pickle.dump(self.initial_epochs+ epoch+1, open( self.epochs_filename, "wb" ))

