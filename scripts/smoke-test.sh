#!/usr/bin/env sh
# Smoke test: build, sobe a stack com Docker Compose e valida auth, flags e evaluation.
# Uso: ./scripts/smoke-test.sh [--down]
# --down: executa docker-compose down ao final.

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

DOWN_AT_END=0
for arg in "$@"; do
  case "$arg" in
    --down) DOWN_AT_END=1 ;;
  esac
done

# Carrega .env se existir
if [ -f "$PROJECT_ROOT/.env" ]; then
  set -a
  . "$PROJECT_ROOT/.env"
  set +a
fi

if [ -z "${JWT_SECRET:-}" ]; then
  echo "[smoke-test] JWT_SECRET não definido; usando valor de teste."
  export JWT_SECRET="default-secret-for-smoke-test"
fi

# Garante que o docker-compose use o mesmo JWT: cria .env se não existir
# (evita 401 no flag-service por secret diferente entre auth e flag)
if [ ! -f "$PROJECT_ROOT/.env" ]; then
  echo "[smoke-test] Criando .env com JWT_SECRET para os containers."
  echo "JWT_SECRET=$JWT_SECRET" > "$PROJECT_ROOT/.env"
  echo "JWT_EXPIRATION=${JWT_EXPIRATION:-3600000}" >> "$PROJECT_ROOT/.env"
fi

echo "[smoke-test] Configuração: JWT_SECRET definido, sistema pronto para subir."
GATEWAY_URL="http://localhost:8080"
MAX_WAIT=90
SLEEP=3

echo "[smoke-test] Build Maven (clean package)..."
mvn clean package -DskipTests -q
echo "[smoke-test]   Build concluído."

echo "[smoke-test] Subindo stack (docker-compose up --build -d --force-recreate)..."
docker-compose up --build -d --force-recreate
echo "[smoke-test]   Containers em execução (recriados com .env atual)."

echo ""
echo "[smoke-test] Aguardando gateway em $GATEWAY_URL (timeout ${MAX_WAIT}s)..."
waited=0
while [ $waited -lt $MAX_WAIT ]; do
  # GET em /auth/login pode retornar 405 (Method Not Allowed) — qualquer resposta HTTP = gateway no ar
  _get_code=$(curl -s -o /dev/null -w "%{http_code}" "$GATEWAY_URL/auth/login" 2>/dev/null || echo "000")
  if [ "$_get_code" != "000" ]; then
    # Gateway respondeu; confirma que o auth aceita requisições (POST com body inválido = 400/422)
    _post_code=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$GATEWAY_URL/auth/login" \
      -H "Content-Type: application/json" -d '{}' 2>/dev/null || echo "000")
    if [ "$_post_code" = "400" ] || [ "$_post_code" = "401" ] || [ "$_post_code" = "422" ] || [ "$_post_code" = "200" ]; then
      echo "[smoke-test] Serviços prontos em ${waited}s."
      break
    fi
  fi
  printf "\r[smoke-test] Aguardando... %ds" "$waited"
  slept=$SLEEP
  [ $((waited + SLEEP)) -gt $MAX_WAIT ] && slept=$((MAX_WAIT - waited))
  sleep "$slept"
  waited=$((waited + slept))
done
printf "\r"
if [ $waited -ge $MAX_WAIT ]; then
  echo "[smoke-test] ERRO: timeout. Gateway não respondeu a tempo."
  echo "[smoke-test] Últimas linhas dos containers (para diagnóstico):"
  docker-compose logs --tail=15 2>/dev/null | sed 's/^/  /'
  exit 1
fi
echo ""

# Respostas em arquivos para debug e extração de token
REGISTER_RESP="/tmp/smoke-register-$$.json"
LOGIN_RESP="/tmp/smoke-login-$$.json"
EVAL_RESP="/tmp/smoke-eval-$$.txt"
cleanup() {
  rm -f "$REGISTER_RESP" "$LOGIN_RESP" "$EVAL_RESP"
}
trap cleanup EXIT

# Nomes únicos para evitar conflito com dados existentes
SMOKE_SUFFIX="smoke-$$-$(date +%s)"
SMOKE_EMAIL="${SMOKE_SUFFIX}@test.com"
SMOKE_PASSWORD="password123"
FLAG_NAME="test-flag-${SMOKE_SUFFIX}"

echo "[smoke-test] Executando testes da API (email=$SMOKE_EMAIL, flag=$FLAG_NAME)..."
echo ""
echo "[smoke-test] 1/6 POST /auth/register..."
_code=$(curl -s -w "%{http_code}" -o "$REGISTER_RESP" -X POST "$GATEWAY_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$SMOKE_EMAIL\",\"password\":\"$SMOKE_PASSWORD\"}")
# 201 = criado
if [ "$_code" != "201" ]; then
  echo "[smoke-test] Falha register: esperado 201, obtido $_code. Resposta:"
  cat "$REGISTER_RESP" 2>/dev/null || true
  exit 1
fi
echo "[smoke-test]   Sucesso (HTTP $_code)."

echo "[smoke-test] 2/6 POST /auth/login..."
_code=$(curl -s -w "%{http_code}" -o "$LOGIN_RESP" -X POST "$GATEWAY_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$SMOKE_EMAIL\",\"password\":\"$SMOKE_PASSWORD\"}")
if [ "$_code" != "200" ]; then
  echo "[smoke-test] Falha login: esperado 200, obtido $_code. Resposta:"
  cat "$LOGIN_RESP" 2>/dev/null || true
  exit 1
fi
echo "[smoke-test]   Sucesso (token obtido)."

if command -v jq >/dev/null 2>&1; then
  TOKEN=$(jq -r .token "$LOGIN_RESP")
else
  TOKEN=$(grep -o '"token":"[^"]*"' "$LOGIN_RESP" | cut -d'"' -f4)
fi
if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
  echo "[smoke-test] Não foi possível extrair token da resposta de login."
  cat "$LOGIN_RESP"
  exit 1
fi

echo "[smoke-test] 3/6 POST /flags (criar flag)..."
_code=$(curl -s -w "%{http_code}" -o /dev/null -X POST "$GATEWAY_URL/flags" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"name\":\"$FLAG_NAME\",\"enabled\":true,\"rolloutPercentage\":100}")
# 200 ou 201 = sucesso (flag-service não define status no controller)
if [ "$_code" != "200" ] && [ "$_code" != "201" ]; then
  echo "[smoke-test] Falha criar flag: esperado 200 ou 201, obtido $_code."
  exit 1
fi
echo "[smoke-test]   Sucesso (flag $FLAG_NAME criada)."

echo "[smoke-test] 4/6 GET /flags..."
_code=$(curl -s -w "%{http_code}" -o /dev/null -X GET "$GATEWAY_URL/flags" \
  -H "Authorization: Bearer $TOKEN")
if [ "$_code" != "200" ]; then
  echo "[smoke-test] Falha listar flags: esperado 200, obtido $_code."
  exit 1
fi
echo "[smoke-test]   Sucesso (lista obtida)."

echo "[smoke-test] 5/6 GET /flags/$FLAG_NAME..."
_code=$(curl -s -w "%{http_code}" -o /dev/null -X GET "$GATEWAY_URL/flags/$FLAG_NAME" \
  -H "Authorization: Bearer $TOKEN")
if [ "$_code" != "200" ]; then
  echo "[smoke-test] Falha obter flag: esperado 200, obtido $_code."
  exit 1
fi
echo "[smoke-test]   Sucesso (flag obtida)."

echo "[smoke-test] 6/6 GET /evaluate..."
_code=$(curl -s -w "%{http_code}" -o "$EVAL_RESP" -X GET "$GATEWAY_URL/evaluate?flag=$FLAG_NAME&userId=user1" \
  -H "Authorization: Bearer $TOKEN")
_eval_body=$(cat "$EVAL_RESP" 2>/dev/null)
if [ "$_code" != "200" ]; then
  echo "[smoke-test] Falha evaluate: esperado 200, obtido $_code. Resposta: $_eval_body"
  exit 1
fi
if [ "$_eval_body" != "true" ]; then
  echo "[smoke-test] Evaluate esperado 'true' (rollout 100%%), obtido: $_eval_body"
  exit 1
fi
echo "[smoke-test]   Sucesso (evaluate = true)."

echo ""
echo "[smoke-test] OK — Todos os 6 passos passaram (auth, flags, evaluate)."

echo "[smoke-test] Limpeza: removendo flag $FLAG_NAME..."
_code=$(curl -s -w "%{http_code}" -o /dev/null -X DELETE "$GATEWAY_URL/flags/$FLAG_NAME" \
  -H "Authorization: Bearer $TOKEN")
if [ "$_code" = "204" ] || [ "$_code" = "200" ]; then
  echo "[smoke-test]   Flag removida."
else
  echo "[smoke-test]   Aviso: DELETE retornou $_code (flag pode já ter sido removida)."
fi

if [ "$DOWN_AT_END" = "1" ]; then
  echo "[smoke-test] docker-compose down..."
  docker-compose down
  echo "[smoke-test]   Containers encerrados."
fi

exit 0
