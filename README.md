# Skewness-aware Vertex-cut Partitioner (S5P)
The implementation of S5P.
## Installation
We tested the program (main) on Ubuntu 20.046 LTS.

The programs require the below packages: `g++`, `cmake`, `glog`, `gflags`, `boost`:
```
sudo apt-get install libgoogle-glog-dev libgflags-dev libboost-all-dev cmake g++
```

## Build Programs
```
#C++
cd cpp
mkdir build && cd build
cmake ..
make
```

```
#java
cd Java
#Details in Java Folder
```

## Usage
Parameters:
* `filename(inputGraphPath)`: path to the edge list file
* `Vcount`: $|V|$
* `Ecount`: $|E|$
* `batchsize`: default: 1000
* `k`: the number of partitions
* `$tao$($\beta$)`: Skewness coefficient

### Data sets used in the paper
* OK: https://snap.stanford.edu/data/com-Orkut.html
* TW: https://snap.stanford.edu/data/twitter-2010.html
* FR: https://snap.stanford.edu/data/com-Friendster.html
* LJ: https://snap.stanford.edu/data/com-LiveJournal.html
* IT: http://law.di.unimi.it/webdata/it-2004/
* UK7: http://law.di.unimi.it/webdata/uk-2007-05/
* IN: https://law.di.unimi.it/webdata/in-2004/
* SK: https://law.di.unimi.it/webdata/sk-2005/
* UK2: https://law.di.unimi.it/webdata/uk-2002/
* AR: https://law.di.unimi.it/webdata/arabic-2005/
* WB: https://law.di.unimi.it/webdata/webbase-2001/
* Synthetic Graphs (R-MAT/TrillionG): https://github.com/chan150/TrillionG (SIGMOD'17)
