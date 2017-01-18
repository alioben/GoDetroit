import numpy as np
import pandas as pd
import pickle


def float_formatter(X):
    return "%.6f" % X


def haversine(lat1, lon1, lat2, lon2):
    """
    Calculate the great circle distance between two points
    on the earth (specified in decimal degrees)
    """
    # convert decimal degrees to radians
    lon1, lat1, lon2, lat2 = map(np.radians, [lon1, lat1, lon2, lat2])
    # haversine formula
    dlon = lon2 - lon1
    dlat = lat2 - lat1
    a = (np.sin(dlat / 2)**2 + np.cos(lat1) * np.cos(lat2) * np.sin(dlon / 2)**2)
    c = 2 * np.arcsin(np.sqrt(a))
    km = 6371 * c
    return km


centroids = np.loadtxt(fname='data/centroids.txt', delimiter=',')
complete_df = pd.read_csv('data/complete_df.csv')
np.set_printoptions(formatter={'float_kind': float_formatter})
threshold = 1

while True:
    print 'Please input latitude, longitude seperated by space:'
    try:
        long_lat = raw_input().split()
        long_lat = [float(elem) for elem in long_lat]
    except:
        print 'Wrong format, please try again!\n'
        continue

    distance_array = haversine(centroids[:, 0], centroids[:, 1], long_lat[0], long_lat[1])
    distance_to_nearest_centroid = distance_array.min()
    closest_centroid = centroids[distance_array.argmin()]
    closest_centroid_index = distance_array.argmin()

    print 'the closest centroid is: ', closest_centroid
    print 'the distance to the nearest centroid is: ', distance_to_nearest_centroid, 'kilo meters'
    print 'closest centroid index is: ', closest_centroid_index

    with open('model_for_danger/danger_clf_' + str(closest_centroid_index) + '.pickle', 'rb') as f:
        danger_clf = pickle.load(f)[0]
    danger_rating = 0 if danger_clf.predict(distance_to_nearest_centroid) < 0 else danger_clf.predict(distance_to_nearest_centroid).item()
    print 'The danger scale for this location is: ', danger_rating
