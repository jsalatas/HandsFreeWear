from dataset import importCSV2
import pandas as pd
import numpy as np

from keras import backend as K
from keras.models import load_model
from keras.models import Sequential,Model
import tensorflow as tf

from tensorflow.python.tools import freeze_graph
from tensorflow.python.tools import optimize_for_inference_lib


FILENAME='model/model.h5'
export_path="export/model"
output_frozen_graph_name = '../HandsFreeWear/wear/src/main/assets/frozen_model.pb'

# open up a Tensorflow session
sess = tf.Session()
# tell Keras to use the session
K.set_session(sess)

# From this document: https://blog.keras.io/keras-as-a-simplified-interface-to-tensorflow-tutorial.html

# let's convert the model for inference
K.set_learning_phase(0)  # all new operations will be in test mode from now on
# serialize the model and get its weights, for quick re-building
previous_model = load_model(FILENAME)
previous_model.summary()

config = previous_model.get_config()
weights = previous_model.get_weights()

# re-build a model where the learning phase is now hard-coded to 0
try:
    model= Sequential.from_config(config)
except:
    model= Model.from_config(config)
    #model= model_from_config(config)
model.set_weights(weights)

model.summary()

print("Input name:")
print(model.input.name)
print("Output name:")
print(model.output.name)
output_name=model.output.name.split(':')[0]

#  not sure what this is for
export_version = 1 # version number (integer)

graph_file=export_path+".pb"
ckpt_file=export_path+".ckpt"
# create a saver
saver = tf.train.Saver(sharded=True)
tf.train.write_graph(sess.graph_def, '', graph_file)
save_path = saver.save(sess, ckpt_file)


input_graph_path = 'export/model.pb'
checkpoint_path = 'export/model.ckpt'

freeze_graph.freeze_graph(input_graph_path, "",  False, checkpoint_path, "output_1/Softmax", "save/restore_all", "save/Const", output_frozen_graph_name, True, "")
