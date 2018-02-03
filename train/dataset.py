import pandas as pd
from keras.utils import np_utils
import tensorflow as tf
from sklearn.model_selection import train_test_split
import pickle

def importCSV2():
    data = pd.read_csv('data/data.csv')
    feature_columns_names = list(data.columns.values)
    feature_columns_names.remove('classification')
    feature_columns = {}
    for name in feature_columns_names:
        feature_columns[name] = tf.feature_column.numeric_column(name)

    x_data = data.drop('classification', axis=1)
    labels=np_utils.to_categorical(data['classification'])

    return [x_data, labels]

def importCSV():
    data = pd.read_csv('data/data.csv')
    feature_columns_names = list(data.columns.values)
    feature_columns_names.remove('classification')
    feature_columns = {}
    for name in feature_columns_names:
        feature_columns[name] = tf.feature_column.numeric_column(name)

    x_data = data.drop('classification', axis=1)
    labels=np_utils.to_categorical(data['classification'])

    X_train, X_test, y_train, y_test = train_test_split(x_data,labels,test_size=0.33, random_state=101)

    return [X_train, X_test, y_train, y_test]



def getDataset():
    data = pickle._load(open( "data/sensors.pkl", "rb" ))
    return data


