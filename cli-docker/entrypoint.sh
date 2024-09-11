#!/bin/sh

# config file
if [ -n "$DECODABLE_ACCOUNT_NAME" ]; then
mkdir -p ~/.decodable
cat <<EOF > ~/.decodable/config
version: 1.0.0
active-profile: default

profiles:
  default:
    account: $DECODABLE_ACCOUNT_NAME
EOF
fi

# auth file
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
exec decodable "$@"
