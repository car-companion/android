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

qt_add_qml_module(
    Qt_Car_Baked_low_v2
    URI "Qt_Car_Baked_low_v2"
    VERSION 1.0
    QML_FILES
        Qt_Car_Baked_low_v2/Qt_Car_Baked_low_v2.qml
    RESOURCES
        "Qt_Car_Baked_low_v2/_importdata.json"
        "Qt_Car_Baked_low_v2/maps/textureData.jpg"
        "Qt_Car_Baked_low_v2/maps/textureData72.jpg"
        "Qt_Car_Baked_low_v2/maps/textureData81.jpg"
        "Qt_Car_Baked_low_v2/maps/textureData8.png"
        "Qt_Car_Baked_low_v2/maps/textureData45.jpg"
        "Qt_Car_Baked_low_v2/maps/textureData63.jpg"
        "Qt_Car_Baked_low_v2/maps/textureData58.jpg"
        "Qt_Car_Baked_low_v2/maps/textureData67.jpg"
        "Qt_Car_Baked_low_v2/maps/textureData33.jpg"
        "Qt_Car_Baked_low_v2/maps/textureData76.jpg"
        "Qt_Car_Baked_low_v2/maps/textureData43.jpg"
        "Qt_Car_Baked_low_v2/maps/textureData35.png"
        "Qt_Car_Baked_low_v2/maps/textureData13.jpg"
        "Qt_Car_Baked_low_v2/maps/textureData83.jpg"
        "Qt_Car_Baked_low_v2/maps/textureData6.jpg"
        "Qt_Car_Baked_low_v2/meshes/door_b_R_Low_mesh.mesh"
        "Qt_Car_Baked_low_v2/meshes/doorWindow_b_L_low_mesh.mesh"
        "Qt_Car_Baked_low_v2/meshes/headLights_low_mesh.mesh"
        "Qt_Car_Baked_low_v2/meshes/seat_front_low_mesh.mesh"
        "Qt_Car_Baked_low_v2/meshes/tire_FrontL_low_mesh.mesh"
        "Qt_Car_Baked_low_v2/meshes/insideFloor_low_mesh.mesh"
        "Qt_Car_Baked_low_v2/meshes/seat_rear_low_mesh.mesh"
        "Qt_Car_Baked_low_v2/meshes/backR_Tire_b_Low_mesh.mesh"
        "Qt_Car_Baked_low_v2/meshes/mainbody_b_Low_mesh.mesh"
        "Qt_Car_Baked_low_v2/meshes/roof_b_001_mesh.mesh"
        "Qt_Car_Baked_low_v2/meshes/backL_Tire_b_low_mesh.mesh"
        "Qt_Car_Baked_low_v2/meshes/dash_low_mesh.mesh"
        "Qt_Car_Baked_low_v2/meshes/doorWindow_b_R_low_mesh.mesh"
        "Qt_Car_Baked_low_v2/meshes/frontR_Tire_b_Low_mesh.mesh"
        "Qt_Car_Baked_low_v2/meshes/carBottom_low_mesh.mesh"
        "Qt_Car_Baked_low_v2/meshes/door_b_L_Low_mesh.mesh"
        "Qt_Car_Baked_low_v2/meshes/screens_low_mesh.mesh"
        "Qt_Car_Baked_low_v2/meshes/wheel_screen_low_mesh.mesh"
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
