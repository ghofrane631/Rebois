# Verification rapide du projet (lecture seule : aucune donnee modifiee).
# Usage : powershell -ExecutionPolicy Bypass -File .\verifier.ps1
$checks = @(
  @{ Nom = "Config Server";                 Url = "http://localhost:8888/ressources-service/default"; Code = 200 },
  @{ Nom = "Eureka (dashboard)";            Url = "http://localhost:8761";                           Code = 200 },
  @{ Nom = "Ressources - actuator/health";  Url = "http://localhost:8085/actuator/health";           Code = 200 },
  @{ Nom = "Ressources - actuator/info";    Url = "http://localhost:8085/actuator/info";             Code = 200 },
  @{ Nom = "Ressources - Swagger";          Url = "http://localhost:8085/v3/api-docs";               Code = 200 },
  @{ Nom = "Gateway -> liste des moyens";   Url = "http://localhost:8080/api/ressources";            Code = 200 },
  @{ Nom = "Gateway -> moyen inconnu (404)";Url = "http://localhost:8080/api/ressources/999999";     Code = 404 }
)
$ok = 0
foreach ($c in $checks) {
  try   { $code = (Invoke-WebRequest -Uri $c.Url -UseBasicParsing -TimeoutSec 5).StatusCode }
  catch { $code = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { "injoignable" } }
  if ($code -eq $c.Code) { Write-Host ("[OK] {0} ({1})" -f $c.Nom, $code) -ForegroundColor Green; $ok++ }
  else                   { Write-Host ("[KO] {0} : attendu {1}, recu {2}" -f $c.Nom, $c.Code, $code) -ForegroundColor Red }
}
try {
  $apps = (Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps" -Headers @{Accept="application/json"} -TimeoutSec 5).applications.application
  foreach ($a in $apps) { $s = ($a.instance | ForEach-Object { $_.status }) -join ","; Write-Host ("[EUREKA] {0} : {1} instance(s) {2}" -f $a.name, @($a.instance).Count, $s) -ForegroundColor Cyan }
} catch { Write-Host "[KO] Impossible de lire le registre Eureka" -ForegroundColor Red }
Write-Host ("`n{0}/{1} verifications reussies" -f $ok, $checks.Count)
