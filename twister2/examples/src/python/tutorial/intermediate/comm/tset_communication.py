from twister2 import TSetContext
from twister2.Twister2Environment import Twister2Environment
from twister2.tset.fn.SourceFunc import SourceFunc

env = Twister2Environment(resources=[{"cpu": 1, "ram": 512, "instances": 4}])

print("Hello from worker %d" % env.worker_id)


class IntSource(SourceFunc):

    def __init__(self):
        super().__init__()
        self.i = 0

    def has_next(self):
        return self.i < 10

    def next(self):
        res = self.i
        self.i = self.i + 1
        return res


source_x = env.create_source(IntSource(), 4)


def mul_by_five(itr, collector, ctx: TSetContext):
    for i in itr:
        collector.collect(i * 5)


def add_two(itr, collector, ctx: TSetContext):
    for i in itr:
        collector.collect(i + 2)


source_x.compute(mul_by_five).compute(add_two).reduce(lambda i1, i2: i1 + i2) \
    .for_each(lambda i: print("SUM = %d" % i))
