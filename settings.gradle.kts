pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://www.jitpack.io" )
        } //jitpack사용, 그래프 그리기
    }
}

rootProject.name = "NaviProj"
include(":app")
 