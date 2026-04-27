# Orders API 📦
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen)
![Java](https://img.shields.io/badge/Java-17-orange)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Redis](https://img.shields.io/badge/Redis-7-red)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)
![Coverage](https://img.shields.io/badge/Coverage-100%25-brightgreen)
[![NPM](https://img.shields.io/npm/l/react)](https://github.com/DarieldonMedeiros/X-Men/blob/main/LICENSE)


Uma API REST de alto desempenho desenvolvida para o gerenciamento completo de pedidos, projetada com foco em escalabilidade, segurança e manutenibilidade. Este projeto utiliza o ecossistema moderno do **Spring Boot 4** e implementa padrões de projeto avançados para resolver desafios comuns de sistemas de e-commerce e logística.

---

## 📖 Índice

- [Visão Geral](#-visão-geral)
- [Stack Utilizada](#-stack-utilizada)
- [Arquitetura e Design Patterns](#-arquitetura-e-design-patterns)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Modelagem de Dados](#-modelagem-de-dados)
- [Segurança e Autenticação](#-segurança-e-autenticação)
- [Como Executar](#-como-executar)
- [Endpoints e Payloads](#-endpoints-e-payloads)
- [Variáveis de Ambiente](#-variáveis-de-ambiente)
- [Testes e Qualidade](#-testes-e-qualidade)
- [Funcionalidades de Auditoria e Sanitização](#-funcionalidades-de-auditoria-e-sanitização)
- [Licença](#-licença)

## 🌟 Visão Geral

A **Orders API** permite o gerenciamento do ciclo de vida de pedidos, desde a criação com cálculos de descontos dinâmicos até o acompanhamento de status e remoção lógica. A arquitetura foi pensada para ser resiliente, utilizando **Redis** para cache, **PostgreSQL com JSONB** para flexibilidade de dados e **Docker** para portabilidade total.

---

## 🛠 Stack Utilizada

### Backend
- **Java 17** (LTS) & **Spring Boot 4.0.5-SNAPSHOT**
- **Spring Security** & **JWT (io.jsonwebtoken)**: Autenticação e autorização stateless.
- **Spring Data JPA** & **Hibernate 7**: Persistência de dados e mapeamento objeto-relacional.
- **Spring Data Redis**: Abstração de cache distribuído.
- **MapStruct 1.7**: Mapeamento performático entre Entidades e DTOs.
- **Lombok**: Redução de código boilerplate.
- **SpringDoc OpenAPI 3**: Documentação interativa e padronizada.

### Banco de Dados & Cache
- **PostgreSQL 16**: Banco de dados relacional com suporte a tipos binários JSON (JSONB).
- **Redis 7-Alpine**: Armazenamento em cache na memória para baixa latência.
- **Hypersistence Utils**: Integração avançada para suporte a tipos JSONB no Hibernate.

### Infraestrutura & Ferramentas
- **Docker & Docker Compose**: Containerização de toda a infraestrutura.
- **pgAdmin 4**: Interface web para gerenciamento do PostgreSQL.
- **Redis Insight 3.4**: interface visual para monitoramento do Redis.
- **JUnit 5** & **Mockito**: Testes unitários e de integração.
- **JaCoCo**: Ferramenta de análise de cobertura de código.

---

## 🏗 Arquitetura e Design Patterns

O projeto segue os princípios de **Clean Code** e **SOLID**, organizando as responsabilidades de forma clara:

### Padrões Aplicados:
- **Strategy Pattern:** Implementado na camada `strategy` para o cálculo de descontos. O sistema injeta dinamicamente a lógica correta (`VipDiscount` ou `NormalDiscount`) com base no `ClientType` do pedido, permitindo expansão sem alteração do código core.
- **Soft Delete:** Pedidos não são removidos fisicamente. A presença do campo `deleted_at` gerencia a exclusão lógica, garantindo que dados históricos sejam preservados e permitindo auditorias.
- **JSONB Storage:** Em vez de tabelas relacionais complexas para itens de pedido, utilizamos uma coluna `jsonb` no PostgreSQL. Isso permite armazenar listas de objetos (`OrderItem`) de forma eficiente, mantendo a capacidade de consulta e reduzindo o número de joins.
- **Cache-Aside Pattern:** Consultas por ID são cacheadas no Redis. A estratégia de invalidação (`@CacheEvict`) garante que o cache seja limpo sempre que um pedido for atualizado ou removido.
- **DTO Pattern:** Total separação entre as entidades de banco de dados e os objetos de transferência de dados, garantindo que a API não exponha detalhes internos da persistência.

---

## 📁 Estrutura do Projeto

```text
src/main/java/com/darieldon/pedidos/
├── config/              # Configurações (Security, JwtFilter, Redis, Swagger)
├── controller/          # Controladores REST (Entrypoint da API)
├── dto/                 # Objetos de transferência de dados
│   ├── request/         # Payloads de entrada
│   └── response/        # Estruturas de saída
├── exception/           # Tratamento global de erros e exceções customizadas
├── mapper/              # Interfaces MapStruct para mapeamento de objetos
├── model/               # Entidades JPA e Enums de domínio
├── repository/          # Interfaces de acesso ao banco de dados (Spring Data)
├── service/             # Regras de negócio e lógica de autenticação
└── strategy/            # Implementações do padrão Strategy para descontos
```

---

## 🗄️ Modelagem de Dados

### Tabela `orders` (Pedidos)
| Coluna          | Tipo            | Descrição                                              |
|:----------------|:----------------|:-------------------------------------------------------|
| `id`            | `BIGINT`        | Chave primária (Identity)                              |
| `customer_name` | `VARCHAR(150)`  | Nome do cliente (sanitizado com trim)                  |
| `items`         | `JSONB`         | Lista de `OrderItem` (productId, quantity, unitPrice)  |
| `status`        | `VARCHAR`       | Enum: `PENDING`, `PROCESSING`, `COMPLETED`, `CANCELED` |
| `total_amount`  | `NUMERIC(10,2)` | Valor total com desconto aplicado                      |
| `user_id`       | `BIGINT`        | Referência ao usuário que criou o pedido               |
| `created_at`    | `TIMESTAMP`     | Data de criação (Auditoria automática)                 |
| `updated_at`    | `TIMESTAMP`     | Data da última atualização                             |
| `deleted_at`    | `TIMESTAMP`     | Marcação para Soft Delete                              |

### Tabela `tb_users` (Usuários)
| Coluna     | Tipo      | Descrição                              |
|:-----------|:----------|:---------------------------------------|
| `id`       | `BIGINT`  | Chave primária                         |
| `email`    | `VARCHAR` | Identificador único (E-mail)           |
| `password` | `VARCHAR` | Senha criptografada (BCrypt)           |
| `role`     | `VARCHAR` | Papel do usuário (Padrão: `ROLE_USER`) |

---

## 🔐 Segurança e Autenticação

A API utiliza **Spring Security** com autenticação **Stateless** via **JWT (JSON Web Token)**.

1. **Criptografia:** senhas são armazenadas utilizando `BCryptPasswordEncoder`.
2. **Tokens:** O token gerado contém o `email` (subject) e o `userId` como claims customizadas.
3. **Filtro:** O `JwtFilter` intercepta cada requisição, valida a assinatura do token e injeta a identidade do usuário no contexto de segurança do Spring.
4. **Validade:** Tokens expiram conforme configurado (Padrão: 24 horas).

---

## 🚀 Como Executar

### Pré-requisitos
- **Docker** e **Docker Compose** instalados.

### 1. Subir a Infraestrutura e Aplicação
O projeto está totalmente configurado para rodar em containers. Execute:

```bash
docker-compose up -d --build
```

Isso iniciará os seguintes serviços:
- **API (Spring Boot):** [http://localhost:8080](http://localhost:8080)
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **pgAdmin 4 (Gerenciamento DB):** [http://localhost:5050](http://localhost:5050)
- **Redis Insight (Monitoramento Cache):** [http://localhost:5540](http://localhost:5540)

### 2. Acesso ao Banco de Dados (pgAdmin)
- **Email:** `admin@admin.com`
- **senha:** `admin`
- **Configuração do servidor:** Host: `db`, Port: `5432`, DB: `orders_db`, User: `postgres`, Pass: `postgres`.

---

## 📋 Endpoints e Payloads

Todos os endpoints da API estão listados abaixo com seus respectivos exemplos de uso.

### 🔐 Autenticação (`/auth`)

#### 1. Registrar novo usuário
`POST /auth/register`
```json
{
  "email": "dev@darieldon.com",
  "password": "senha_forte_aqui"
}
```

#### 2. Realizar login
`POST /auth/login`
```json
{
  "email": "dev@darieldon.com",
  "password": "senha_forte_aqui"
}
```
*Retorno: Token JWT para ser usado no header `Authorization: Bearer <token>`.*

---

### 📦 Pedidos (`/orders`)
*Todos os endpoints abaixo exigem o Header de Autorização.*

#### 3. Criar Pedido
`POST /orders`
```json
{
  "customerName": "Darie Don",
  "clientType": "VIP",
  "items": [
    {
      "productId": 101,
      "quantity": 2,
      "unitPrice": 45.50
    },
    {
      "productId": 202,
      "quantity": 1,
      "unitPrice": 120.00
    }
  ]
}
```
*Obs: O `clientType` pode ser `NORMAL` ou `VIP` (aplica 10% de desconto).*

#### 4. Listar Pedidos (Paginado)
`GET /orders?status=PENDING&page=0&size=10&sort=createdAt,desc`
- **Filtros Opcionais:** `status` (`PENDING`, `PROCESSING`, `COMPLETED`, `CANCELED`).
- **Paginação:** `page` (página), `size` (tamanho), `sort` (campo, direção).

#### 5. Buscar Pedido por ID
`GET /orders/{id}`
*Exemplo: `GET /orders/1`*

#### 6. Atualizar Status do Pedido
`PUT /orders/{id}/status`
```json
{
  "status": "COMPLETED"
}
```
*Status permitidos: `PENDING`, `PROCESSING`, `COMPLETED`, `CANCELED`.*

#### 7. Deletar Pedido (Soft Delete)
`DELETE /orders/{id}`
*O pedido não é removido do banco, apenas marcado como deletado.*

---

## 🔥 Variáveis de Ambiente

Configuradas no `docker-compose.yml` e `application.yml`:

| Variável             | Descrição                  | Padrão                                |
|:---------------------|:---------------------------|:--------------------------------------|
| `DB_URL`             | URL de conexão PostgreSQL  | `jdbc:postgresql://db:5432/orders_db` |
| `REDIS_HOST`         | Host do Redis              | `redis`                               |
| `APP_JWT_SECRET`     | Chave de assinatura do JWT | *Segredo de 256 bits*                 |
| `APP_JWT_EXPIRATION` | Tempo de expiração (ms)    | `86400000`                            |

---

## 🧪 Testes e Qualidade

O projeto preza pela confiabilidade do código.

```bash
# Executar todos os testes
./mvnw clean test

# Gerar relatório de cobertura Jacoco
./mvnw test jacoco:report
```
*O relatório estará em `target/site/jacoco/index.html`.*

---

## 💡 Funcionalidades de Auditoria e Sanitização
- **Auditoria JPA:** Uso de `@PrePersist` e `@PreUpdate` para gerenciar datas de criação e modificação sem intervenção manual.
- **Sanitização:** O nome do cliente passa por um `.trim()` automático antes de ser persistido, evitando espaços em branco desnecessários.
- **Tratamento de Erros:** Exceções como `ResourceNotFoundException` e erros de validação retornam um JSON padronizado com código HTTP, mensagem e timestamp.

---

## 📄 Licença

Este projeto está licenciado sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

---
**Desenvolvido por [Darieldon de Brito Medeiros](https://github.com/DarieldonMedeiros)**
