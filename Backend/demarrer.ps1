# Demarre l'infrastructure et le service Ressources dans l'ordre (une fenetre par service).
# Prerequis : MySQL (XAMPP) lance, port 8080 libre (Oracle arrete).
# Usage : powershell -ExecutionPolicy Bypass -File .\demarrer.ps1
$b = $PSScriptRoot
function Lancer($nom, $dossier, $attente, $avant = "") {
  Write-Host "Demarrage de $nom..." -ForegroundColor Cyan
  Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$host.UI.RawUI.WindowTitle='$nom'; cd '$b\$dossier'; $avant mvn spring-boot:run"
  Start-Sleep $attente
}
Lancer "config-server"      "config-server"                   30
Lancer "discovery-server"   "discovery-server"                30
Lancer "ressources-service" "microservices\ressources-service" 40 "`$env:RABBIT_HEALTH='false';"
Lancer "api-gateway"        "api-gateway"                     30
Write-Host "Termine. Verifier : http://localhost:8761 puis http://localhost:8080/api/ressources" -ForegroundColor Green
Start-Process "http://localhost:8761"
