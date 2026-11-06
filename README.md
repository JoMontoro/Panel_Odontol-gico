# 🦷 Consultorio Odontológico - Panel de Gestión

Sistema de gestión para consultorio odontológico con historial clínico, citas, tratamientos, presupuestos y **módulo de Consentimiento Informado con firma digital del paciente**.

## 📋 Descripción

Este proyecto consta de dos partes:

- **Backend:** Spring Boot 3.3.4 (Java 17) con JPA, MySQL, JWT y generación de PDF (iText).
- **Frontend:** Angular 20 con Bootstrap 5, consumiendo la API REST del backend en `http://localhost:8088`.

Incluye el módulo de **Consentimiento Informado** que permite:
- Registrar consentimientos con firma digital del paciente (capturada vía canvas).
- Generar PDF profesional con datos del paciente, cláusulas y firmas.
- Descargar el PDF generado para respaldo legal.

---

## 🛠️ Requisitos previos (ambas PCs)

Antes de instalar en cualquier máquina, asegúrate de tener lo siguiente:

| Componente | Versión recomendada | Descarga |
|------------|---------------------|----------|
| **Docker Desktop** | Última estable | https://www.docker.com/products/docker-desktop/ |
| **Java JDK** | 17 LTS | https://adoptium.net/ |
| **Node.js** | 18 o 20 LTS | https://nodejs.org/ |
| **Git** (opcional) | Última | https://git-scm.com/ |
| **Google Chrome** | Última | https://www.google.com/chrome/ |

---

## 📁 Estructura del proyecto

```
Consultorio odontologico Proyecto/
├── Panel_Odontol-gico/          # BACKEND (Spring Boot)
│   ├── docker-compose.yml       # Base de datos MySQL en Docker
│   ├── init.sql                 # Exportación de la base de datos (opcional)
│   ├── pom.xml
│   ├── src/
│   └── README.md
│
└── Interfaz_Consultorio/
    └── panel-odontologico/      # FRONTEND (Angular)
        ├── src/
        ├── package.json
        └── angular.json
```

---

## 🐳 Paso 1: Base de datos MySQL en Docker

El proyecto usa Docker para correr MySQL, evitando instalar MySQL directamente en Windows.

### Archivo `docker-compose.yml` (ya incluido en `Panel_Odontol-gico/`)

```yaml
version: '3.8'

services:
  mysql-odontologico:
    image: mysql:8.0
    container_name: mysql-odontologico
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: odontologico_db
      MYSQL_USER: root
      MYSQL_PASSWORD: root
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - odonto-net

networks:
  odonto-net:
    driver: bridge

volumes:
  mysql_data:
    driver: local
```

### Exportar tu base de datos actual (MySQL Workbench)

Si ya tienes datos en MySQL Workbench y quieres llevarlos a Docker:

1. Abre **MySQL Workbench** → conéctate a tu base de datos `odontologico_db`.
2. Ve a **Server → Data Export**.
3. Selecciona tu base de datos y elige **"Dump Structure and Data"**.
4. En "Export to:" selecciona **"Dump to Self-Contained File"**.
5. Guarda el archivo como `init.sql` dentro de la carpeta `Panel_Odontol-gico/`.
6. Haz clic en **Start Export**.

> Este archivo `init.sql` se importará automáticamente la primera vez que levantes el contenedor Docker.

---

## 🚀 Paso 2: Instalación en otra PC

Sigue estos pasos en la máquina donde quieras instalar el sistema:

### 2.1 Instalar software necesario

1. **Docker Desktop**
   - Descarga e instala desde: https://www.docker.com/products/docker-desktop/
   - **Importante:** En la configuración de Docker Desktop, activa la opción **"Start Docker Desktop when you log in"** para que inicie con Windows.

2. **Java JDK 17**
   - Descarga desde: https://adoptium.net/
   - Configura la variable de entorno `JAVA_HOME`:
     ```
     JAVA_HOME = C:\Program Files\Java\jdk-17
     Path = %JAVA_HOME%\bin
     ```

3. **Node.js** (incluye npm)
   - Descarga desde: https://nodejs.org/ (versión LTS)

4. **Git** (opcional, recomendado para compartir código)
   - Descarga desde: https://git-scm.com/

### 2.2 Copiar el proyecto

Copia la carpeta completa del proyecto a la otra PC (por USB, red compartida, o clonando desde Git si usas repositorio remoto).

### 2.3 Ejecutar el lanzador automático

El proyecto incluye un archivo `.bat` ubicado en:
```
C:\Users\hp\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\Startup\Lanzador_Proyecto.bat
```

Este archivo se ejecuta automáticamente al prender la PC, pero también puedes ejecutarlo manualmente:

1. Haz doble clic en `Lanzador_Proyecto.bat`.
2. El script hará lo siguiente:
   - Levanta MySQL en Docker (`docker-compose up -d`).
   - Espera a que la base de datos esté lista.
   - Inicia el backend Spring Boot en `http://localhost:8088`.
   - Inicia el frontend Angular en `http://localhost:4200`.
   - Abre Google Chrome en la interfaz.

---

## 🖥️ Ejecución manual (si no usas el .bat)

Si prefieres iniciar los servicios por separado:

### Backend
```powershell
cd "C:\Users\hp\Desktop\carpeta de jose\Consultorio odontologico Proyecto\Panel_Odontol-gico"
docker-compose up -d
# Esperar a que MySQL esté listo (aprox. 15 segundos)
./mvnw spring-boot:run
```

### Frontend
```powershell
cd "C:\Users\hp\Desktop\carpeta de jose\Consultorio odontologico Proyecto\Interfaz_Consultorio\panel-odontologico"
npm start
```

Luego abre el navegador en: `http://localhost:4200`

---

## 🌐 Puertos utilizados

| Servicio | Puerto | URL |
|----------|--------|-----|
| Frontend (Angular) | 4200 | http://localhost:4200 |
| Backend (Spring Boot) | 8088 | http://localhost:8088 |
| Base de datos (MySQL Docker) | 3306 | localhost:3306 |

---

## 🔄 Cómo compartir cambios entre máquinas

Hay dos tipos de cambios: **código** y **datos**.

### A. Cambios de código (Java, Angular, HTML, CSS)

Los cambios de código **no se reflejan automáticamente** en otra máquina. Cada PC tiene su propia copia del proyecto.

**Flujo recomendado con Git:**

En tu PC (donde haces los cambios):
```bash
git add .
git commit -m "Descripción del cambio"
git push origin main
```

En la otra PC:
```bash
git pull origin main
# Luego reinicia el lanzador (.bat) o los servicios por separado
```

**Alternativa sin Git:** Copia los archivos modificados por USB/red y reemplázalos en la otra PC, luego reinicia los servicios.

### B. Cambios de datos (pacientes, citas, consentimientos, presupuestos)

Los datos se reflejan automáticamente **solo si ambas máquinas usan la misma base de datos**.

| Opción | Cómo configurarlo | Ventaja |
|--------|-------------------|---------|
| **MySQL en Docker en una PC servidor** | Una PC levanta Docker con MySQL. Las otras PCs modifican su `application.properties` para apuntar a la IP de esa PC (`spring.datasource.url=jdbc:mysql://192.168.1.X:3306/odontologico_db`). | Los datos se ven al instante en todas las PCs |
| **MySQL en la nube** (AWS RDS, Azure, etc.) | Configura `application.properties` con la URL de la nube. | Accesible desde cualquier lugar |
| **Cada PC con su propio Docker** | Cada máquina tiene su propia BD local. | Los datos NO se comparten entre PCs |

---

## 🔧 Solución de problemas comunes

### El backend no inicia (error de Java)
```
Asegúrate de que JAVA_HOME apunte a JDK 17:
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%
```

### Error de conexión a la base de datos
```
Verifica que Docker esté corriendo:
docker ps

Deberías ver el contenedor mysql-odontologico en estado "Up".
Si no está, ejecuta: docker-compose up -d
```

### El frontend no carga o da error de CORS
```
El backend tiene configurado @CrossOrigin(origins = "http://localhost:4200").
Asegúrate de que ambos servicios estén corriendo en sus puertos correctos.
```

### No se genera el PDF del consentimiento
```
El PDF se genera en la carpeta uploads/consentimientos/ del backend.
Verifica que el backend tenga permisos de escritura en esa carpeta.
```

### Docker Desktop no inicia
```
Asegúrate de tener habilitada la virtualización en la BIOS de tu PC.
Reinicia Docker Desktop y espera a que el icono de la bandeja del sistema deje de parpadear.
```

---

## 📞 Soporte

Si tienes problemas con la instalación en otra máquina, verifica en orden:

1. ¿Docker Desktop está corriendo? (`docker ps`)
2. ¿Java 17 está configurado en `JAVA_HOME`? (`java -version`)
3. ¿Node.js está instalado? (`node -v` y `npm -v`)
4. ¿Los puertos 8088, 4200 y 3306 están libres?

---

## 📄 Licencia

Proyecto desarrollado para gestión de consultorio odontológico.
