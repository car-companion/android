# Copyright (C) 2024 The Qt Company Ltd.
# SPDX-License-Identifier: LicenseRef-Qt-Commercial OR GPL-3.0-only WITH Qt-GPL-exception-1.0

cmake_minimum_required(VERSION 3.21)

project(my_car_companionApp VERSION 0.1 LANGUAGES CXX)


set(CMAKE_AUTOMOC ON)
set(CMAKE_INCLUDE_CURRENT_DIR ON)
set(QT_QML_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/qml)
set(QML_IMPORT_PATH
    ${QT_QML_OUTPUT_DIRECTORY}
    CACHE STRING "code model"
    FORCE
)
# set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(
    Qt6 6.8
    REQUIRED COMPONENTS
        Quick
        Core
        Gui
        Qml
)

qt_standard_project_setup(REQUIRES 6.8)

qt_add_executable(
    ${CMAKE_PROJECT_NAME}
        main.cpp
)

qt_add_qml_module(
    ${CMAKE_PROJECT_NAME}
    URI "QmlModule"
    VERSION 1.0
    QML_FILES
        Main.qml
)

target_link_libraries(
    ${CMAKE_PROJECT_NAME}
    PRIVATE
        Qt::Core
        Qt::Quick
        Qt::Qml
        Qt::Gui
)

install(
    TARGETS ${CMAKE_PROJECT_NAME}
    BUNDLE DESTINATION .
    LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
    RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR}
)
