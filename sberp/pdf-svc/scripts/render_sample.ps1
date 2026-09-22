# 로컬 pdf-svc에 samples/minimal.html 을 POST 해 test.pdf 로 저장
# 사용: .\scripts\render_sample.ps1 [-PdfSvcUrl http://127.0.0.1:8002]

param(
    [string]$PdfSvcUrl = "http://127.0.0.1:8002",
    [string]$OutFile = "test.pdf"
)

$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$htmlPath = Join-Path $root "samples\minimal.html"
$html = [System.IO.File]::ReadAllText($htmlPath, [System.Text.Encoding]::UTF8)

$payloadObj = [PSCustomObject]@{
    html = $html
    print_background = $true
    prefer_css_page_size = $true
}
$json = $payloadObj | ConvertTo-Json -Compress -Depth 3
$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($json)

$uri = "$PdfSvcUrl/v1/render/pdf"
Invoke-WebRequest -Uri $uri -Method Post -Body $bodyBytes -ContentType "application/json; charset=utf-8" -OutFile $OutFile
Write-Host "Saved: $OutFile ($((Get-Item $OutFile).Length) bytes)"
