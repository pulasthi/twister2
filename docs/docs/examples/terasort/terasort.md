---
id: terasort
title: Tera Sort
sidebar_label: Tera Sort
---

Tera Sort is a common benchmark to measure and compare high performance big data frameworks such 
as Twister2. The idea is to measure the time to sort one terabyte of randomly distributed data.

This terasort is implemented according to the requirements listed in the 
[https://sortbenchmark.org/](https://sortbenchmark.org/) website. It uses partition and sort method. 
First we globally partition the data into available tasks. Then the data in each task is sorted. This 
gives a global sorting among the tasks. 

Because terasort can run with data larger than the memory, we can use file system to store and sort 
the data. All this handle internally to twister2 and you can configure how much data to keep in memory etc.

This implementation can read from a file generated by the input generator in 
[gensort Data Generator](http://www.ordinal.com/gensort.html) or it can use an in-memory generator.

When it runs with in-memory mode, we generate a batch of tuples, each having a random byte array as 
the key and a byte array of configurable length as the value. These tuples will be sent from multiple 
sources to a single sinks with keyed gather operation applied as the connection. Keyed Gather 
operation is configured with a comparator to sort by key. 

### Generating data files

[gensort Data Generator](http://www.ordinal.com/gensort.html) can be used to generate data files.

### In memory example command

```bash
./bin/twister2 submit standalone jar examples/libexamples-java.jar edu.iu.dsc.tws.examples.batch.terasort.TeraSort -size .5 -valueSize 90 -keySize 10 -instances 8 -instanceCPUs 1 -instanceMemory 1024 -sources 8 -sinks 8 -memoryBytesLimit 4000000000 -fileSizeBytes 100000000
```

### File mode example command

```bash
./bin/twister2 submit standalone jar examples/libexamples-java.jar edu.iu.dsc.tws.examples.batch.terasort.TeraSort -size .5 -valueSize 90 -keySize 10 -instances 8 -instanceCPUs 1 -instanceMemory 1024 -sources 8 -sinks 8 -memoryBytesLimit 4000000000 -fileSizeBytes 100000000 -inputFile /path/to/file-%d
```

### Tera Sort parameters

#### Data Configuration - File Based mode

| Parameter  | Description | Default Value |
| ------------- | ------------- | ------------- |
| inputFile  | Path to the input file. This path can contain, %d which will be replaced with the task index at runtime. For example, if file is specified as input-%d, when executing task with index 0, it will search for file input-0.  | Mandatory |
| valueSize | Size of the value component of the tuple in bytes | Mandatory |
| keySize | Size of the value component of the tuple in bytes | Mandatory |


#### Data Configuration - Non File Based mode
Terasort will switch to non file based mode, if filePath is not specified.

| Parameter  | Description | Default Value |
| ------------- | ------------- | ------------- |
| size  | Total size of data generated by sources.  | Mandatory |
| valueSize | Size of the value component of the tuple in bytes | Mandatory |
| keySize | Size of the value component of the tuple in bytes | Mandatory |

#### Resource Configuration

| Parameter  | Description | Default Value |
| ------------- | ------------- | ------------- |
| instances | No of twister2 worker instances | Mandatory |
| instanceCpus | No of CPUs to allocate per each instance(Might not be applicable for all twister2 modes) | Mandatory |
| instanceMemory | Amount of memory to allocate in MB for each instance | Mandatory |
| sources | No. of sources to generate data. This will multiply the data size of your current configuration. For example, if you have specified, 512GB as the data size and 4 as the No. of sources, twister2 will produce and sort 1TB of data in total | Mandatory |
| sinks | No. of sinks to receive the globally sorted data | Mandatory |

#### Tuneup Parameters

| Parameter  | Description | Default Value |
| ------------- | ------------- | ------------- |
| memoryBytesLimit | Maximum amount of random access memory(in bytes) to utilize to hold incoming tuples. Tuples will be written to the disk, once this limit is exceeded. | 6400 |
| fileSizeBytes | Size of a single file to use when going to disk | 64 |

