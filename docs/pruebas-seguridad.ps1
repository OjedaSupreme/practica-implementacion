# Pruebas de seguridad de la autenticación JWT.
# Uso: con el backend en el puerto 8080, en PowerShell:  .\docs\pruebas-seguridad.ps1
$base = "http://localhost:8080"
$tokenExpirado = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJlYTJhZjU1NS02Y2M4LTRiZTQtODgxZi04NjUzMDgzMDdjMGMiLCJpc3MiOiJzaXN0ZW1hLXRhcmVhcyIsInN1YiI6Implc3VzIiwicm9sIjoiVVNVQVJJTyIsImlhdCI6MTc5NDk2MDAwMCwiZXhwIjoxNzk0OTYwOTAwfQ.lgy8_3aYtQ_pP4-43j4yaOjNdSbUziwCMGlqHTzAN8c"
$tokenFalso = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJhZDY1OThkZC04MjAzLTQ0MWItYWM5My1lMzBmZjkxZGE5MmMiLCJpc3MiOiJzaXN0ZW1hLXRhcmVhcyIsInN1YiI6ImF0YWNhbnRlIiwicm9sIjoiQURNSU4iLCJpYXQiOjE3OTQ5NjAwMDAsImV4cCI6MTg5MzQ1NjAwMH0.PG3vhqnYR7oZE6_WxqAXP6WBT0mvOn4-F3PUHpYOuws"

function Probar($titulo, $metodo, $ruta, $token = $null, $cuerpo = $null, $sesion = $null) {
    $h = @{}
    if ($token) { $h["Authorization"] = "Bearer $token" }
    $p = @{ Method = $metodo; Uri = "$base$ruta"; Headers = $h; ContentType = "application/json"; SkipHttpErrorCheck = $true }
    if ($cuerpo) { $p["Body"] = ($cuerpo | ConvertTo-Json) }
    if ($sesion) { $p["WebSession"] = $sesion }
    $r = Invoke-WebRequest @p
    Write-Host ""
    Write-Host ("== {0}" -f $titulo) -ForegroundColor Cyan
    Write-Host ("   {0} {1} -> HTTP {2}" -f $metodo, $ruta, [int]$r.StatusCode) -ForegroundColor Yellow
    if ($r.Content) { Write-Host ("   {0}" -f $r.Content) }
    return $r
}

$s = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$login = Probar "Login ADMIN" POST "/api/auth/login" $null @{ username = "admin"; password = "Admin123*" } $s
$tokenAdmin = ($login.Content | ConvertFrom-Json).accessToken
Probar "Login con contraseña incorrecta" POST "/api/auth/login" $null @{ username = "admin"; password = "incorrecta" } | Out-Null
Probar "Ruta protegida SIN token" GET "/api/tareas" | Out-Null
Probar "Token con firma falsa" GET "/api/tareas" $tokenFalso | Out-Null
Probar "Token expirado" GET "/api/tareas" $tokenExpirado | Out-Null
$t = Probar "Crear tarea con token válido" POST "/api/tareas" $tokenAdmin @{ titulo = "Preparar reporte"; responsable = "Jesus" }
$id = ($t.Content | ConvertFrom-Json).id
$s2 = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$lu = Probar "Login USUARIO" POST "/api/auth/login" $null @{ username = "jesus"; password = "Usuario123*" } $s2
$tokenUsuario = ($lu.Content | ConvertFrom-Json).accessToken
Probar "DELETE con rol USUARIO" DELETE "/api/tareas/$id" $tokenUsuario | Out-Null
Probar "Refresh (cookie HttpOnly)" POST "/api/auth/refresh" $null $null $s2 | Out-Null
Probar "Logout" POST "/api/auth/logout" $null $null $s2 | Out-Null
Probar "Refresh después del logout" POST "/api/auth/refresh" $null $null $s2 | Out-Null
