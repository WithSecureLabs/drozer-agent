cmake_minimum_required(VERSION 3.4.1)

add_library( mstring
             SHARED
             src/main/cpp/libmstring/mstring.cpp)

target_link_libraries(mstring
                      android
                      log)