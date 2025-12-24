# LP System - Prevention & Loss Management

Sistema completo de gestão de prevenção de perdas (Loss Prevention), controle de inventário e análise de divergências. Desenvolvido com **Java Spring Boot**, **PostgreSQL**, **Redis** e interface moderna em **Vanilla JavaScript**.

## 🚀 Tecnologias

*   **Backend**: Java 17, Spring Boot 3, Spring Data JPA.
*   **Banco de Dados**: PostgreSQL 15 (Persistência), Redis 7 (Cache/Filas).
*   **Frontend**: HTML5, CSS3 Moderno, JavaScript (ES6+).
*   **Infraestrutura**: Docker & Docker Compose.

## 📋 Funcionalidades Principais

*   **Dashboard**: Monitoramento em tempo real com badges de status.
*   **Módulos de Gestão**:
    *   **Pendentes**: Fila de itens aguardando análise.
    *   **Análise**: Processo de investigação de divergências (com workflow de DFL/Found).
    *   **DFLs (Damaged/Defective)**: Gestão de itens avariados.
    *   **Founds**: Registro de itens encontrados (com valor e localização).
    *   **Sistêmico**: Análise de erros sistêmicos/integrados.
*   **Filtros Avançados**: Pesquisa por localização (Andar, Rua, Posição), Categoria, Risco e Valores.
*   **Alta Performance**: Cache com Redis e arquitetura preparada para alto volume (Simulação de carga incluída).

## 🛠️ Como Rodar o Projeto

### Pré-requisitos
*   Java 17+ instalado.
*   Docker & Docker Compose instalados.
*   Maven (opcional, se usar o wrapper).

### Passo a Passo

1.  **Subir o Ambiente (Banco e Cache)**:
    Execute o script na raiz do projeto:
    ```bash
    ./REINICIAR_AMBIENTE.bat
    ```
    Ou manualmente:
    ```bash
    docker-compose up -d
    ```

2.  **Executar a Aplicação**:
    No VS Code, abra `PrevencaoPerdasApplication.java` e clique em "Run".
    Ou via terminal:
    ```bash
    ./mvnw spring-boot:run
    ```

3.  **Acessar**:
    Abra seu navegador em: `http://localhost:8080/index.html`

## ⚙️ Utilitários

*   **Simulação de Carga**: Script `load_test.ps1` para gerar 200 registros/min para testes de stress.
*   **Correção de Porta**: Script `MATAR_PORTA_8080.bat` para liberar a porta caso o servidor trave.
