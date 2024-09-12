#!/bin/sh

# write the config file if env var set
if [ -n "$DECODABLE_ACCOUNT_NAME" ]; then
mkdir -p ~/.decodable
cat <<EOF > ~/.decodable/config
version: 1.0.0
active-profile: default

profiles:
  default:
    account: $DECODABLE_ACCOUNT_NAME
EOF
else
  echo "\n⚠️ DECODABLE_ACCOUNT_NAME not set.\n"
  exit 1
fi

# write the auth file if env var set
if [ -n "$DECODABLE_REFRESH_TOKEN" ]; then
mkdir -p ~/.decodable
cat <<EOF > ~/.decodable/auth
version: 1.0.0
tokens:
    default:
        refresh_token: $DECODABLE_REFRESH_TOKEN
EOF
fi

# Execute the main process
if [ "$1" = "print-refresh-token" ]; then
  decodable login
  echo "Refresh token: $(decodable token refresh)"
else
  if [ -n "$DECODABLE_REFRESH_TOKEN" ]; then
    exec decodable "$@"
  else
    echo "\n⚠️ DECODABLE_REFRESH_TOKEN not set.\n👉 Use print-refresh-token to generate a refresh token.\n"
    exit 1
  fi
fi
