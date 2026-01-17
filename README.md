# 🛡️ LP System - Premium Loss Prevention Management

O **LP System** é uma solução robusta e moderna voltada para a gestão de Prevenção de Perdas e registros de erros operacionais. O sistema combina uma interface de usuário premium com automações inteligentes para busca e análise de produtos, integrando dados reais para auxiliar na tomada de decisão.

## 🚀 Funcionalidades Principais

- **📦 Gestão de Registros:** Controle total de itens pendentes, em análise, sistêmicos e DFLS.
- **🔍 Automação com Mercado Livre:** Scraper integrado que utiliza IA e Jsoup para buscar imagens e informações técnicas de produtos diretamente no Mercado Livre.
- **📊 Filtros Avançados:** Localização por andar/setor, nível de risco (crítico, alto, médio), faixa de valor e categorias.
- **📱 Interface Mobile-First:** Design responsivo e moderno com estética *glassmorphism* e suporte a micro-animações.
- **⚙️ Sincronização Inteligente:** Botão de sincronia para atualização de dados pendentes em tempo real.

## 🛠️ Tecnologias Utilizadas

### Backend
- **Java 17** & **Spring Boot 3.2.3**
- **Spring Data JPA** (Persistência de dados)
- **PostgreSQL / H2 Database** (Banco de dados relacional)
- **Jsoup** (Web Scraping para automação)
- **Lombok** (Produtividade no código)

### Frontend
- **HTML5 & CSS3** (Vanilla CSS com Design System Premium)
- **JavaScript** (Lógica de interface assíncrona)
- **Font Awesome 6** (Iconografia)
- **Google Fonts** (Tipografia Inter e Outfit)

## 📋 Pré-requisitos

Para rodar o projeto localmente, você precisará de:
- Java JDK 17 ou superior
- Maven 3.6+
- Banco de dados PostgreSQL (ou utilizar o H2 configurado por padrão)

## 🔧 Instalação e Execução

1. **Clone o repositório:**
   ```bash
   git clone [https://github.com/willincunha95-spec/sistema-prevencao-perdas.git](https://github.com/willincunha95-spec/sistema-prevencao-perdas.git)
   cd sistema-prevencao-perdas
2 Configure o banco de dados:
Verifique as credenciais no arquivo 
src/main/resources/application.properties
.
3 Compile e execute o projeto:
bash
mvn spring-boot:run
Acesse no navegador:
http://localhost:8080

4 🏗️ Estrutura do Projeto
src/main/java/com/projeto/automation: Módulo responsável pela integração com APIs externas e scraping.
src/main/java/com/projeto/controller: Endpoints da API REST.
src/main/java/com/projeto/specification: Lógica de filtros dinâmicos e critérios de busca.
src/main/resources/static: Interface frontend (CSS, JS, Imagens).
5 🤝 Contribuição
Faça um Fork do projeto
Crie uma Branch para sua Feature (git checkout -b feature/NovaFeature)
Faça o Commit de suas alterações (git commit -m 'Adicionando nova feature')
Envie para a Branch (git push origin feature/NovaFeature)
Abra um Pull Request

📄 Licença
Este projeto está sob a licença MIT. Veja o arquivo 
LICENSE
 para mais detalhes.

