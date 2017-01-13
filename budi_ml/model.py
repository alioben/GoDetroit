from sklearn.cluster import KMeans
from haversine import haversine
import pickle
import numpy as np


def float_formatter(X):
    return "%.7f" % X

'''
K-means clustering algorithm
Num of centroids: 40
'''
dt_trans = np.load('long_lat_array.npy')
K = 40

'''
Convert dt_trans to cartesian coordinate before training (format: latitude, longitude)

The formula is

x = R * cos(lat) * cos(lon)

y = R * cos(lat) * sin(lon)

z = R *sin(lat)

where R is 6371 KM
'''


def convert_long_lat_to_cartesian(latitude, longitude):
    R = 6371
    x = R * np.cos(latitude / 180 * np.pi) * np.cos(longitude / 180 * np.pi)
    y = R * np.cos(latitude / 180 * np.pi) * np.sin(longitude / 180 * np.pi)
    z = R * np.sin(latitude / 180 * np.pi)
    return np.array((x, y, z), dtype=float).T


def convert_cartesian_to_long_lat(x, y, z):
    R = 6371
    lat = np.arcsin(z / R)
    lon = np.arcsin(y / (R * np.cos(lat)))
    return np.array((lat / np.pi * 180, lon / np.pi * 180), dtype=float).T

cartesian = convert_long_lat_to_cartesian(dt_trans[:, 0], dt_trans[:, 1])

clf = None
try:
    with open('clf_data.pickle', 'rb') as f:
        clf = pickle.load(f)[0]
except:
    pass

if not clf:
    clf = KMeans(n_clusters=K).fit(cartesian)
    with open('clf_data.pickle', 'wb') as f:
        pickle.dump([clf], f)

centroids = np.array(clf.cluster_centers_, dtype=np.float64)
# convert back to lat - long format for the centroids
centroids = convert_cartesian_to_long_lat(
    centroids[:, 0], centroids[:, 1], centroids[:, 2])
np.set_printoptions(formatter={'float_kind': float_formatter})
np.savetxt('centroids.txt', centroids, delimiter=',', fmt='%5.6f')
