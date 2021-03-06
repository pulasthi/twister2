import numpy as np

from twister2.TSetContext import TSetContext
from twister2.Twister2Environment import Twister2Environment
from twister2.tset.fn.SourceFunc import SourceFunc

env = Twister2Environment(resources=[{"cpu": 4, "ram": 512, "instances": 1}])


class PointSource(SourceFunc):

    def __init__(self, size, count):
        super().__init__()
        self.i = 0
        self.read = True
        self.size = size
        self.count = count

    def has_next(self):
        return self.read

    def next(self):
        self.i = self.i + 1
        arr = np.random.rand(self.count, self.size)
        self.read = False
        return arr


data = env.create_source(PointSource(10, 1000), 2).cache()
centers = env.create_source(PointSource(10, 20), 2).cache()


def apply_kmeans(points, ctx: TSetContext):
    from sklearn.cluster import KMeans
    data_partition = ctx.get_input("centroids")
    centers = data_partition.first()
    kmeans = KMeans(init=centers, n_clusters=20, n_init=1).fit(points)
    return kmeans.cluster_centers_


mapped = data.map(apply_kmeans)


def reduce_centroids(c1, c2):
    return c1 + c2


def average(points, ctx):
    import numpy as np
    return np.divide(points, 10)


reduced = mapped.all_reduce(reduce_centroids).map(average)

for i in range(10):
    mapped.add_input("centroids", centers)
    centers = reduced.cache()

centers.for_each(lambda x: print(x))
