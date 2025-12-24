@echo off
echo ==========================================
echo      REINICIANDO AMBIENTE DOCKER
echo ==========================================
echo.
echo [1/3] Parando containers antigos...
docker-compose down

echo.
echo [2/3] Subindo banco de dados e Redis...
docker-compose up -d

echo.
echo [3/3] Verificando status...
docker ps

echo.
echo ==========================================
echo           PRONTO!
echo ==========================================
echo.
echo AGORA SIM:
echo 1. Volte para o VS Code.
echo 2. Se aparecer um pop-up "Synchronize configuration", clique em "Always" ou "Yes".
echo 3. Clique no botao "Run" (Play) novamente.
echo.
pause
