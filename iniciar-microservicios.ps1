param(
    [switch]$Detener
)

$servicios = @(
    @{ Nombre = 'auth';         Directorio = 'C:\BIBLIOTECA\auth';         Jar = 'target\auth-0.0.1-SNAPSHOT.jar';         Puerto = 8081; Argumentos = @() },
    @{ Nombre = 'catalogo';     Directorio = 'C:\BIBLIOTECA\catalogo';     Jar = 'target\catalogo-0.0.1-SNAPSHOT.jar';     Puerto = 8082; Argumentos = @() },
    @{ Nombre = 'prestamos';    Directorio = 'C:\BIBLIOTECA\prestamos';    Jar = 'target\prestamos-0.0.1-SNAPSHOT.jar';    Puerto = 8083; Argumentos = @() },
    @{ Nombre = 'notificacion'; Directorio = 'C:\BIBLIOTECA\notificacion'; Jar = 'target\notificacion-0.0.1-SNAPSHOT.jar'; Puerto = 8084; Argumentos = @() }
)

if ($Detener) {
    foreach ($servicio in $servicios) {
        $pidFile = Join-Path $servicio.Directorio 'app.pid'

        if (Test-Path $pidFile) {
            $processId = Get-Content $pidFile
            Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
            Write-Host "Detenido $($servicio.Nombre) (PID $processId)"
            Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
        }
    }

    return
}

foreach ($servicio in $servicios) {
    $conexion = Get-NetTCPConnection -LocalPort $servicio.Puerto -State Listen -ErrorAction SilentlyContinue

    if ($conexion) {
        Write-Host "$($servicio.Nombre) ya esta ejecutandose en el puerto $($servicio.Puerto) (PID $($conexion.OwningProcess))"
        continue
    }

    $log = Join-Path $servicio.Directorio 'app.log'
    $errorLog = Join-Path $servicio.Directorio 'app.err.log'
    $pidFile = Join-Path $servicio.Directorio 'app.pid'

    Remove-Item $log, $errorLog -Force -ErrorAction SilentlyContinue

    $javaArgs = @('-jar', $servicio.Jar) + $servicio.Argumentos

    $proceso = Start-Process `
        -FilePath 'java' `
        -ArgumentList $javaArgs `
        -WorkingDirectory $servicio.Directorio `
        -PassThru `
        -WindowStyle Hidden `
        -RedirectStandardOutput $log `
        -RedirectStandardError $errorLog

    $proceso.Id | Set-Content $pidFile
    Write-Host "$($servicio.Nombre) iniciado en el puerto $($servicio.Puerto) (PID $($proceso.Id))"
}

Start-Sleep -Seconds 2

$pendientes = $servicios | ForEach-Object { $_.Nombre }

for ($intento = 0; $intento -lt 60; $intento++) {
    $listos = @()
    $fallidos = @()

    foreach ($servicio in $servicios) {
        try {
            $respuesta = Invoke-WebRequest `
                -Uri "http://localhost:$($servicio.Puerto)/actuator/health" `
                -UseBasicParsing `
                -TimeoutSec 3

            if ($respuesta.StatusCode -eq 200) {
                $listos += $servicio.Nombre
            } else {
                $fallidos += $servicio.Nombre
            }
        } catch {
            $fallidos += $servicio.Nombre
        }
    }

    $pendientes = $fallidos

    if ($fallidos.Count -eq 0) {
        break
    }

    Start-Sleep -Seconds 2
}

foreach ($servicio in $servicios) {
    $conexion = Get-NetTCPConnection -LocalPort $servicio.Puerto -State Listen -ErrorAction SilentlyContinue

    if ($conexion) {
        Write-Host "OK $($servicio.Nombre) -> http://localhost:$($servicio.Puerto) (PID $($conexion.OwningProcess))"
    } else {
        Write-Host "ERROR $($servicio.Nombre) no esta escuchando en el puerto $($servicio.Puerto)"
        $log = Join-Path $servicio.Directorio 'app.log'
        if (Test-Path $log) {
            Get-Content $log -Tail 20
        }
    }
}
