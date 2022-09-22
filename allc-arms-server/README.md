# Arms SERVER

## Agregar repositorio azure

Para poder descargar las dependencias es necesario agregar en el pom, la url del repositorio de azure.

```shell script
<repositories>
        <repository>
            <id>commons-lib</id>
            <url>https://pkgs.dev.azure.com/NUO-DEV/Nuo-Retail-Pipeline-Test/_packaging/commons-lib/maven/v1</url>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
```

Es necesario crear un archivo con las credenciales para maven.

Si no existe crear el siguiente arhivo:

~/.m2/settings.xml

Completarlo con la siguiente informacion, se debe generar en azure devops un
[token](https://dev.azure.com/NUO-DEV/_usersSettings/tokens) de acceso y reemplazarlo en el tag password.

```shell script
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
    https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>commons-lib</id>
      <username>NUO-DEV</username>
      <password>TOKEN_AZURE</password>
    </server>
  </servers>
</settings>
```

