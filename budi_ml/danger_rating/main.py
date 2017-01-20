import numpy as np
import pandas as pd
import pickle


'''
ALGORITHM:
CHOOSE THE CLOSEST CENTROIDS (from data/centroids.txt), and then use predict using Linear Regression
model for that particular centroid to get the danger rating (stored in model_for_danger folder).
There are 40 centroids.
'''

# Formats print function to be 6 digits decimal
def float_formatter(X):
    return "%.6f" % X


def haversine(lat1, lon1, lat2, lon2):
    """
    Calculates the great circle distance between two points
    on the earth (specified in decimal degrees), numpy vectorized version
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

# Ali, just see this section on how to use the models
if __name__ == '__main__':
    centroids = np.loadtxt(fname='data/centroids.txt', delimiter=',')
    complete_df = pd.read_csv('data/complete_df.csv')
    np.set_printoptions(formatter={'float_kind': float_formatter})

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
