# Skrypt do usuwania komentarzy z plików Java

$javaFiles = Get-ChildItem -Path "src" -Filter "*.java" -Recurse

foreach ($file in $javaFiles) {
    Write-Host "Processing: $($file.FullName)"

    $content = Get-Content $file.FullName -Raw

    # Usuń komentarze wieloliniowe /* ... */
    $content = $content -replace '/\*[\s\S]*?\*/', ''

    # Usuń komentarze jednoliniowe //
    $content = $content -replace '//[^\r\n]*', ''

    # Usuń puste linie (opcjonalnie)
    $content = ($content -split "`r`n" | Where-Object { $_.Trim() -ne '' }) -join "`r`n"

    # Zapisz z powrotem
    Set-Content -Path $file.FullName -Value $content -NoNewline
}

Write-Host "Completed!"

