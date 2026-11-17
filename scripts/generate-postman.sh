#!/usr/bin/env bash
# Genereert de Postman-collectie uit de OpenAPI-specificatie (docs/api/openapi.json).
#
# Stappen om de API-beschrijving te vernieuwen na een wijziging in de controllers:
#   1. cd ClimateChecker && ./mvnw test -Dtest=OpenApiSpecTest -Dopenapi.update=true   (ververst docs/api/openapi.json)
#   2. ./scripts/generate-postman.sh                                                     (ververst de collectie)
#
# Vereist Node.js (npx). De converter staat vastgepind zodat de uitvoer reproduceerbaar is.
set -euo pipefail

CONVERTER="openapi-to-postmanv2@5.0.0"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SPEC="$ROOT/docs/api/openapi.json"
OUT="$ROOT/postman/ClimateChecker.postman_collection.json"

[ -f "$SPEC" ] || { echo "Ontbrekend: $SPEC (zie stap 1 in dit script)" >&2; exit 1; }

TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT

npx --yes "$CONVERTER" -s "$SPEC" -o "$TMP" -p \
  -O folderStrategy=Tags,requestParametersResolution=Schema,exampleParametersResolution=Schema

# Maak de uitvoer reproduceerbaar en klein: de converter plakt willekeurige id's op en genereert
# voorbeeldantwoorden met een willekeurig aantal array-elementen, die laten we weg (de spec is de
# bron voor de antwoorden). Voeg ook de variabele voor het JWT toe die de beveiligde requests
# gebruiken (zie het bearerAuth-scheme in de spec).
python3 - "$TMP" "$OUT" <<'PY'
import json, sys

src, dest = sys.argv[1], sys.argv[2]
with open(src) as f:
    collection = json.load(f)

def strip_ids(node):
    if isinstance(node, dict):
        for key in ("_postman_id", "id"):
            if isinstance(node.get(key), str) and len(node[key]) == 36:
                del node[key]
        for value in node.values():
            strip_ids(value)
    elif isinstance(node, list):
        for value in node:
            strip_ids(value)

def strip_examples(node):
    if isinstance(node, dict):
        node.pop("response", None)
        for value in node.values():
            strip_examples(value)
    elif isinstance(node, list):
        for value in node:
            strip_examples(value)

def normalize_payload(node):
    # De converter kiest willekeurige enumwaarden, wachtwoorden en aantallen array-elementen;
    # zet ze vast zodat opnieuw genereren geen ruis in de diff geeft.
    if isinstance(node, dict):
        for key, value in node.items():
            if key == "password":
                node[key] = "<string>"
            elif key == "role":
                node[key] = "USER"
            else:
                node[key] = normalize_payload(value)
    elif isinstance(node, list):
        return [normalize_payload(value) for value in node[:1]]
    return node

def fix_bodies(node):
    if isinstance(node, dict):
        body = node.get("body")
        if isinstance(body, dict) and isinstance(body.get("raw"), str):
            try:
                payload = json.loads(body["raw"])
            except ValueError:
                payload = None
            if payload is not None:
                body["raw"] = json.dumps(normalize_payload(payload), indent=2, ensure_ascii=False)
        if node.get("key") == "role" and "value" in node:
            node["value"] = "USER"
        for value in node.values():
            fix_bodies(value)
    elif isinstance(node, list):
        for value in node:
            fix_bodies(value)

strip_ids(collection)
strip_examples(collection)
fix_bodies(collection)

variables = collection.setdefault("variable", [])
if not any(v.get("key") == "bearerToken" for v in variables):
    variables.append({"key": "bearerToken", "value": "", "type": "string",
                      "description": "JWT uit POST /api/authentication/login of /verify"})

with open(dest, "w") as f:
    json.dump(collection, f, indent=2, ensure_ascii=False)
    f.write("\n")
PY

echo "Geschreven: $OUT"
