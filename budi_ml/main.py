import pickle
import sys
import numpy as np
from sklearn.cluster import KMeans
from haversine import haversine

def float_formatter(X):
    return "%.6f" % X

centroids = np.loadtxt(fname='centroids_5.txt', delimiter=',')
np.set_printoptions(formatter={'float_kind': float_formatter})
distance_matrix = pdist(centroids, metric=haversine)
# print 'distance_matrix is: ', distance_matrix.shape
threshold = 1

while True:
    print 'Please input latitude, longitude seperated by space:'
    try:
        long_lat = raw_input().split()
        long_lat = [float(elem) for elem in long_lat]
    except:
        print 'Wrong format, please try again!\n'
        continue
    print 'long_lat is', long_lat

    min_dist = sys.maxint
    for i in centroids:
        if haversine(i, long_lat) < min_dist:
            closest_centroid = i
            min_dist = haversine(i, long_lat)
    print 'the closest centroid is: ', closest_centroid
    distance_to_nearest_centroid = haversine(closest_centroid, long_lat)
    print 'the distance to the nearest centroid is: ', distance_to_nearest_centroid, 'kilo meters'
    danger_factor = 1 if distance_to_nearest_centroid > threshold else int(10 - int((distance_to_nearest_centroid / threshold) * 10))
    print 'this danger factor of this location is: ', danger_factor, '\n'
