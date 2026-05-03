plugins {
    id("java")
}

group = "ru.dzhibrony"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven("https://repo.luminiadev.com/snapshots")
    maven("https://repo.luminiadev.com/releases")
}

val devKitDir = file("C:/Users/vla71/OneDrive/Документы/PluginsCodex/LumiDevKit")
val serverDir = file("C:/Users/vla71/OneDrive/Рабочий стол/СЕРВЕРА Minecrfat/Сервер Lumi")

dependencies {
    compileOnly(files(devKitDir.resolve("Sources/Lumi/build/libs/Lumi-1.5.0-SNAPSHOT.jar")))
    compileOnly(files(serverDir.resolve("plugins/FormConstructor.jar")))
    compileOnly(files(serverDir.resolve("plugins/JOOQConnector-Nukkit-1.0.1.jar")))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks.register<Copy>("copyToServer") {
    dependsOn(tasks.jar)
    from(tasks.jar.flatMap { it.archiveFile })
    into(serverDir.resolve("plugins"))
}
