# Reporte de Vulnerabilidades

## Herramienta:
Se está usando OWASP dependency-check-gradle en su vesión 8.0.2 (latest):
https://plugins.gradle.org/plugin/org.owasp.dependencycheck <br />
Repo: https://github.com/dependency-check/dependency-check-gradle <br />
Configuración: https://jeremylong.github.io/DependencyCheck/dependency-check-maven/configuration.html
<br />
<br />

## Ejecutar Análisis:
- Navegar hasta el root del proyecto (connector-root)
- Ejecutar: ./gradlew dependencyCheckAnalyze
- Cada subproyecto generará un reporte en formato .html 
- El reporte se ubica en <subproyecto>/build/reports/dependency-check-report.html
<br />