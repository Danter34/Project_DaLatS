$ErrorActionPreference = 'Stop'
$destination = Join-Path $PSScriptRoot '../SafeDalat_API/wwwroot/uploads/incidents/demo'
New-Item -ItemType Directory -Force -Path $destination | Out-Null
$images = [ordered]@{
    pothole = 'File:Pothole on local Road in County Monaghan.jpg'
    rubbish = 'File:Illegal dumping of rubbish - geograph.org.uk - 8039864.jpg'
    drain = 'File:Second blocked drain - Albert Place, Tottenham N17.jpg'
    tree = 'File:Fallen Tree in Dormer Place, Leamington Spa (1).jpg'
    light = 'File:Zernikeplaats damaged street light, Ommoord, Rotterdam (2022) 01.jpg'
}
$credits = @()
foreach ($item in $images.GetEnumerator()) {
    $api = 'https://commons.wikimedia.org/w/api.php?action=query&prop=imageinfo&iiprop=url%7Cextmetadata&iiurlwidth=960&format=json&titles=' + [uri]::EscapeDataString($item.Value)
    $result = Invoke-RestMethod -Uri $api
    $info = @($result.query.pages.PSObject.Properties.Value)[0].imageinfo[0]
    $file = Join-Path $destination ($item.Key + '.jpg')
    $download = $info.thumburl.Split('?')[0]
    if (!(Test-Path -LiteralPath $file)) {
        Invoke-WebRequest -UseBasicParsing -Uri $download -OutFile ($file + '.download') -UserAgent 'DalatS-Demo/1.0 (educational demo image download)'
        Move-Item -LiteralPath ($file + '.download') -Destination $file
    }
    $credits += [ordered]@{
        file = $item.Key + '.jpg'
        title = $item.Value
        source = $info.descriptionurl
        original = $info.url
        author = $info.extmetadata.Artist.value
        license = $info.extmetadata.LicenseShortName.value
        licenseUrl = $info.extmetadata.LicenseUrl.value
        note = 'Wikimedia thumbnail, no content edits. Not photographed in Da Lat. Demo incidents and locations are fictional.'
    }
    Write-Host ($item.Key + ': ' + (Get-Item -LiteralPath $file).Length + ' bytes')
}
$credits | ConvertTo-Json -Depth 5 | Set-Content -Encoding UTF8 (Join-Path $destination 'credits.json')
