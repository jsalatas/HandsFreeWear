from keras.models import load_model
from dataset import importCSV2, getDataset
import pandas as pd
import numpy as np

FILENAME='model/model.h5'

#Load previous model
model = load_model(FILENAME)

# # all
# x_data, labels =  importCSVTest()
# actual=np.ndarray.argmax(labels, axis=1)
# p=model.predict(x_data)
# predictions = np.ndarray.argmax(p, axis=1)
# probability=np.ndarray.max(p, axis=1)
# a=pd.concat([pd.Series(data=actual, name='Actual'), pd.Series(data=predictions, name='Predicted'), pd.Series(data=probability, name='Probability')], axis=1)
# a.to_csv(path_or_buf="predictions_all_test.csv", index=False)

# all
x_data, labels = importCSV2()
actual=np.ndarray.argmax(labels, axis=1)
p=model.predict(x_data)
predictions = np.ndarray.argmax(p, axis=1)
probability=np.ndarray.max(p, axis=1)
a=pd.concat([pd.Series(data=actual, name='Actual'), pd.Series(data=predictions, name='Predicted'), pd.Series(data=probability, name='Probability')], axis=1)
a.to_csv(path_or_buf="predictions_all.csv", index=False)


# test
_, x_test, _, y_test = getDataset()
actual=np.ndarray.argmax(y_test, axis=1)
p=model.predict(x_test)
predictions = np.ndarray.argmax(p, axis=1)
probability=np.ndarray.max(p, axis=1)
a=pd.concat([pd.Series(data=actual, name='Actual'), pd.Series(data=predictions, name='Predicted'), pd.Series(data=probability, name='Probability')], axis=1)
a.to_csv(path_or_buf="predictions_test.csv", index=False)
