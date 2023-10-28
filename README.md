# FunkoDocker
***
**"Funkos Server"** es un servidor diseñado para la gestión de Funkos. El servidor proporciona funcionalidades de CRUD junto con búsqueda por **ID**, **Modelo**, y **año**.
Se ha implementado un sistema de autenticación y autorización basado en **JWT**. para asegurar que solo los administradores puedan eliminar funkos. Además, todas las conexiones son seguras.

# Características
- **Carga de Datos**: Al iniciar, el servidor lee el archivo **"funkos.csv"** del directorio **"data"** y carga la información a una base de datos.
- **Autenticación y Autorización**: Basado en JWT, asegurando que solo los administradores tengan permisos para eliminar funkos.
- **Resiliente**: Diseñado para manejar errores inesperados de manera eficiente.
- **Desplegable en Docker**: Facilita el despligue y escalabilidad.
- **Logger**: Registra todas las operaciones y eventos significativos.

# Cómo empezar
1. **Instalación**:
    - Clona el repositorio: ```git clone https://github.com/Binweiwang/FunkoDocker.git```

2. **Ejecución**:
    - Construir el docker: ```docker-compose -f docker-compose.yaml up --build```

3. **Uso**:
    - Utiliza un cliente para realizar peticiones al servidor.

# Testing
Se ha hecho un especial énfasis en el testeo, asegurando una cobertura del 85%. Para ver el informe de cobertura:
    - Ejecute: ```./gradle test jacocoTestReport```
    - Abra el archivo ```"build/reports/jacoco/test/html/index.html"``` en un navegador.