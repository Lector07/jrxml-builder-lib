Get-ChildItem -Path "C:\Users\Softres\Documents\my-projects\jrxml-builder-lib\src" -Filter "*.java" -Recurse | ForEach-Object {
    $file = $_
    Write-Host "Processing: $($file.Name)"

    $content = Get-Content $file.FullName -Raw -Encoding UTF8

    $content = $content -replace '/\*[\s\S]*?\*/', ''

    $content = $content -replace '(?m)^\s*//.*$', ''

    $content = $content -replace '(?m)^\s*$\r?\n', ''

    [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.Encoding]::UTF8)
}

Write-Host "Done!"

