// Copyright (C) 2024 The Qt Company Ltd.
// SPDX-License-Identifier: LicenseRef-Qt-Commercial OR GPL-3.0-only WITH Qt-GPL-exception-1.0

import QtQuick
import QtQuick.Controls
import QtQuick3D
import QtQuick3D.Effects
import Qt_Car_Baked_low_v2

Rectangle {
    id: root
    property double sliderValue: 0
    property bool rightDoorOpen: false
    property bool rightWindowUp: true
    property bool leftWindowUp: true
    property bool leftDoorOpen: false
    property bool lightsOff: false
    width: Constants.width
    height: 1080

    color: "#00FFFFFF"

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
                rightWindowUp: root.rightWindowUp
                rightDoorOpen: root.rightDoorOpen
                leftWindowUp: root.leftWindowUp
                leftDoorOpen: root.leftDoorOpen
                lightsOff: root.lightsOff
                eulerRotation.y: root.sliderValue
                scale: Qt.vector3d(400, 400, 400)

                SequentialAnimation {
                    running: true
                    loops: Animation.Infinite
                    NumberAnimation {
                        target: qt_Car_Baked_low_v2
                        property: "eulerRotation.y"
                        from: 0
                        to: 360
                        duration: 120000
                    }
                }
            }

            PerspectiveCamera {
                id: sceneCamera
                x: -1500
                y: 500
                eulerRotation.z: -0.49531
                eulerRotation.y: -110.44622
                eulerRotation.x: -19.31819
                z: -500
            }
        }

        Text {
            id: _text
            text: qsTr("Text")
            font.pixelSize: 12
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

