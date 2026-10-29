$token = (Invoke-RestMethod -Uri 'http://localhost:38080/realms/bomerp/protocol/openid-connect/token' -Method Post -Body @{
    client_id = 'bomerp-frontend'
    username = 'admin'
    password = 'Bomerp2026!'
    grant_type = 'password'
}).access_token

$headers = @{
    Authorization = "Bearer $token"
}

$response = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/rendiciones' -Headers $headers
$response.content | Format-Table id, codigo, responsable, fecha, montoTotal, estado
