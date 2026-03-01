Stop-Process -Name "emulator" -Force -ErrorAction SilentlyContinue
Stop-Process -Name "qemu-system-x86_64" -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 3

$lockDir = "$env:USERPROFILE\.android\avd\Pixel_9.avd"
Remove-Item "$lockDir\hardware-qemu.ini.lock" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item "$lockDir\multiinstance.lock" -Force -ErrorAction SilentlyContinue
Write-Output "Locks cleared"

$emulator = "C:\Users\u.biswas\AppData\Local\Android\Sdk\emulator\emulator.exe"
Start-Process $emulator -ArgumentList "-avd Pixel_9 -wipe-data -no-snapshot-load" -WindowStyle Normal
Write-Output "Emulator starting with wipe-data..."
