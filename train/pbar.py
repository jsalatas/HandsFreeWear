from keras.callbacks import ProgbarLogger
from keras.utils.generic_utils import Progbar

class PLogger(ProgbarLogger):
    def __init__(self, count_mode='samples', step=50):
        super(PLogger, self).__init__()
        self.logstep = step
        self.initialized=False
        self.epoch=0;

    def on_epoch_end(self, epoch, logs=None):
        if (epoch + 1) % self.logstep == 0:
            logs = logs or {}
            for k in self.params['metrics']:
                if k in logs:
                    self.log_values.append((k, logs[k]))
            self.progbar.update(epoch+1, self.log_values)

    def on_epoch_begin(self, epoch, logs=None):
        self.epoch = epoch
        if not self.initialized:
            self.initialized = True
            if self.use_steps:
                target = self.params['steps']
            else:
                target = self.params['samples']
            self.target = target
            self.progbar = Progbar(target=self.epochs, verbose=1)
            self.seen = 0

    def on_train_end(self, logs=None):
        if(self.epoch+1 < self.epochs):
            print("")
