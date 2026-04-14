param(
    [switch]$NoGateway,
    [switch]$DryRun,
    [string]$EnvFile = ".env"
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$envPath = if ([System.IO.Path]::IsPathRooted($EnvFile)) {
    $EnvFile
} else {
    Join-Path $root $EnvFile
}

$shellPath = if (Get-Command pwsh -ErrorAction SilentlyContinue) {
    (Get-Command pwsh).Source
} else {
    Join-Path $env:SystemRoot "System32\WindowsPowerShell\v1.0\powershell.exe"
}

function Import-DotEnv {
    param(
        [string]$Path
    )

    if (-not (Test-Path $Path)) {
        throw ".env file not found: $Path"
    }

    Get-Content $Path |
        ForEach-Object { $_.Trim() } |
        Where-Object { $_ -and -not $_.StartsWith("#") -and $_.Contains("=") } |
        ForEach-Object {
            $parts = $_.Split("=", 2)
            $name = $parts[0].Trim()
            $value = $parts[1].Trim()

            if (-not $name) {
                return
            }

            if (
                ($value.StartsWith('"') -and $value.EndsWith('"')) -or
                ($value.StartsWith("'") -and $value.EndsWith("'"))
            ) {
                $value = $value.Substring(1, $value.Length - 2)
            }

            Set-Item -Path "env:$name" -Value $value
        }
}

Import-DotEnv $envPath

$requiredEnvironmentVariables = @(
    "RIOT_API_KEY",
    "OPENAI_API_KEY"
)

foreach ($name in $requiredEnvironmentVariables) {
    if (-not [Environment]::GetEnvironmentVariable($name, "Process")) {
        throw "Required environment variable is missing or empty: $name"
    }
}

$services = @(
    @{
        Name = "search-service"
        Port = 18082
        MavenModule = "search-service"
    },
    @{
        Name = "ai-service"
        Port = 18083
        MavenModule = "ai-service"
    },
    @{
        Name = "match-service"
        Port = 18081
        MavenModule = "match-service"
    },
    @{
        Name = "summoner-service"
        Port = 18080
        MavenModule = "summoner-service"
    },
    @{
        Name = "api-gateway"
        Port = 18000
        MavenModule = "api-gateway"
    }
)

if ($NoGateway) {
    $services = $services | Where-Object { $_.Name -ne "api-gateway" }
}

$activePorts = (
    [System.Net.NetworkInformation.IPGlobalProperties]::GetIPGlobalProperties().GetActiveTcpListeners()
).Port

foreach ($service in $services) {
    if ($activePorts -contains $service.Port) {
        Write-Host ("[skip] {0} is already using port {1}." -f $service.Name, $service.Port)
        continue
    }

    $importEnvCommand = @"
Get-Content '$envPath' |
    ForEach-Object { `$_.Trim() } |
    Where-Object { `$_ -and -not `$_.StartsWith('#') -and `$_.Contains('=') } |
    ForEach-Object {
        `$parts = `$_.Split('=', 2)
        `$name = `$parts[0].Trim()
        `$value = `$parts[1].Trim()

        if (`$value.StartsWith('"') -and `$value.EndsWith('"')) {
            `$value = `$value.Substring(1, `$value.Length - 2)
        }
        if (`$value.StartsWith("'") -and `$value.EndsWith("'")) {
            `$value = `$value.Substring(1, `$value.Length - 2)
        }

        Set-Item -Path "env:`$name" -Value `$value
    }
"@
    $command = "Set-Location '$root'; $importEnvCommand; `$Host.UI.RawUI.WindowTitle = '$($service.Name)'; & '.\mvnw.cmd' -pl $($service.MavenModule) spring-boot:run"

    if ($DryRun) {
        Write-Host ("[dry-run] {0}: {1}" -f $service.Name, $command)
        continue
    }

    Start-Process -FilePath $shellPath `
        -ArgumentList @("-NoExit", "-Command", $command) `
        -WorkingDirectory $root | Out-Null

    Write-Host ("[start] {0} on port {1}" -f $service.Name, $service.Port)
}
