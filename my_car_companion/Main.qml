// Copyright (C) 2024 The Qt Company Ltd.
// SPDX-License-Identifier: LicenseRef-Qt-Commercial OR GPL-3.0-only WITH Qt-GPL-exception-1.0

import QtQuick

Rectangle {
    visible: true
    color: "teal"

    Text {
        id: helloText
        color: "white"
        text: qsTr("Hello World from QML")
        anchors.centerIn: parent
    }

    Component.onCompleted: {
        console.log("QML reporting")
    }
}

