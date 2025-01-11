import QtQuick
import QtQuick3D
import QtQuick.Timeline 1.0
import QtQuick.Controls 6.7

Node {
    id: node
    scale.x: 500
    scale.y: 500
    scale.z: 500

    property bool rightDoorOpen: false
    property bool leftWindowUp: true
    property bool rightWindowUp: true
    property bool leftDoorOpen: false
    property bool lightsOff: true
    property bool areTiresTurning: false

    // Resources
    property url textureData: "maps/textureData.jpg"
    property url textureData63: "maps/textureData63.jpg"
    property url textureData58: "maps/textureData58.jpg"
    property url textureData6: "maps/textureData6.jpg"
    property url textureData83: "maps/textureData83.jpg"
    property url textureData8: "maps/textureData8.png"
    property url textureData81: "maps/textureData81.jpg"
    property url textureData76: "maps/textureData76.jpg"
    property url textureData72: "maps/textureData72.jpg"
    property url textureData13: "maps/textureData13.jpg"
    property url textureData45: "maps/textureData45.jpg"
    property url textureData33: "maps/textureData33.jpg"
    property url textureData43: "maps/textureData43.jpg"
    property url textureData35: "maps/textureData35.png"
    property url textureData67: "maps/textureData67.jpg"
    Texture {
        id: _2_texture
        generateMipmaps: true
        mipFilter: Texture.Linear
        source: node.textureData13
    }
    Texture {
        id: _5_texture
        generateMipmaps: true
        mipFilter: Texture.Linear
        source: node.textureData35
    }
    Texture {
        id: _0_texture
        generateMipmaps: true
        mipFilter: Texture.Linear
        source: node.textureData6
    }
    Texture {
        id: _1_texture
        generateMipmaps: true
        mipFilter: Texture.Linear
        source: node.textureData8
    }
    Texture {
        id: _6_texture
        generateMipmaps: true
        mipFilter: Texture.Linear
        source: node.textureData43
    }
    Texture {
        id: _12_texture
        generateMipmaps: true
        mipFilter: Texture.Linear
        source: node.textureData76
    }
    Texture {
        id: _7_texture
        generateMipmaps: true
        mipFilter: Texture.Linear
        source: node.textureData45
    }
    Texture {
        id: _8_texture
        generateMipmaps: true
        mipFilter: Texture.Linear
        source: node.textureData58
    }
    Texture {
        id: _9_texture
        generateMipmaps: true
        mipFilter: Texture.Linear
        source: node.textureData63
    }
    Texture {
        id: _10_texture
        pivotV: 1
        positionV: 2
        scaleU: 3
        scaleV: 3
        generateMipmaps: true
        mipFilter: Texture.Linear
        source: node.textureData67
    }
    Texture {
        id: _11_texture
        generateMipmaps: true
        mipFilter: Texture.Linear
        source: node.textureData72
    }
    Texture {
        id: _3_texture
        generateMipmaps: true
        mipFilter: Texture.Linear
        source: node.textureData
    }
    Texture {
        id: _13_texture
        generateMipmaps: true
        mipFilter: Texture.Linear
        source: node.textureData81
    }
    Texture {
        id: _14_texture
        generateMipmaps: true
        mipFilter: Texture.Linear
        source: node.textureData83
    }
    Texture {
        id: _4_texture
        generateMipmaps: true
        mipFilter: Texture.Linear
        source: node.textureData33
    }

    // Nodes:
    Node {
        id: root
        visible: true

        objectName: "ROOT"
        Model {
            id: dash_low
            objectName: "Dash_low"
            source: "meshes/dash_low_mesh.mesh"
            materials: [
                dashBoard_material
            ]
        }
        Model {
            id: screens_low
            objectName: "Screens_low"
            x: 0.6419070959091187
            source: "meshes/screens_low_mesh.mesh"
            materials: [
                m_Screens_material
            ]
        }
        Model {
            id: carBottom_low
            objectName: "CarBottom_low"
            source: "meshes/carBottom_low_mesh.mesh"
            materials: [
                m_Black_material
            ]
        }
        Model {
            id: headLights_low
            visible: !node.lightsOff
            objectName: "HeadLights_low"
            source: "meshes/headLights_low_mesh.mesh"
            materials: [
                m_Headlights_material
            ]
            Behavior on visible {
                NumberAnimation {
                    duration: 500
                    easing.type: Easing.InOutQuad
                }
            }
        }
        Model {
            id: doorWindow_b_L_low
            objectName: "DoorWindow_b_L_low"
            x: node.leftWindowUp? -0.6165767908096313 : -0.6565767908096313
            y: node.leftWindowUp? 0.2809993028640747 : 0.1409993028640747
            z: 0.6550559401512146
            source: "meshes/doorWindow_b_L_low_mesh.mesh"
            eulerRotation.y: node.leftDoorOpen? -74 : 0
            materials: [
                m_windowGlass_material
            ]
            Behavior on eulerRotation.y {
                NumberAnimation {
                    duration: 500
                    easing.type: Easing.InOutQuad
                }
            }
            Behavior on x {
                NumberAnimation {
                    duration: 500
                    easing.type: Easing.InOutQuad
                }
            }
            Behavior on y {
                NumberAnimation {
                    duration: 500
                    easing.type: Easing.InOutQuad
                }
            }
        }
        Model {
            id: doorWindow_b_R_low
            objectName: "DoorWindow_b_R_low"
            x: node.rightWindowUp? -0.6165767908096313 : -0.6565767908096313
            y: node.rightWindowUp? 0.2809993028640747 : 0.1409993028640747
            z: -0.6550559401512146
            source: "meshes/doorWindow_b_R_low_mesh.mesh"
            eulerRotation.y: node.rightDoorOpen? 74 : 0
            materials: [
                m_windowGlass_material
            ]
            Behavior on eulerRotation.y {
                NumberAnimation {
                    duration: 500
                    easing.type: Easing.InOutQuad
                }
            }
            Behavior on x {
                NumberAnimation {
                    duration: 500
                    easing.type: Easing.InOutQuad
                }
            }
            Behavior on y {
                NumberAnimation {
                    duration: 500
                    easing.type: Easing.InOutQuad
                }
            }
        }
        Model {
            id: mainbody_b_Low
            objectName: "Mainbody_b_Low"
            source: "meshes/mainbody_b_Low_mesh.mesh"
            materials: [
                m_CarPaint_material,
                m_Black_material
            ]
        }

        Model {
            id: door_b_L_Low
            objectName: "Door_b_L_Low"
            x: -0.6165767908096313
            y: 0.2809993028640747
            z: 0.6550559401512146
            source: "meshes/door_b_L_Low_mesh.mesh"
            eulerRotation.y: node.leftDoorOpen? -70 : 0
            materials: [
                m_CarPaint_material,
                m_Doors_material,
                m_DarkPlastic_material,
                m_Screens_material
            ]
            Behavior on eulerRotation.y {
                NumberAnimation {
                    duration: 500
                    easing.type: Easing.InOutQuad
                }
            }
        }
        Model {
            id: door_b_R_Low
            objectName: "Door_b_R_Low"
            x: -0.6165767908096313
            y: 0.2809993028640747
            z: -0.6550559401512146
            source: "meshes/door_b_R_Low_mesh.mesh"
            eulerRotation.y: node.rightDoorOpen? 70 : 0
            materials: [
                m_CarPaint_material,
                m_Doors_material,
                m_DarkPlastic_material,
                m_Screens_material
            ]
            Behavior on eulerRotation.y {
                NumberAnimation {
                    duration: 500
                    easing.type: Easing.InOutQuad
                }
            }
        }

        Model {
            id: tire_FrontL_low
            objectName: "Tire_FrontL_low"
            x: -1.0675599575042725
            y: 0.26712650060653687
            z: 0.6252589821815491
            source: "meshes/tire_FrontL_low_mesh.mesh"
            materials: [
                m_Tire_material,
                m_ShinyMetal_material,
                m_CarPaint_material
            ]
            NumberAnimation on eulerRotation.z {
                from: 2160
                to: 0
                duration: 3000
                easing.type: Easing.Linear
            }
            NumberAnimation on eulerRotation.y {
                from: 40
                to: 0
                duration: 3000
                easing.type: Easing.Linear
            }
            NumberAnimation on eulerRotation.z {
                from: 2160
                to: 0
                duration: 3000
                easing.type: Easing.Linear
                loops: Animation.Infinite
                running: node.areTiresTurning
            }
        }
        Model {
            id: backL_Tire_b_low
            objectName: "BackL_Tire_b_low"
            x: 1.1479299068450928
            y: 0.26712650060653687
            z: 0.6252589821815491
            source: "meshes/backL_Tire_b_low_mesh.mesh"
            materials: [
                m_Tire_material,
                m_ShinyMetal_material,
                m_CarPaint_material
            ]
            NumberAnimation on eulerRotation.z {
                from: 2160
                to: 0
                duration: 3000
                easing.type: Easing.Linear
            }
            NumberAnimation on eulerRotation.z {
                from: 2160
                to: 0
                duration: 3000
                easing.type: Easing.Linear
                loops: Animation.Infinite
                running: node.areTiresTurning
            }
        }
        Model {
            id: frontR_Tire_b_Low
            objectName: "FrontR_Tire_b_Low"
            x: -1.0675599575042725
            y: 0.26712650060653687
            z: -0.6252589821815491
            rotation: Qt.quaternion(-7.54979e-08, 0, 1, 0)
            source: "meshes/frontR_Tire_b_Low_mesh.mesh"
            materials: [
                m_Tire_material,
                m_ShinyMetal_material,
                m_CarPaint_material
            ]
            NumberAnimation on eulerRotation.z {
                from: 2160
                to: 0
                duration: 3000
                easing.type: Easing.Linear
            }

            NumberAnimation on eulerRotation.z {
                from: 2160
                to: 0
                duration: 3000
                easing.type: Easing.Linear
                loops: Animation.Infinite
                running: node.areTiresTurning
            }
        }
        Model {
            id: backR_Tire_b_Low
            objectName: "BackR_Tire_b_Low"
            x: 1.1479300260543823
            y: 0.26712650060653687
            z: -0.6252589821815491
            rotation: Qt.quaternion(-1.62921e-07, 0, 1, 0)
            source: "meshes/backR_Tire_b_Low_mesh.mesh"
            materials: [
                m_Tire_material,
                m_ShinyMetal_material,
                m_CarPaint_material
            ]
            NumberAnimation on eulerRotation.z {
                from: 2160
                to: 0
                duration: 3000
                easing.type: Easing.Linear
            }

            NumberAnimation on eulerRotation.z {
                from: 2160
                to: 0
                duration: 3000
                easing.type: Easing.Linear
                loops: Animation.Infinite
                running: node.areTiresTurning
            }
        }
        Model {
            id: seat_front_low
            objectName: "Seat_front_low"
            x: -0.1593221127986908
            y: 0.38361045718193054
            z: 0.2873550057411194
            source: "meshes/seat_front_low_mesh.mesh"
            materials: [
                seatFront_M_material
            ]
        }
        Model {
            id: seat_rear_low
            objectName: "Seat_rear_low"
            x: 0.4633820056915283
            y: 0.40423914790153503
            z: -0.2809269428253174
            source: "meshes/seat_rear_low_mesh.mesh"
            materials: [
                seatRear_M_material
            ]
        }
        Model {
            id: wheel_screen_low
            objectName: "Wheel_screen_low"
            x: -0.3444710969924927
            y: 0.5826135873794556
            z: 0.2870819568634033
            rotation: Qt.quaternion(0.990268, 0, 0, 0.139173)
            scale.x: 0.109379
            scale.y: 0.109379
            scale.z: 0.109379
            source: "meshes/wheel_screen_low_mesh.mesh"
            materials: [
                m_Alcantara_material,
                m_Black_material,
                m_CarPaint_material,
                m_Screens_material
            ]
        }
        Model {
            id: insideFloor_low
            objectName: "InsideFloor_low"
            source: "meshes/insideFloor_low_mesh.mesh"
            materials: [
                m_InterirFloor_material,
                m_Black_material,
                m_GlassScreen_material,
                m_Centralpanels_material
            ]
        }
        Model {
            id: roof_b_001
            objectName: "Roof_b.001"
            source: "meshes/roof_b_001_mesh.mesh"
            materials: [
                m_Black_material,
                m_windowGlass_material,
                m_InteriorFabric_material
            ]
        }
    }

    Node {
        id: __materialLibrary__

        PrincipledMaterial {
            id: dashBoard_material
            objectName: "DashBoard"
            baseColorMap: _0_texture
            roughness: 0.800000011920929
            emissiveMap: _1_texture
            emissiveFactor.x: 1
            emissiveFactor.y: 1
            emissiveFactor.z: 1
            cullMode: PrincipledMaterial.NoCulling
            alphaMode: PrincipledMaterial.Opaque
        }

        PrincipledMaterial {
            id: m_Screens_material
            objectName: "M_Screens"
            baseColorMap: _2_texture
            roughness: 0.047272734344005585
            emissiveMap: _2_texture
            emissiveFactor.x: 1
            emissiveFactor.y: 1
            emissiveFactor.z: 1
            cullMode: PrincipledMaterial.NoCulling
            alphaMode: PrincipledMaterial.Opaque
        }

        PrincipledMaterial {
            id: m_Black_material
            objectName: "M_Black"
            baseColor: "#ff040404"
            roughness: 0.44999998807907104
            cullMode: PrincipledMaterial.NoCulling
            alphaMode: PrincipledMaterial.Opaque
        }

        PrincipledMaterial {
            id: m_Headlights_material
            objectName: "M_Headlights"
            baseColorMap: _3_texture
            roughness: 0.5
            emissiveMap: _3_texture
            emissiveFactor.x: 1
            emissiveFactor.y: 1
            emissiveFactor.z: 1
            cullMode: PrincipledMaterial.NoCulling
            alphaMode: PrincipledMaterial.Opaque
            indexOfRefraction: 1.4500000476837158
        }

        PrincipledMaterial {
            id: m_windowGlass_material
            objectName: "M_windowGlass"
            baseColor: "#80000000"
            roughness: 0.15000000596046448
            cullMode: PrincipledMaterial.NoCulling
            alphaMode: PrincipledMaterial.Blend
            clearcoatAmount: 1
            clearcoatRoughnessAmount: 1
        }

        PrincipledMaterial {
            id: seatFront_M_material
            objectName: "SeatFront_M"
            baseColorMap: _8_texture
            roughness: 0.800000011920929
            cullMode: PrincipledMaterial.NoCulling
            alphaMode: PrincipledMaterial.Opaque
        }

        PrincipledMaterial {
            id: m_Tire_material
            objectName: "M_Tire"
            baseColorMap: _6_texture
            roughness: 0.5
            normalMap: _7_texture
            cullMode: PrincipledMaterial.NoCulling
            alphaMode: PrincipledMaterial.Opaque
        }

        PrincipledMaterial {
            id: seatRear_M_material
            objectName: "SeatRear_M"
            baseColorMap: _9_texture
            roughness: 0.800000011920929
            cullMode: PrincipledMaterial.NoCulling
            alphaMode: PrincipledMaterial.Opaque
        }

        PrincipledMaterial {
            id: m_ShinyMetal_material
            objectName: "M_ShinyMetal"
            baseColor: "#ff030303"
            roughness: 0.05454540252685547
            cullMode: PrincipledMaterial.NoCulling
            alphaMode: PrincipledMaterial.Opaque
        }

        PrincipledMaterial {
            id: m_Alcantara_material
            objectName: "M_Alcantara"
            baseColorMap: _10_texture
            roughness: 0.8999999761581421
            cullMode: PrincipledMaterial.NoCulling
            alphaMode: PrincipledMaterial.Opaque
            indexOfRefraction: 1.4500000476837158
        }

        PrincipledMaterial {
            id: m_DarkPlastic_material
            objectName: "M_DarkPlastic"
            baseColor: "#ff060606"
            roughness: 0.550000011920929
            cullMode: PrincipledMaterial.NoCulling
            alphaMode: PrincipledMaterial.Opaque
            indexOfRefraction: 1.4500000476837158
        }

        PrincipledMaterial {
            id: m_InterirFloor_material
            objectName: "M_InterirFloor"
            baseColorMap: _11_texture
            roughness: 0.800000011920929
            cullMode: PrincipledMaterial.NoCulling
            alphaMode: PrincipledMaterial.Opaque
        }

        PrincipledMaterial {
            id: m_Doors_material
            objectName: "M_Doors"
            baseColorMap: _4_texture
            roughness: 0.800000011920929
            emissiveMap: _5_texture
            emissiveFactor.x: 1
            emissiveFactor.y: 1
            emissiveFactor.z: 1
            cullMode: PrincipledMaterial.NoCulling
            alphaMode: PrincipledMaterial.Opaque
        }

        PrincipledMaterial {
            id: m_GlassScreen_material
            objectName: "M_GlassScreen"
            baseColor: "#ad0c0c0c"
            roughness: 0.08756375312805176
            cullMode: PrincipledMaterial.NoCulling
            alphaMode: PrincipledMaterial.Blend
            transmissionFactor: 1
            indexOfRefraction: 1.4500000476837158
        }

        PrincipledMaterial {
            id: m_Centralpanels_material
            objectName: "M_Centralpanels"
            baseColorMap: _12_texture
            roughness: 0.800000011920929
            cullMode: PrincipledMaterial.NoCulling
            alphaMode: PrincipledMaterial.Opaque
        }

        PrincipledMaterial {
            id: m_InteriorFabric_material
            objectName: "M_InteriorFabric"
            baseColorMap: _13_texture
            roughness: 0.7502273917198181
            normalMap: _14_texture
            cullMode: PrincipledMaterial.NoCulling
            alphaMode: PrincipledMaterial.Opaque
        }

        PrincipledMaterial {
            id: m_CarPaint_material
            objectName: "M_CarPaint"
            baseColor: "#ffa9bdff"
            metalness: 0.25
            roughness: 0.10000000149011612
            cullMode: PrincipledMaterial.NoCulling
            alphaMode: PrincipledMaterial.Opaque
            clearcoatAmount: 1
        }
    }

    // Animations:
}