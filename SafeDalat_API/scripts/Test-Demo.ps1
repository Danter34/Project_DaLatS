param([string]$BaseUrl = 'http://localhost:5084')
$ErrorActionPreference = 'Stop'
function Assert($Condition, $Message) { if (!$Condition) { throw $Message } }
function Login($Email) {
    $body = @{ email = $Email; password = 'DalatS@Demo2026' } | ConvertTo-Json
    Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/Auth/login" -ContentType 'application/json' -Body $body
}
$admin = Login 'admin@demo.dalats.test'
Assert ($admin.user.role -eq 'Admin') 'Admin login failed'
$headers = @{ Authorization = 'Bearer ' + $admin.token }
$all = Invoke-RestMethod "$BaseUrl/api/Incidents" -Headers $headers
$demo = @($all | Where-Object { @($_.images | Where-Object {$_.filePath -like '/uploads/incidents/demo/*'}).Count -gt 0 })
Assert ($demo.Count -eq 15) 'Expected 15 demo incidents'
Assert (@($demo.categoryName | Sort-Object -Unique).Count -eq 5) 'Expected five categories'
foreach ($incident in $demo) {
    Assert ($incident.latitude -ge 11.92 -and $incident.latitude -le 11.98 -and $incident.longitude -ge 108.41 -and $incident.longitude -le 108.48) 'GPS outside Da Lat demo area'
}
foreach ($path in @($demo.images.filePath | Sort-Object -Unique)) {
    $response = Invoke-WebRequest -UseBasicParsing -Uri ($BaseUrl + $path)
    Assert ($response.StatusCode -eq 200 -and $response.Headers['Content-Type'] -like 'image/jpeg*') ('Missing image: ' + $path)
}
$map = Invoke-RestMethod "$BaseUrl/api/Incidents/map"
foreach ($incident in @($demo | Where-Object isPublic)) {
    Assert ($incident.incidentId -in $map.incidentId) 'Public incident missing from map'
}
for ($i = 1; $i -le 5; $i++) {
    $staff = Login "staff$i@demo.dalats.test"
    Assert ($staff.user.role -eq 'Staff') 'Staff login failed'
    $assigned = Invoke-RestMethod "$BaseUrl/api/Incidents" -Headers @{ Authorization = 'Bearer ' + $staff.token }
    Assert ($assigned.Count -gt 0) 'Staff has no assigned incidents'
    Assert (@($assigned.assignedDepartmentId | Sort-Object -Unique).Count -eq 1) 'Staff list spans multiple departments'
}
$citizen = Login 'user1@demo.dalats.test'
Assert ($citizen.user.role -eq 'User') 'Citizen login failed'
$questions = Invoke-RestMethod "$BaseUrl/api/QA" -Headers $headers
Assert ($questions.Count -ge 8) 'Missing demo questions'
$summary = Invoke-RestMethod "$BaseUrl/api/Dashboard/summary" -Headers $headers
Assert ($summary.tongSuCo -ge 15) 'Dashboard count incorrect'
Write-Host 'PASS: Admin, 5 staff, citizen login; 15 incidents; 5 categories; Da Lat GPS; all images; public map; Q&A; dashboard.'
$summary | ConvertTo-Json
