#
# Generated file - Do not overwrite
#

cmake_minimum_required(VERSION 3.10)

set(NABLA_CXX_COMPILER /usr/bin/g++)

set(CMAKE_CXX_COMPILER ${NABLA_CXX_COMPILER} CACHE STRING "")

if(CMAKE_CXX_COMPILER_ID STREQUAL "GNU")
  if(CMAKE_CXX_COMPILER_VERSION VERSION_LESS "7.4.0")
    message(FATAL_ERROR "GCC minimum required version is 7.4.0. Please upgrade.")
  endif()
elseif(CMAKE_CXX_COMPILER_ID STREQUAL "Clang")
  if(CMAKE_CXX_COMPILER_VERSION VERSION_LESS "9.0.0")
    message(FATAL_ERROR "Clang minimum required version is 9.0.0. Please upgrade.")
  endif()
endif()

project(ExplicitHeatEquationProject CXX)

set(LIBCPPNABLA_BACKEND "NONE")
find_package(OpenMP)
add_subdirectory(${CMAKE_SOURCE_DIR}/../libcppnabla ${CMAKE_SOURCE_DIR}/../libcppnabla)


add_executable(explicitheatequation ExplicitHeatEquation.cc)
target_include_directories(explicitheatequation PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/..)
target_link_libraries(explicitheatequation PUBLIC cppnabla OpenMP::OpenMP_CXX)

if (EXISTS ${CMAKE_CURRENT_SOURCE_DIR}/Project.cmake)
  include(${CMAKE_CURRENT_SOURCE_DIR}/Project.cmake)
endif()
