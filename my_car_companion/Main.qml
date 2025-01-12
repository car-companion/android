// Copyright (C) 2024 The Qt Company Ltd.
// SPDX-License-Identifier: LicenseRef-Qt-Commercial OR GPL-3.0-only WITH Qt-GPL-exception-1.0

import QtQuick
import QtQuick.Controls
import QtQuick3D
import QtQuick3D.Effects
import QtQuick3D.Helpers
import QtQuick3D.Particles3D 6.7
import Qt_Car_Baked_low_v2

Rectangle {
    id: root
    property bool rightDoorOpen: false
    property bool rightWindowUp: true
    property bool leftWindowUp: true
    property bool leftDoorOpen: false
    property bool lightsOff: false
    property bool runningRotation: true
    property bool areTiresTurning: false
    property bool isItSnowing: false
    property bool mouseAreaEnabled: true
    color: "#00FFFFFF"

    MouseArea {
        anchors.fill: parent
        enabled: root.mouseAreaEnabled

        OrbitCameraController {
            anchors.fill: parent
            origin: qt_Car_Baked_low_v2
            camera: sceneCamera
            xInvert: true
            ySpeed: 0
        }
    }

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
                areTiresTurning: root.areTiresTurning

                x: 1000
                y: 0
                z: 3000

                ParallelAnimation {
                    id: driveInAnimation // Give the animation an ID
                    NumberAnimation {
                        target: qt_Car_Baked_low_v2
                        property: "x"
                        from: 1000
                        to: 0
                        duration: 5000
                        easing.type: Easing.InOutQuad
                    }
                    NumberAnimation {
                        target: qt_Car_Baked_low_v2
                        property: "z"
                        from: 3000
                        to: 0
                        duration: 3000
                        easing.type: Easing.InOutQuad
                    }
                    NumberAnimation {
                        target: qt_Car_Baked_low_v2
                        property: "eulerRotation.y"
                        from: -90
                        to: 20
                        duration: 5000
                        easing.type: Easing.InOutQuad
                    }
                }

                Component.onCompleted: {
                    driveInAnimation.start(); // Start the animation explicitly
                }
            }

            PerspectiveCamera {
                id: sceneCamera
                x: -2500
                y: 600
                fieldOfViewOrientation: PerspectiveCamera.Vertical
                fieldOfView: 50
                clipNear: 10
                scale.x: 1
                scale.y: 1
                eulerRotation.z: 0
                eulerRotation.y: -90
                eulerRotation.x: -10
                z: 0
            }

            /*Model {
                id: plane
                x: 0
                y: 0
                z: 0
                source: "#Rectangle"
                scale.z: 1
                scale.y: 60
                scale.x: 40
                eulerRotation.x: -90
                materials: defaultMaterial
            }*/
        }

        ParticleSystem3D {
            id: snow
            x: 0
            y: 500
            visible: root.isItSnowing
            ParticleEmitter3D {
                id: snowEmitter
                velocity: snowDirection
                shape: snowShape
                particleScaleVariation: 1
                particleScale: 2
                particle: snowParticle
                lifeSpan: 4000
                emitRate: 500
                VectorDirection3D {
                    id: snowDirection
                    direction.z: 0
                    direction.y: -100
                }

                SpriteParticle3D {
                    id: snowParticle
                    color: "#dcdcdc"
                    sprite: snowTexture
                    particleScale: 5
                    maxAmount: 100001
                    Texture {
                        id: snowTexture
                        source: "snowflake.png"
                    }
                    billboard: true
                }
            }

            ParticleShape3D {
                id: snowShape
                type: ParticleShape3D.Cube
                fill: true
                extents.z: 1000
                extents.y: 1000
                extents.x: 1000
            }

            Wander3D {
                id: wander
                uniquePaceVariation: 0.2
                uniquePace.z: 0.03
                uniquePace.y: 0.01
                uniquePace.x: 0.03
                uniqueAmountVariation: 0.1
                uniqueAmount.z: 50
                uniqueAmount.y: 20
                uniqueAmount.x: 50
                particles: snowParticle
                globalPace.x: 0.01
                globalAmount.x: -500
            }
        }
    }

    Item {
        id: __materialLibrary__
        PrincipledMaterial {
            id: defaultMaterial
            objectName: "Default Material"
            baseColor: "#9b9b9b"
        }
    }
}

