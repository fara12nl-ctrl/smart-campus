# Smart Campus public tunnel - auto restart + reliable URL detection via logfile
$ErrorActionPreference = "SilentlyContinue"
$tools = "C:\Users\ABU TALHA\tools"
$app = Split-Path $PSScriptRoot -Parent
$urlFile = Join-Path $app "tunnel-url.txt"
$logFile = Join-Path $tools "tunnel-run.log"
$cf = Join-Path $tools "cloudflared.exe"

if (-not (Test-Path $cf)) {
    Write-Host "cloudflared download ho raha hai (~55 MB)..." -ForegroundColor Cyan
    Invoke-WebRequest -UseBasicParsing -Uri "https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe" -OutFile $cf
}

while ($true) {
    Remove-Item $logFile -Force
    Write-Host ""
    Write-Host "Tunnel connect ho raha hai..." -ForegroundColor Cyan

    $proc = Start-Process -FilePath $cf `
        -ArgumentList "tunnel","--url","http://127.0.0.1:8080","--no-autoupdate","--logfile","`"$logFile`"" `
        -PassThru -WindowStyle Hidden

    $announced = $false
    for ($i = 0; $i -lt 40; $i++) {
        Start-Sleep -Seconds 1
        if (Test-Path $logFile) {
            $hit = Select-String -Path $logFile -Pattern "https://[a-z0-9-]+\.trycloudflare\.com" | Select-Object -First 1
            if ($hit) {
                $url = $hit.Matches[0].Value
                Set-Content -Path $urlFile -Value $url
                Write-Host ""
                Write-Host "==========================================================" -ForegroundColor Green
                Write-Host "  PUBLIC URL: $url" -ForegroundColor Yellow
                Write-Host "  App ke 'Server URL' field me yahi daalo" -ForegroundColor Green
                Write-Host "  Phone browser test: $url/api/health" -ForegroundColor Green
                Write-Host "  (Yeh URL project folder ke tunnel-url.txt me bhi saved hai)" -ForegroundColor Green
                Write-Host "==========================================================" -ForegroundColor Green
                Write-Host ""
                $announced = $true
                break
            }
        }
        if ($proc.HasExited) { break }
    }
    if (-not $announced -and -not $proc.HasExited) {
        Write-Host "URL detect nahi hua, logs: $logFile" -ForegroundColor Red
    }

    $proc.WaitForExit()
    Write-Host "Tunnel disconnect hua - 5 second me auto-retry..." -ForegroundColor Red
    Start-Sleep -Seconds 5
}
