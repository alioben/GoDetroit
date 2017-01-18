from sklearn.cluster import KMeans
from sklearn.linear_model import LinearRegression
import pickle
import numpy as np
import pandas as pd


def float_formatter(X):
    return "%.7f" % X


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


'''
K-means clustering algorithm
Num of centroids: 40
'''
test_data = pd.read_csv('data/final_data.csv')

long_column = np.array(test_data['LONG'])
lat_column = np.array(test_data['LAT'])
long_lat_array = []

for i in range(len(long_column)):
    long_lat_array.append([float(lat_column[i]), float(long_column[i])])
print np.array(long_lat_array).shape
dt_trans = np.array(long_lat_array)
K = 40

cartesian = convert_long_lat_to_cartesian(dt_trans[:, 0], dt_trans[:, 1])

clf = None
try:
    with open('clf_data.pickle', 'rb') as f:
        print 'clf is already there, just load it...'
        clf = pickle.load(f)[0]
except:
    pass

if not clf:
    print 'Training right now with: ', cartesian.shape[0], 'rows...'
    clf = KMeans(n_clusters=K).fit(cartesian)
    with open('clf_data.pickle', 'wb') as f:
        pickle.dump([clf], f)

print 'Finished training, predicting...'
predictions = clf.predict(cartesian)
centroids = np.array(clf.cluster_centers_, dtype=np.float64)

complete_df = pd.DataFrame(dt_trans, columns=['Lat', 'Long'])
complete_df['X'] = cartesian[:, 0]
complete_df['Y'] = cartesian[:, 1]
complete_df['Z'] = cartesian[:, 2]
complete_df['Centroid'] = predictions
complete_df.to_csv('data/complete_df.csv')

# convert back to lat - long format for the centroids
centroids = convert_cartesian_to_long_lat(
    centroids[:, 0], centroids[:, 1], centroids[:, 2])
np.set_printoptions(formatter={'float_kind': float_formatter})
np.savetxt('data/centroids.txt', centroids, delimiter=',', fmt='%5.7f')

print 'Begin training datasets for determining danger factor..: '
for i in range(len(centroids)):
    # Register all other data points within this centroid, and find the datapoint which has the max distance to the coresponding centroid
    print 'Training centroid number: ', i + 1
    target_df = complete_df[complete_df['Centroid'] == i].reset_index()
    target_df['Dist to centroid'] = haversine(target_df['Lat'], target_df['Long'], centroids[i][0], centroids[i][1])
    target_df.set_index(['Lat', 'Long'], inplace=True)
    max_dist = target_df['Dist to centroid'].max()
    target_df['Danger scale'] = 10 - (target_df['Dist to centroid'] / max_dist * 10)

    danger_clf = None
    try:
        with open('model_for_danger/danger_clf_' + str(i) + '.pickle', 'rb') as f:
            print 'danger clf is already there, just load it...'
            danger_clf = pickle.load(f)[0]
    except:
        pass
    if danger_clf is None:
        # Train all datapoints of all centroids to determine the danger factor
        print 'test'
        classification_df = target_df[['Dist to centroid', 'Danger scale']]
        danger_clf = LinearRegression()
        num_row = target_df['Dist to centroid'].shape[0]
        danger_clf.fit(np.array(target_df['Dist to centroid']).reshape((num_row, 1)), np.array(target_df['Danger scale']).reshape((num_row, 1)))
        with open('model_for_danger/danger_clf_' + str(i) + '.pickle', 'wb') as f:
            pickle.dump([danger_clf], f)
