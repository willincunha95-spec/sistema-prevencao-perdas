@echo off
echo ==========================================
echo      Cleaning Database Cache & Temp Files
echo ==========================================

echo [1/3] Deleting JVM crash logs...
del /q hs_err_pid*.log 2>nul
del /q replay_pid*.log 2>nul

echo [2/3] Remove Maven 'target' directory (this may take a moment)...
:: Fast delete technique using rmdir
if exist target rmdir /s /q target

echo [3/3] Restarting Redis Cache container...
docker-compose restart inventory-cache

echo ==========================================
echo                 CLEANUP COMPLETE
echo ==========================================
pause
