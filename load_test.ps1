$baseUrl = "http://localhost:8080/api"

Write-Host "Starting Load Simulation Script..."

# 1. Create User (if needed) to start analyses
$userBody = @{
    username = "sim_user_$(Get-Random)"
    password = "password"
    fullName = "Simulation User"
    role = "EMPLOYEE"
} | ConvertTo-Json

$userId = 1

try {
    Write-Host "Attempting to create a simulation user..."
    $user = Invoke-RestMethod -Uri "$baseUrl/users" -Method Post -Body $userBody -ContentType "application/json" -ErrorAction Stop
    if ($user -and $user.id) {
        $userId = $user.id
        Write-Host "Created Simulation User with ID: $userId"
    }
} catch {
    Write-Host "Could not create new user (maybe service is down or user dup). Defaulting to ID 1."
    Write-Host "Error: $_"
}

Write-Host "Using User ID: $userId for analyses."
Write-Host "Simulating 200 records per minute. Press Ctrl+C to stop."

# 2. Main Loop
while ($true) {
    $startTime = Get-Date
    Write-Host "Starting batch of 200 requests at $($startTime.ToString('HH:mm:ss'))"

    $count = 0
    for ($i = 1; $i -le 200; $i++) {
        $skuRandom = Get-Random -Minimum 1000 -Maximum 9999
        $body = @{
            title = "Analysis Simulation $i"
            description = "Auto generated analysis for load testing req $i"
            responsible = @{ id = $userId }
            status = "PENDING"
            sku = "SKU-$skuRandom"
            location = "LOC-$(Get-Random)"
        } | ConvertTo-Json -Depth 10

        try {
            Invoke-RestMethod -Uri "$baseUrl/analyses" -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop | Out-Null
            $count++
        } catch {
            Write-Host "Error sending request $i: $_"
        }
        
        # Optional: slight delay to not completely choke if the machine is slow, 
        # but for 200/min we can just burst or sleep small.
        # 200 reqs / 60 sec = 3.3 req/sec.
        # No sleep needed for burst, just wait at end of minute.
    }

    $endTime = Get-Date
    $duration = ($endTime - $startTime).TotalSeconds
    Write-Host "Batch finished: $count requests sent in $([math]::Round($duration, 2)) seconds."

    if ($duration -lt 60) {
        $sleepTime = 60 - $duration
        Write-Host "Sleeping for $([math]::Round($sleepTime, 2)) seconds to match 1 minute interval..."
        Start-Sleep -Seconds $sleepTime
    }
}
