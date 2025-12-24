@echo off
echo ==========================================
echo      LIBERANDO PORTA 8080
echo ==========================================
echo.
echo Procurando processo na porta 8080...

for /f "tokens=5" %%a in ('netstat -aon ^| find ":8080" ^| find "LISTENING"') do (
    echo Matando processo PID: %%a
    taskkill /F /PID %%a
    echo.
    echo Sucesso! A porta 8080 foi liberada.
    echo Agora voce pode rodar o projeto novamente.
    goto :FIM
)

echo Nenhum processo encontrado na porta 8080.
echo O caminho esta livre!

:FIM
echo.
pause
