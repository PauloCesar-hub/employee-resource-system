# 🚀 ERS — Employee Resource System

<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Arquitetura-Camadas-blue?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/API-HTTP%20Nativa-green?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Persist%C3%AAncia-CSV-orange?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Interface-Web-lightgrey?style=for-the-badge"/>
</p>

## 📌 Visão Geral

O **ERS (Employee Resource System)** é um sistema em Java para gestão de colaboradores, recursos internos e alocações corporativas.

Nesta versão, o projeto foi evoluído para um nível muito mais completo, com:

- arquitetura em camadas
- autenticação simples por perfil
- IDs automáticos
- persistência em CSV
- logs do sistema
- relatórios e filtros
- API HTTP nativa
- interface web simples

## ✅ Melhorias implementadas

### Nível 1
- Arquitetura separada em `model`, `repository`, `service`, `api`, `util`
- IDs automáticos para colaboradores, recursos e alocações
- Menu de console mais completo
- Busca por nome

### Nível 2
- Persistência em arquivos CSV na pasta `data/`
- Logs em `data/logs.txt`
- Histórico de eventos em colaboradores e recursos

### Nível 3
- Login com perfis:
  - `admin / 123`
  - `user / 123`
- Relatórios:
  - custo total por colaborador
  - recursos mais caros
  - colaboradores sem recursos
- Filtros:
  - colaboradores ativos
  - recursos disponíveis

### Nível 4
- API HTTP local com endpoints:
  - `GET /api/colaboradores`
  - `POST /api/colaboradores`
  - `GET /api/recursos`
  - `POST /api/recursos`
  - `GET /api/alocacoes`
  - `POST /api/alocar`
- Interface web em `http://localhost:8080`

## 🗂 Estrutura do projeto

```text
src/br/com/ers/
├── api/
├── model/
├── persistence/
├── repository/
├── service/
├── util/
└── Main.java
```

## ▶️ Como executar

### Console

```bash
javac -d out $(find src -name "*.java")
java -cp out br.com.ers.Main
```

### API + Web

1. Execute o projeto normalmente.
2. No menu, escolha a opção **17 - Iniciar API + interface web**.
3. Abra no navegador:

```text
http://localhost:8080
```

## 🔐 Login padrão

- **Administrador:** `admin / 123`
- **Usuário comum:** `user / 123`

## 🧪 Dados persistidos

Os dados são salvos automaticamente na pasta `data/`:

- `colaboradores.csv`
- `recursos.csv`
- `alocacoes.csv`
- `logs.txt`

## 👨‍💻 Autores

| Nome | RM |
|------|----|
| Paulo Cesar de Govea Junior | RM566034 |
| Guilherme Vilela Perez | RM564422 |
| Gustavo Panham Dourado | RM563904 |
| Christian Schunck de Almeida | RM563850 |
| Thomas Jeferson Santana Wang | RM565104 |
