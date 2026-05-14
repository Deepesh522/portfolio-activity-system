plugins {
    java
    id("org.springframework.boot") version "3.3.5" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
}

allprojects {
    group = "com.portfolio"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.34")
        annotationProcessor("org.projectlombok:lombok:1.18.34")
        testCompileOnly("org.projectlombok:lombok:1.18.34")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.addAll(listOf("-parameters"))
        options.encoding = "UTF-8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
