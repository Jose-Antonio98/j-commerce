# Plano de implementação — J-Commerce

## Objetivo

Construir uma API REST de e-commerce com Java e Spring Boot, priorizando regras de negócio, persistência consistente, 
segurança, integração de pagamento e testes. O plano parte do código que já existe e organiza o trabalho em etapas 
dependentes; não pressupõe a criação de um frontend.

## Estado atual e análise dos módulos `users` e `auth`

As etapas 0 e 1 estão concluídas para o escopo atual. O módulo `users` possui `User`, `Role`, `UserRepository`, cadastro validado, normalização de email, senha com hash e testes unitários. O identificador é um `UUID` gerado pelo Hibernate ao persistir a entidade.

A implementação básica da etapa 2 foi concluída e commitada: login, emissão e validação de JWT com expiração, carregamento de usuário ativo e autorização por role na rota administrativa. Testes HTTP automatizados cobrem acesso sem token (401), perfil `CUSTOMER` (403), perfil `ADMIN` (acesso permitido), token inválido e token expirado.

| Arquivo | Estado / ação necessária |
| --- | --- |
| `src/main/java/dev/jose/jcommerce/users/domain/User.java` | Entidade com UUID, email normalizado, hash da senha, role, estado e timestamps. Validações e preenchimento de `createdAt`/`updatedAt` nos callbacks estão implementados. |
| `src/main/java/dev/jose/jcommerce/users/domain/enums/Role.java` | Roles `CUSTOMER` e `ADMIN`; o cadastro público cria `CUSTOMER` por padrão. A autorização JWT converte a role para a autoridade Spring correspondente. |
| `src/main/java/dev/jose/jcommerce/users/repository/UserRepository.java` | Repositório de `User` com `UUID` como identificador, consulta por email e verificação de email duplicado. |
| `src/main/resources/db/migration/V1__create_users.sql` | Migration inicial do domínio de usuários. Mudanças de schema devem continuar sendo feitas em migrations incrementais, sem alterar migrations já aplicadas. |
| `src/main/java/dev/jose/jcommerce/auth/` e `config/SecurityConfig.java` | Login, JWT HS256 com expiração configurada, carregamento de conta ativa e proteção de `/auth/admin/**` com `ROLE_ADMIN` implementados. Os testes HTTP automatizados exercitam os cenários principais de autorização e validade do token. |

**Responsabilidade do módulo:** `users` é dono dos dados e regras da conta; `auth` autentica e emite credenciais usando esses dados. O Spring Security deve consumir um serviço adaptador de usuários, sem colocar lógica de token dentro da entidade `User`.

## Organização sugerida

Manter os pacotes por funcionalidade, como o projeto já começou a fazer (`users`, `catalog`, `cart`, etc.). Dentro de cada funcionalidade, separar responsabilidades conforme necessário:

```text
<feature>/
  domain/       entidades, enums e regras do domínio
  repository/   persistência e consultas
  service/      casos de uso e transações
  web/          controllers e DTOs de entrada/saída
```

Não é necessário criar pastas vazias antecipadamente. Criar cada parte junto com a funcionalidade e evitar expor entidades JPA diretamente pela API.

## Cronograma por etapas

### Etapa 0 — Base do projeto e persistência

**Objetivo:** deixar a aplicação reproduzível e com uma base de testes confiável.

- [x] Revisar `pom.xml`, configuração Spring Boot/Java e dependências existentes; adicionar somente o necessário para cada etapa (Security, suporte JWT, testes de integração).
- [x] Configurar PostgreSQL e variáveis de ambiente sem credenciais versionadas.
- [x] Manter Flyway como fonte do esquema; usar migrations incrementais (`V2`, `V3`...), sem editar migrations já aplicadas.
- [x] Definir convenções: UUID, horário UTC, nomes de tabelas/colunas, erros HTTP e formato de dinheiro em centavos.
- [x] Criar tratamento de erros da API e validação de DTOs com respostas consistentes.
- [x] Configurar testes unitários e de integração; adicionar Testcontainers quando os testes precisarem de PostgreSQL real.

**Concluída:** a aplicação sobe com PostgreSQL limpo, aplica migrations e os testes básicos rodam sem depender de estado local.

### Etapa 1 — Usuários (`users`)

**Objetivo:** corrigir e fechar o domínio que será usado pela autenticação.

**Status: concluída para o escopo atual (cadastro por email, sem campo de nome).**

#### Model

- [x] `User`: UUID, email normalizado e único, `passwordHash`, `role`, `active`, `createdAt` e `updatedAt`. O modelo atual não inclui nome.
- [x] Nunca armazenar senha em texto puro; o serviço produz o hash com `PasswordEncoder`.
- [x] Preencher datas de criação e atualização nos callbacks da entidade.
- [x] `Role` inclui `CUSTOMER` e `ADMIN`; o registro público cria apenas `CUSTOMER`.

#### Repository

- [x] `findByEmail(...)`: localizar conta durante autenticação.
- [x] `existsByEmail(...)`: detectar duplicidade antes da gravação; a constraint `UNIQUE` continua sendo a proteção definitiva contra concorrência.
- [x] `findById(...)`: consultar conta para operações administrativas.

#### Service e API

- [x] Serviço de usuários para cadastro e consulta necessária à autenticação.
- [x] `POST /auth/register`: registrar cliente com validação.
- [x] Não aceitar `role` nem `passwordHash` como campos graváveis pelo cliente.
- [x] Não expor hash da senha na resposta do cadastro.

#### Testes

- [x] Testes de cadastro válido, email duplicado, email inválido e campos obrigatórios.
- [x] Teste de que usuário novo recebe `CUSTOMER` e senha é armazenada somente como hash.

**Concluída:** entidade, repositório, cadastro e testes estão alinhados; o cadastro não permite elevação de privilégio.

### Etapa 2 — Autenticação e autorização (`auth`)

**Objetivo:** proteger as rotas e estabelecer identidade autenticada para os demais módulos.

**Status: implementação básica concluída e commitada; os principais cenários HTTP de autorização e validade do token têm testes automatizados.**

#### Model / componentes

- [x] Não duplicar usuário ou role no módulo `auth`; carregar `User` pelo serviço/repositório de usuários.
- [x] Implementar `UserDetailsService` e conversão de roles para authorities.
- [x] Configurar `PasswordEncoder` e validação de conta ativa.
- [x] Emitir access tokens JWT com expiração configurada (15 minutos); refresh tokens e revogação ficam fora do escopo atual.

#### Repository

- [x] Reutilizar consulta de usuário por email.
- [x] Usar apenas access tokens curtos no escopo inicial; não há refresh-token persistido.

#### API / segurança

- [x] `POST /auth/login`: validar credenciais e devolver access token; resposta sem senha ou hash.
- [x] Deixar cadastro e login públicos e exigir autenticação nas demais rotas protegidas.
- [x] Exigir `ROLE_ADMIN` na consulta administrativa `GET /auth/admin/users/{id}`.
- [ ] Nas futuras operações de carrinho e pedidos, resolver o usuário pelo principal autenticado, nunca por um `userId` confiado no body.

#### Testes

- [x] Testes unitários de credenciais válidas/inválidas, usuário inexistente, conta inativa e carregamento de role.
- [x] Teste manual da rota administrativa com perfis `CUSTOMER` e `ADMIN`.
- [x] Automatizar testes HTTP para token ausente (401), `CUSTOMER` (403), `ADMIN` (acesso permitido), token inválido e expirado.

**Marco básico concluído:** a rota administrativa exige `ADMIN`, o cadastro público não permite alterar a própria role e os testes HTTP verificam acesso anônimo, autorização por role e tokens inválidos ou expirados.

### Etapa 3 — Catálogo (`catalog`)

**Objetivo:** publicar e administrar categorias e produtos, com busca paginada.

#### Model

- `Category`: UUID, nome, slug único, ativa, timestamps.
- `Product`: UUID, nome, slug único, descrição, preço em centavos, quantidade disponível, ativo, imagem URL opcional, categoria e timestamps.
- Preço e estoque não podem ser negativos. Definir se produto inativo pode continuar associado a pedidos históricos.
- A categoria é uma relação muitos-para-um do produto; carregar relações de forma explícita para evitar consultas N+1.

#### Repository

- `CategoryService`: procurar por slug e verificar unicidade do slug.
- `ProductRepository`: procurar por slug; listar por nome parcial, categoria e estado ativo com paginação/ordenação.
- Usar `Specification` ou mecanismo equivalente para combinar filtros opcionais sem duplicar consultas.
- Considerar `@Version` para concorrência otimista no produto; a reserva de estoque terá sua regra transacional própria na etapa 6.

#### API / service

- Leitura pública: `GET /categories`, `GET /products`, `GET /products/{id}`.
- Administração: criar/editar/ativar/desativar categorias e produtos somente para `ADMIN`.
- Filtros: texto, categoria, ativo e faixa de preço; paginação com limites máximos.
- DTOs de resposta não devem serializar entidades ou relações JPA diretamente.

#### Testes

- Filtros combinados, paginação, slug duplicado, produto/categoria inexistente e restrição de acesso administrativo.

**Concluído quando:** catálogo público só apresenta produtos disponíveis para venda conforme a regra definida e alterações administrativas estão protegidas.

### Etapa 4 — Endereços (`addresses`)

**Objetivo:** guardar endereços do cliente e permitir selecionar o endereço de entrega.

#### Model

- `Address`: UUID, usuário, CEP, logradouro, número, complemento opcional, bairro, cidade, estado e timestamps.
- Definir campos obrigatórios e limites de tamanho; endereço pertence a um único usuário.

#### Repository

- Listar endereços por usuário autenticado e localizar endereço por ID e proprietário.
- Consultas de escrita/leitura sempre verificam que o endereço pertence ao usuário atual.

#### API / service

- Criar, listar, editar e excluir endereços próprios.
- Administradores não precisam de acesso geral a endereços no MVP.

#### Testes

- Isolamento entre usuários, validação dos campos e operações sobre endereço inexistente.

**Concluído quando:** um cliente não consegue consultar ou alterar o endereço de outro cliente.

### Etapa 5 — Carrinho (`cart`)

**Objetivo:** manter a seleção de produtos do cliente antes da criação do pedido.

#### Model

- `Cart`: UUID, usuário único, timestamps.
- `CartItem`: UUID, carrinho, produto, quantidade.
- Constraint única para `(cart_id, product_id)`; adicionar um produto já presente atualiza sua quantidade.
- O carrinho não é fonte autoritativa de preço: preço e disponibilidade são revalidados no checkout.

#### Repository

- Buscar carrinho pelo usuário autenticado e carregar itens/produtos necessários para a resposta.
- Buscar item pelo carrinho e produto; listar/remover itens do carrinho.

#### API / service

- `GET /cart`, `POST /cart/items`, `PUT /cart/items/{itemId}`, `DELETE /cart/items/{itemId}`.
- Validar quantidade positiva, produto ativo e regra de estoque definida.
- Nunca aceitar proprietário do carrinho vindo do cliente; obter usuário do principal autenticado.

#### Testes

- Adicionar, atualizar e remover item, produto inativo/inexistente, quantidade inválida e isolamento do carrinho por usuário.

**Concluído quando:** carrinho persiste por usuário e cada alteração respeita ownership e validações.

### Etapa 6 — Pedidos e decisão de reserva (`orders`, `inventory`)

**Objetivo:** converter o carrinho em pedido imutável para histórico e definir formalmente o ciclo de estoque.

#### Decisão obrigatória antes do checkout

- [ ] Definir quando o estoque é reservado (ao iniciar checkout ou antes de criar a sessão de pagamento).
- [ ] Definir duração da reserva e comportamento para sessão expirada, pagamento falho, cancelamento e webhook atrasado.
- [ ] Definir a transação que confirma o pagamento e consome a reserva exatamente uma vez.
- [ ] Escolher mecanismo concorrente: atualização condicional/controle otimista ou lock pessimista. O mecanismo escolhido deve ser coberto por teste concorrente com estoque unitário.

**Direção recomendada para o MVP:** criar uma reserva com validade ao iniciar checkout; impedir que a quantidade reservada seja oferecida a outro pedido; confirmar a reserva em transação quando o pagamento for confirmado; liberar reserva expirada ou falha por processo idempotente. Registrar essa decisão antes de integrar Stripe.

#### Model

- `Order`: UUID, usuário, status, total em centavos, moeda, endereço de entrega copiado como snapshot, `createdAt`, `updatedAt`.
- `OrderItem`: pedido, produto referenciado para contexto, nome/SKU snapshot, quantidade e preço unitário em centavos.
- O total é calculado no servidor a partir dos itens e componentes definidos; nunca confiar em preço ou total enviado pelo cliente.
- Estados iniciais sugeridos: `PENDING_PAYMENT`, `PAID`, `CANCELLED`, `FULFILLING`, `SHIPPED`, `DELIVERED`. Documentar transições válidas e não permitir regressões arbitrárias.
- `StockReservation` (se adotada como tabela): produto, pedido, quantidade, estado e validade. Definir unicidade/constraints para que uma reserva não seja confirmada ou liberada duas vezes.

#### Repository

- Listar pedidos do usuário autenticado com paginação e ordenação.
- Buscar pedido por ID e usuário; operação administrativa separada para consulta global.
- Localizar reservas ativas/expiradas para confirmar ou liberar.
- Atualizar disponibilidade de forma atômica, verificando saldo suficiente na própria operação.

#### API / service

- `POST /orders`: criar pedido a partir do carrinho no servidor, revalidar preço/estado/estoque e copiar snapshots.
- `GET /orders`, `GET /orders/{id}`: permitir acesso somente ao proprietário, salvo rota administrativa.
- Fazer gravação do pedido, itens e reserva na mesma transação local. Não manter transação de banco aberta durante chamada externa ao Stripe.

#### Testes

- Pedido válido, carrinho vazio, produto desativado, alteração de preço, estoque insuficiente, snapshot preservado e duas compras concorrentes pela última unidade.

**Concluído quando:** o pedido guarda os valores e endereço do momento da compra e o estoque não pode ser vendido acima do disponível.

### Etapa 7 — Pagamentos e Stripe (`payments`)

**Objetivo:** permitir mais de uma tentativa de pagamento para o mesmo pedido sem duplicar pedido nem confirmar duas vezes.

#### Model

- `PaymentAttempt`: UUID, pedido, provedor, identificador da sessão Stripe único, `paymentIntentId` opcional e único quando presente, estado, timestamps e informação segura de falha.
- Relação recomendada: `Order 1:N PaymentAttempt`. Um pedido pode ter tentativas falhas/expiradas e no máximo uma confirmação efetiva.
- Não armazenar dados de cartão.

#### Repository

- Buscar tentativa por session ID e pedido.
- Consultar tentativas do pedido por data/estado.
- Persistir IDs externos com constraints únicas para proteger reprocessamentos concorrentes.

#### Service / integração

- Criar checkout somente para pedido que ainda possa ser pago e cuja reserva esteja válida.
- Nova tentativa reutiliza o mesmo pedido e cria novo `PaymentAttempt`; não cria novo pedido.
- Persistir tentativa e coordenar falhas de integração para evitar sessões sem referência local ou tentativas impossíveis de reconciliar.
- A criação de sessão Stripe é chamada externa; não executá-la dentro de uma transação de banco longa.

#### API

- Endpoint autenticado para iniciar/reiniciar pagamento de um pedido próprio.
- Não aceitar preço do cliente nem permitir pagar pedido de outro usuário.

#### Testes

- Tentativa inicial, nova tentativa após falha/expiração, pedido já pago/cancelado e falha na comunicação com o provedor.

**Concluído quando:** falhas de pagamento não apagam o pedido, e um pedido não pode ser confirmado duas vezes.

### Etapa 8 — Webhooks e ciclo de estoque

**Objetivo:** processar eventos do provedor de forma autenticada, idempotente e transacional.

#### Model / Repository

- `ProcessedWebhookEvent` ou estrutura equivalente com ID do evento Stripe único, tipo, data de recebimento e resultado de processamento.
- Buscar evento/tentativa por identificador externo e atualizar estado da tentativa/pedido.

#### Webhook / service

- `POST /webhooks/stripe`: validar assinatura sobre o corpo bruto usando o segredo configurado fora do repositório.
- Registrar evento com unicidade antes de produzir efeitos; reenvio de evento já processado não deve repetir efeitos.
- Tratar eventos duplicados, fora de ordem e eventos que não pertencem ao fluxo suportado.
- Confirmar pagamento e reserva/estoque em uma transação local consistente; não executar baixa de estoque duas vezes.
- Não confiar apenas em redirecionamento do navegador para marcar pedido como pago.

#### Testes

- Assinatura inválida, evento válido, evento duplicado, dois eventos de sucesso, falha transacional, evento desconhecido e evento atrasado após reserva expirar.

**Concluído quando:** o webhook pode ser repetido sem duplicar pagamento, baixa de estoque ou notificação.

### Etapa 9 — Administração, envio e email

**Objetivo:** completar operações básicas de backoffice após o fluxo de compra estar estável.

#### Administração

- Consultar pedidos com filtros e paginação.
- Alterar estado de fulfillment por operações explícitas (`marcar como enviado`, por exemplo), respeitando transições válidas.
- Registrar código de rastreio e data de envio se estiver no escopo.
- Proteger toda operação com `ROLE_ADMIN` e auditar mudanças de estado.

#### Email

- Enviar confirmação de pagamento e atualização de envio por serviço desacoplado.
- Evitar envio duplicado após reprocessamento de webhook; usar registro/outbox de notificação se necessário.
- Falha de email não deve desfazer pagamento confirmado.

#### Testes

- Permissão, transição inválida, reenvio idempotente e falha do provedor de email.

### Etapa 10 — Qualidade, documentação e entrega

**Objetivo:** tornar o repositório fácil de executar e avaliar.

- [ ] Aumentar cobertura nos fluxos críticos; priorizar testes de integração com PostgreSQL para constraints e concorrência.
- [ ] Documentar OpenAPI: autenticação, exemplos, validações, erros e rotas administrativas.
- [ ] Adicionar coleção Bruno ou Postman com variáveis e fluxo demonstrativo.
- [ ] Documentar execução local, variáveis de ambiente, migrations, testes e configuração de webhook Stripe local.
- [ ] Garantir que `docker compose` suba os serviços necessários sem segredos fixos no repositório.
- [ ] Revisar logs para não registrar senhas, tokens, segredos ou payloads sensíveis.

**Concluído quando:** uma pessoa consegue clonar, configurar variáveis, iniciar a aplicação, rodar testes e seguir o fluxo de compra documentado.

## Dependências entre etapas

```text
0 Base
  └─ 1 Users
       └─ 2 Auth
            ├─ 3 Catalog ──────────────┐
            ├─ 4 Addresses ────────────┤
            └─ 5 Cart ─────────────────┴─ 6 Orders + decisão de reserva
                                             └─ 7 Payments
                                                  └─ 8 Webhooks
                                                       └─ 9 Administração/email
                                                            └─ 10 Entrega
```

Catálogo e endereços podem avançar em paralelo depois da autenticação básica. A integração de pagamentos depende da decisão documentada de reserva de estoque.

## Fora do MVP

Cupons, avaliações, wishlist, múltiplos vendedores, frete com transportadoras, devoluções/reembolsos automatizados e refresh-token rotation podem ser evoluções futuras. Não incluir antes do fluxo de compra, pagamento, estoque e testes estarem concluídos.
