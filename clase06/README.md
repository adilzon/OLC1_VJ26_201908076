## Requisitos

- Java JDK 21
- Apache Maven 3.9 o superior

Verifique la instalación con:

```bash
java -version
mvn -version
```

## Compilación

Ubíquese en la carpeta donde se encuentra el archivo `pom.xml` y ejecute:

```bash
mvn clean
mvn compile
```

Si la compilación finaliza correctamente, Maven mostrará el mensaje:

```text
BUILD SUCCESS
```

## Generar el archivo ejecutable

Para generar el archivo JAR con todas las dependencias:

```bash
mvn clean package
```

El archivo generado se encontrará en:

```text
target/clase06-1.0-jar-with-dependencies.jar
```

## Ejecutar la aplicación

```bash
java -jar target/clase06-1.0-jar-with-dependencies.jar
```