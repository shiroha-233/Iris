pluginManagement {
    repositories {
        maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://repo.huaweicloud.com/repository/maven/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://repo.huaweicloud.com/repository/maven/")
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.su5ed.dev/releases")
        maven {
            name = "Modrinth"
            url = uri("https://api.modrinth.com/maven")
        }
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.neoforged.net/mojang-meta/")
    }
}

rootProject.name = "Iris"

include("common")
include("fabric")
include("neoforge")