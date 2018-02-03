import pickle
from dataset import importCSV

data = importCSV()

pickle.dump(data, open( "data/sensors.pkl", "wb" ))

data = pickle._load(open( "data/sensors.pkl", "rb" ))
x_train, x_test, y_train, y_test = data
print(x_train.head())
