// Copyright (C) 2024 The Qt Company Ltd.
// SPDX-License-Identifier: LicenseRef-Qt-Commercial OR GPL-3.0-only WITH Qt-GPL-exception-1.0

import QtQuick
import QtQuick.Controls
import QtQuick3D
import QtQuick3D.Effects
import Qt_Car_Baked_low_v2

Rectangle {
    id: root
    property bool lightsON: false
    property double sliderValue: 0
    color: "transparent"


    View3D {
        id: view3D
        anchors.fill: parent
        anchors.leftMargin: 0
        anchors.rightMargin: 0

        environment: sceneEnvironment

        SceneEnvironment {
            id: sceneEnvironment
            antialiasingMode: SceneEnvironment.MSAA
            antialiasingQuality: SceneEnvironment.High
        }

        Node {
            id: scene
            DirectionalLight {
                id: directionalLight
                eulerRotation.z: -0.00002
                eulerRotation.y: -90
                eulerRotation.x: -50
            }

            Qt_Car_Baked_low_v2 {
                id: qt_Car_Baked_low_v2
                visible: true
                eulerRotation.y: root.sliderValue
                scale.z: 400
                scale.y: 400
                scale.x: 400
                eulerRotation.z: 0.00002
                eulerRotation.x: -0.00003
            }

            PerspectiveCamera {
                id: sceneCamera
                x: -1500
                y: 500
                eulerRotation.z: 0
                eulerRotation.y: -111.56232
                eulerRotation.x: -20.93197
                z: -500
            }
        }
    }

    Item {
        id: __materialLibrary__
        PrincipledMaterial {
            id: defaultMaterial
            objectName: "Default Material"
            baseColor: "#4aee45"
        }
    }
}

