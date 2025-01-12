// Copyright (C) 2024 The Qt Company Ltd.
// SPDX-License-Identifier: LicenseRef-Qt-Commercial OR GPL-3.0-only WITH Qt-GPL-exception-1.0

#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QQuickView>
#include <cstdio>

int main(int argc, char *argv[])
{
    printf("Cpp reporting");

    qputenv("QT_ENABLE_HIGHDPI_SCALING", "0");
    QGuiApplication app(argc, argv);

    QQmlApplicationEngine engine;

    engine.addImportPath(QCoreApplication::applicationDirPath() + "/qml");
    engine.addImportPath(":/");

    QSurfaceFormat fmt;
    fmt.setSwapInterval(0);
    QSurfaceFormat::setDefaultFormat(fmt);

    return app.exec();
}

