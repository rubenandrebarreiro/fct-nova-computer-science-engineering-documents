# -*- coding: utf-8 -*-

import numpy as np
import matplotlib.pyplot as plt

from sklearn import cluster, datasets, mixture
from sklearn.neighbors import kneighbors_graph
from sklearn.preprocessing import StandardScaler

np.random.seed(0)


# Generate datasets.
#Based on sklearn tutorial:
#http://scikit-learn.org/stable/auto_examples/cluster/plot_cluster_comparison.html
n_samples = 1500
CIRCLE = datasets.make_circles(n_samples=n_samples, factor=.5,noise=.05)[0]
MOON = datasets.make_moons(n_samples=n_samples, noise=.05)[0]
BLOB = datasets.make_blobs(n_samples=n_samples, random_state=8)[0]
UNSTRUCTURED = np.random.rand(n_samples, 2)
datasets = [CIRCLE, MOON, BLOB, UNSTRUCTURED]
#standardize data:
for ix, ds in enumerate(datasets):
    datasets[ix] = StandardScaler().fit_transform(ds)

NOISY_BLOB = np.copy(datasets[2])
ixs = np.random.randint(0,n_samples,100)
NOISY_BLOB[ixs,:] = datasets[3][ixs,:]
datasets.append(NOISY_BLOB)

def plot_clusters(X,y_pred,title=''):
    """Plotting function; y_pred is an integer array with labels"""    
    plt.figure()
    colors = np.array([x for x in 'bgrcmykbgrcmykbgrcmykbgrcmyk'])
    plt.scatter(X[:, 0], X[:, 1], color=colors[y_pred].tolist(), s=10)
    plt.xlim(-2, 2)
    plt.ylim(-2, 2)
    plt.xticks(())
    plt.yticks(())
    plt.title(title)
    plt.axis('equal')
    plt.show()

