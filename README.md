# Serviço de Gerenciamento de Bibliotecas

## Autores
- **Guilherme Marcello Casagranda**  
- **Milena Risso Zanotelli**

---

## Descrição do Projeto
Este projeto é um **serviço de gerenciamento de bibliotecas** que opera com base em **arquitetura de microsserviços**, permitindo o gerenciamento eficiente de:  
- Usuários,  
- Acervos,  
- Empréstimos,  
- Multas.  

O sistema é dividido em três APIs principais:  
- `api-usuario`  
- `api-biblioteca`  
- `api-emprestimo`  

Cada API é responsável por funcionalidades específicas, detalhadas abaixo.

---

## Microsserviços e Funcionalidades

### 1. **Acesso Usuário**  
Permite que os usuários da biblioteca realizem:  
- Cadastro no sistema;  
- Consulta ao acervo de livros (geral ou por critérios específicos como nome ou ISBN);  
- Visualização do histórico de empréstimos e empréstimos ativos;  
- Consulta de multas pendentes;  
- Reserva de livros.  

#### Endpoints  
- **POST** `/cadastro` - Cadastrar um novo usuário.  
- **GET** `/livros` - Consultar todos os livros no acervo.  
- **GET** `/livros/nome` - Consultar livros por nome.  
- **GET** `/livros/isbn` - Consultar livros por ISBN.  
- **GET** `/historico` - Visualizar histórico de empréstimos do usuário (autenticado).  
- **GET** `/emprestimos/ativos` - Verificar empréstimos ativos (autenticado).  
- **GET** `/multa/ativa` - Consultar multas pendentes (autenticado).  
- **POST** `/reserva` - Realizar reserva de livro (requisição chama o endpoint **POST** `/reserva` do microsserviço Empréstimo).  

---

### 2. **Acesso Biblioteca**  
Responsável pela gestão do acervo de livros e controle operacional da biblioteca, incluindo gerenciamento de empréstimos, devoluções e multas.  

#### Endpoints  
- **POST** `/livros` - Cadastrar novo livro no acervo (autenticado).  
- **PATCH** `/livros/isbn` - Atualizar informações de um livro por ISBN (autenticado).  
- **DELETE** `/livros` - Remover livro do acervo (autenticado).  
- **POST** `/emprestimo` - Registrar empréstimo de livro (chama **POST** `/emprestimo` do microsserviço Empréstimo).  
- **POST** `/devolucao` - Registrar devolução de livro (chama **POST** `/devolucao` do microsserviço Empréstimo).  
- **POST** `/multa/pagamento` - Registrar pagamento de multa (chama **PATCH** `/zerar-multa` do microsserviço Empréstimo).  
- **GET** `/emprestimos` - Listar todos os empréstimos (autenticado).  
- **GET** `/emprestimos/isbn` - Consultar empréstimo específico por ISBN (autenticado).  
- **GET** `/emprestimos/atrasados` - Listar empréstimos atrasados (autenticado).  

---

### 3. **Empréstimo**  
Gerencia a lógica de reservas, empréstimos e devoluções, além de atualizar automaticamente informações relacionadas ao usuário e à disponibilidade de livros.  

Este microsserviço:  
- Atualiza diariamente empréstimos atrasados e multas;  
- Lida com a efetivação de reservas, ajustando automaticamente a disponibilidade do livro reservado.  

#### Endpoints  
- **POST** `/reserva` - Registrar reserva de livro.  
- **POST** `/emprestimo` - Registrar empréstimo de livro.  
- **POST** `/devolucao` - Registrar devolução de livro.  
- **PATCH** `/zerar-multa` - Zerar multas de um usuário após pagamento.  

---

## Tecnologias Utilizadas
- **Linguagem:** Java  
- **Banco de Dados:** MongoDB  
- **Arquitetura:** Microsserviços com integração entre APIs REST  

---
## Arquitetura
![Diagrama representando a arquitetura]()
---
## Instruções de Uso

### 1. **Instalação e Configuração**
1. Clone o repositório:  
   ```bash
   git clone https://github.com/gcasagranda/MicrosservicosBiblioteca
2. Configure as variáveis de ambiente se necessário
3. Execute cada microsserviço individualmente
