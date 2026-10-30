# Evaluador de Créditos

Aplicación de escritorio en Java (Swing) que evalúa si un cliente (persona física o empresa) es apto para un préstamo y guarda los datos en MySQL.

## Requisitos

- Java 17 o superior
- MySQL 8 o superior
- Maven (viene incluido en IntelliJ IDEA y NetBeans)

## Cómo ejecutarlo

### 1. Crear la base de datos

Ejecutar los dos scripts de la carpeta `EvaluadorCreditos/sql/`, **en este orden**, desde MySQL Workbench o la consola de MySQL:

1. `01_tablas.sql`: crea la base `evaluador_creditos`, las tablas y el usuario inicial.
2. `02_stored_procedures.sql`: crea los procedimientos almacenados que usa la aplicación.

> El script 01 borra y vuelve a crear las tablas, así que se pierden los datos que hubiera.

Si al ingresar aparece el error *"Parameter number 3 is not an OUT parameter"*, falta ejecutar el script 02.

### 2. Configurar la conexión

Abrir `EvaluadorCreditos/src/main/java/com/example/data/ConexionBD.java` y poner el usuario y la clave de su MySQL:

```java
private static final String url = "jdbc:mysql://localhost:3306/evaluador_creditos";
private static final String user = "root";
private static final String password = "su_clave";
```

### 3. Abrir y ejecutar

1. Abrir la carpeta `EvaluadorCreditos` como proyecto Maven (en IntelliJ: *File → Open* y elegir `EvaluadorCreditos/pom.xml` → *Open as Project*).
2. Esperar a que Maven descargue el conector de MySQL.
3. Ejecutar la clase `com.example.Main`.

### 4. Ingresar

| Usuario | Clave |
|---|---|
| `Bonillo` | `Pequenio` |

El login distingue mayúsculas de minúsculas.

## Estructura

```
EvaluadorCreditos/
├── sql/                     Scripts de la base (tablas y stored procedures)
└── src/main/java/com/example/
    ├── Cliente, PersonaFisica, Empresa, Usuario   Modelo
    ├── VentanaLogin, VentanaPrincipal, VentanaUsuarios   Interfaz
    ├── data/                Acceso a la base (solo mediante stored procedures)
    └── servicio/            Lógica del programa que usa la interfaz
```

## Reglas de evaluación

- **Persona física:** apta si tiene buen historial y le queda libre al menos el 30% de sus ingresos después de los gastos.
- **Empresa:** apta si sus ingresos son al menos 1,5 veces sus gastos (con gastos en 0, apta si tiene ingresos).
