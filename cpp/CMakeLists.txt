cmake_minimum_required(VERSION 3.10)

project(S5P)
set(CMAKE_CXX_STANDARD 11)
include_directories(${CMAKE_CURRENT_SOURCE_DIR})
find_package(Threads REQUIRED)
FIND_PACKAGE( OpenMP REQUIRED)
if(OPENMP_FOUND)
message("OPENMP FOUND")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${OpenMP_C_FLAGS}")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${OpenMP_CXX_FLAGS}")
set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} ${OpenMP_EXE_LINKER_FLAGS}")
endif()

set(EXECUTABLE_OUTPUT_PATH "./bin/")


add_executable(main main.cpp cm_sketch.cpp         
                globalConfig.cpp StreamCluster.cpp
                ClusterGameTask.cpp Partitioner.cpp readGraph.cpp )
target_link_libraries(main PRIVATE Threads::Threads)
