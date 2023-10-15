# S5P Graph Partitioning Algorithm by JAVA
## 1. Introduction
This JAVA version of S5P implements the efficient gaming part,with input graph file directly partitioned and extensive code optimization performed.

## 2. Compile
Use `javac` to compile all the classes.

```sh
find ./S5P/src -name *.java | xargs javac
```

## 3. Edit Graph Infomation
When S5P starts running,it first gets graph information and running settings from the file `./S5P/src/s5p/properties/project.properties`.

Make sure you have edit this file with your graph information and settings correctly.

The input graph file has $| E |$ lines,where $| E |$ is the edge count of the graph.

The format of each line of file is two integers separated by a tab like this:

```
{source-NodeID}\t{destination-NodeID}
```

## 4. Run

```sh
cd ./S5P/src && java s5p.main.Main
```