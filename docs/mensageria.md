# 📨 Mensageria (SNS + SQS)

Infraestrutura de eventos da Saga coreografada (ADR 005, em
[`oficina-app-api`](https://github.com/GrupoPosTech-FIAP/oficina-app-api/blob/main/docs/adr/005-saga-coreografada.md)).
É a mesma implementação do `oficina-app-api`; o guia completo (diagrama, como publicar e
consumir) está em
[`oficina-app-api/docs/mensageria.md`](https://github.com/GrupoPosTech-FIAP/oficina-app-api/blob/main/docs/mensageria.md).

## Neste serviço

| | |
|---|---|
| Publica em | `oficina-inventory-events` |
| Consome | `oficina-inventory-queue` (DLQ: `oficina-inventory-dlq`) |
| Eventos que publica (ADR 005) | EstoqueBaixado, EstoqueInsuficiente, EstoqueEstornado |
| Eventos que recebe | OrcamentoAprovado, PagamentoRecusado, PagamentoExpirado (billing), ExecucaoFalhou (os; execution quando o serviço existir) |

### Passos da Saga

| Gatilho | Caso de uso | Resultado |
|---|---|---|
| `OrcamentoAprovado` | `BaixarEstoqueOrdemServicoUseCase` | Confere o saldo de todos os insumos antes de mexer no estoque. Faltando algum → `EstoqueInsuficiente` (nada é baixado). Senão baixa tudo, grava `baixas_ordem_servico` e publica `EstoqueBaixado` (também com lista vazia) |
| `PagamentoRecusado`, `PagamentoExpirado`, `ExecucaoFalhou` | `EstornarEstoqueOrdemServicoUseCase` | Devolve exatamente o que foi baixado para a OS → `EstoqueEstornado`. Sem baixa ou já estornada, não faz nada |

O registro em `baixas_ordem_servico` existe porque os eventos de falha não trazem a lista de
insumos; ele também impede baixar duas vezes a mesma OS.

## Peças

| Classe | Papel |
|---|---|
| `application/evento/gateway/PublicadorEventosGateway` | Porta de saída usada pelos casos de uso |
| `adapters/messaging/OutboxPublicadorEventos` | Grava o evento em `outbox_eventos` na transação do chamador |
| `adapters/messaging/OutboxRelay` | Publica os pendentes no SNS (atributo `eventType`) |
| `adapters/messaging/SqsConsumidorEventos` | Recebe da fila; só apaga depois de processar |
| `adapters/messaging/ProcessadorEventos` | Deduplica por `eventId` (`eventos_processados`) e chama o manipulador |
| `adapters/messaging/ManipuladorEvento` | Interface dos manipuladores; cada um declara os `eventTypes` que trata |

## Configuração

| Variável | Padrão | Descrição |
|---|---|---|
| `MENSAGERIA_HABILITADA` | `false` | Liga relay e consumidor. Desligada, eventos ficam só na outbox |
| `MENSAGERIA_TOPICO_ARN` | — | `oficina-inventory-events` |
| `MENSAGERIA_FILA_URL` | — | `oficina-inventory-queue` |
| `MENSAGERIA_ENDPOINT` | — | Endpoint alternativo (LocalStack) |
| `AWS_REGION` | `us-east-1` | Região |

## Local e AWS

- `docker-compose up --build` sobe o LocalStack (`inventory-localstack`, porta `LOCALSTACK_PORT`,
  padrão 4566) com a topologia completa criada por
  [`localstack/init-mensageria.sh`](../localstack/init-mensageria.sh).
- No deploy, o `deploy.yml` resolve ARN e URL pelo nome e injeta no `configmap-rds.yaml`.
  O `oficina-infra-cluster` (que cria tópicos e filas em `messaging.tf`) precisa ter sido
  aplicado antes.
