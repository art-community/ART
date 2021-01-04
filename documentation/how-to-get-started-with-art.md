# Okey, ART. Lets start!

### Open Intellij IDEA and create new project with Gradle template
![](https://i.ibb.co/9q93W4y/2019-08-26-140804.png)
![](https://i.ibb.co/BNRz30D/image.png)
![](https://i.ibb.co/Th3Dqvy/image.png)

### Edit build.gradle.kts file:
```kotlin
tasks.withType(Wrapper::class) {
    gradleVersion = "5.6"
}

group = "ru"
version = "1.0-SNAPSHOT"
```

### Wait until gradle finish configuring and change build file to
```kotlin
plugins {
    id("io.github.art.project") version "1.0.113" // apply ART plugin for simplify project Gradle configuring
}

repositories {
    jcenter()
}

tasks.withType(Wrapper::class) {
    gradleVersion = "5.6"
}

art {
    idea() // apply Intellij IDEA Gradle configuration
    lombok() // add Lombok
    embeddedModules {
        kit() // add ART modules to project as embedded dependencies (include into result *jar)
    }
}
group = "ru"
version = "1.0-SNAPSHOT"
```
Now click "Import gradle project".

### Create MainModule.java inside src/main/java directory
``` java
import static ru.art.config.extensions.activator.AgileConfigurationsActivator.useAgileConfigurations;
import static ru.art.entity.PrimitiveMapping.StringPrimitive.fromModel;
import static ru.art.http.server.HttpServer.httpServer;
import static ru.art.http.server.function.HttpServiceFunction.httpGet;

public class MainModule {
    public static void main(String[] args) {
        useAgileConfigurations(MainModule.class.getName());
        httpGet("/hello")
                .responseMapper(fromModel)
                .produce(() -> "<h1>Hello, ART!</h1>");
        startHttpServer().await();
    }
}
```
Now run created `main(...)`

### Look into logs
```
...
Registered HTTP service method for path: 'http://<your_ip>:<selected_port>/hello' with HTTP service id 'HTTP_SERVICE(/hello)' and HTTP request types '[GET]'
...
```

### Open browser with url http://<your_ip>:<selected_port>/hello and see magic
![](https://i.ibb.co/x53YDpR/image.png)

### Next steps
You can try some ART functional with help of [examples](https://github.com/art-community/ART/tree/latest/documentation/examples.md)

To see what use cases  ART is solving see [Use Cases](https://github.com/art-community/ART/tree/latest/documentation/use-cases.md)
