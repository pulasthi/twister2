import cloudpickle as cp

from twister2.tset.fn.PartitionFunc import PartitionFunc


class PartitionFunctions:

    def __init__(self, java_ref):
        self.__java_ref = java_ref

    @property
    def load_balanced(self):
        return JavaWrapper(self.__java_ref.loadBalanced())

    @staticmethod
    def build(self, partition_func: PartitionFunc):
        # send python dump to java -> create a java object in JVM -> get the ref back
        return self.java_ref.build(cp.dumps(partition_func))


class JavaWrapper(PartitionFunc):

    def prepare(self, sources: set, destinations: set):
        self.__java_ref.prepare(sources, destinations)

    def partition(self, source_index: int, val) -> int:
        return self.__java_ref.partition(source_index, val)

    def commit(self, source_index: int, partition: int) -> int:
        return self.__java_ref.commit(source_index, partition)

    def pre_defined(self) -> bool:
        return True

    def java_ref(self):
        return self.__java_ref

    def __init__(self, java_ref):
        super().__init__()
        self.__java_ref = java_ref
