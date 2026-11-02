# Plano de Implementação — Passo 2: Criação e Extração do `oficina-inventory-service`

> **Status:** ✅ Concluído e Validado (`mvnw clean test` 100% verde em ambos os repositórios).
>
> **Objetivo:** Construir o microsserviço independente [`oficina-inventory-service`](../README.md) seguindo o mesmo padrão arquitetural e tecnológico do [`oficina-billing-service`](../../oficina-billing-service/README.md) (Clean Architecture / Hexagonal, Java 25, Spring Boot 4.1.1, PostgreSQL + H2, K8s e CI/CD), e desconectar o domínio de Estoque do monólito [`oficina-app-api`](../../oficina-app-api/README.md), deixando a mensageria do **SAGA Pattern** para a última etapa.

---

## 1. Decisões Arquiteturais Confirmadas

1. **Escopo do Domínio (`1A`):** Migrado exclusivamente **`Insumos` (Catálogo de Peças e Controle de Estoque)** para o `oficina-inventory-service`, mantendo `Servico` + `StatusServico` juntos no `oficina-app-api` (`os-service`) devido ao cálculo de tempo médio de execução.
2. **Endpoints de Baixa e Estorno (`2A`):** Expostos endpoints em lote `POST /insumos/baixa` e `POST /insumos/estorno` recebendo uma lista de `{ insumoId, quantidade }`.
3. **Autenticação e Padronização (`3`):** Mesmo padrão do `oficina-billing-service` (Single-Module Maven + Clean Architecture, sem `spring-boot-starter-security` local, delegando validação JWT para o API Gateway).
4. **Remoção no Monólito (`4`):** Domínio `Insumos` completamente removido do `oficina-app-api` após a validação dos testes do novo microsserviço.

---

## 2. Arquitetura Interna Implementada (Clean Architecture)

```text
c:\source\oficina-inventory-service\
├── pom.xml
├── Dockerfile
├── docker-compose.yaml
├── Makefile
├── k8s/
│   ├── base/ (app-deployment.yaml, app-service.yaml, app-hpa.yaml, configmap.yaml, kustomization.yaml)
│   └── overlays/aws/ (kustomization.yaml, service-loadbalancer.yaml, configmap-rds.yaml, secret.example.yaml)
├── .github/workflows/
│   ├── maven.yml (CI - Build & Testes)
│   └── deploy.yml (CD - Build ECR & Deploy EKS)
└── src/
    ├── main/java/com/mecanica/oficina_inventory_service/
    │   ├── OficinaInventoryServiceApplication.java
    │   ├── domain/insumo/
    │   │   ├── Insumos.java
    │   │   └── OrigemNotificacaoEstoque.java
    │   ├── application/insumo/
    │   │   ├── gateway/ (InsumosGateway.java, NotificarEstoqueBaixoGateway.java)
    │   │   ├── input/ (ItemEstoqueInput.java)
    │   │   ├── output/ (AlertaEstoqueBaixo.java)
    │   │   └── usecase/
    │   │       ├── CadastrarInsumoUseCase.java
    │   │       ├── ConsultarInsumoUseCase.java
    │   │       ├── ListarInsumosUseCase.java
    │   │       ├── AlterarInsumoUseCase.java
    │   │       ├── AtivarInsumoUseCase.java
    │   │       ├── InativarInsumoUseCase.java
    │   │       ├── RegistrarCompraUseCase.java
    │   │       ├── BaixarEstoqueUseCase.java       <-- (Lógica resgatada do Passo 1)
    │   │       └── EstornarEstoqueUseCase.java     <-- (Compensação pronta para SAGA)
    │   └── adapters/
    │       ├── config/InsumoConfig.java
    │       ├── notification/LogNotificadorEstoqueBaixo.java
    │       ├── persistence/ (InsumosJpaEntity.java, InsumosJpaGateway.java, repository/InsumosSpringDataRepository.java)
    │       └── web/ (InsumosController.java, GlobalExceptionHandler.java, presenter/InsumosPresenter.java, dto/*)
    └── test/
        ├── java/com/mecanica/oficina_inventory_service/ (84 testes automatizados passando)
        └── resources/insumos/
```

---

## 3. Roteiro de Execução Passo a Passo (Checklist)

- [x] **Etapa 2.1 — Configuração Base do Projeto (`oficina-inventory-service`):**
  - Criado `pom.xml`, Maven Wrapper (`mvnw` / `mvnw.cmd`), `application.properties` (porta `8083`, PostgreSQL `inventory_db` + perfil `local` H2).
- [x] **Etapa 2.2 — Domínio e Casos de Uso (`domain` & `application`):**
  - Migrados `Insumos`, `OrigemNotificacaoEstoque`, `AlertaEstoqueBaixo`, Gateways e os 7 UseCases existentes.
  - Implementados `BaixarEstoqueUseCase` (com alerta de estoque crítico) e `EstornarEstoqueUseCase`.
- [x] **Etapa 2.3 — Adaptadores (`adapters`):**
  - Implementados `InsumosJpaEntity`, `InsumosSpringDataRepository`, `InsumosJpaGateway`, `LogNotificadorEstoqueBaixo`, `InsumoConfig`, `InsumosController`, DTOs e `GlobalExceptionHandler`.
- [x] **Etapa 2.4 — Testes Automatizados:**
  - Implementada suíte completa de testes unitários, de slice e de persistência (`InsumoTest`, 9 testes de UseCase incluindo `BaixarEstoqueUseCaseTest` e `EstornarEstoqueUseCaseTest`, `InsumosPresenterTest`, `InsumosJpaGatewayTest`, `InsumosControllerTest` — **84 testes passando**).
- [x] **Etapa 2.5 — Containerização, K8s, CI/CD e Documentação:**
  - Criados `Dockerfile`, `docker-compose.yaml`, `Makefile`, manifestos `k8s/`, workflows `.github/workflows/` e atualizado o `README.md`.
- [x] **Etapa 2.6 — Desconexão no `oficina-app-api`:**
  - Removidos pacotes, entidades, repositórios, controllers, seeds e testes de `Insumos` do `oficina-app-api`, mantendo `mvnw clean test` 100% verde nos dois repositórios.
