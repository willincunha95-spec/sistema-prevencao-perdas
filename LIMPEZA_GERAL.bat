@echo off
echo ==========================================
echo      LIMPEZA RAPIDA (MODO TURBO)
echo ==========================================
echo.
echo [1/3] Limpando cache (Rapido)...
echo Removendo apenas o lixo obvio (containers parados e imagens sem nome).
docker system prune -f

echo.
echo [2/3] Otimizando Banco (Modo leve)...
docker exec -i inventory-db psql -U postgres -d prevencao_perdas -c "VACUUM;"

echo.
echo [3/3] Concluido!
echo ==========================================
echo Limpeza finalizada.
echo ==========================================
pause
