FROM amd64/ubuntu:latest

# Set environment variables
ENV DECO_CLI_VERSION=1.20.0
ENV DECO_CLI_URL=https://releases.decodable.co/decodable-cli/linux/amd64/decodable-cli-linux-amd64-${DECO_CLI_VERSION}.tar.gz

# Install necessary packages
RUN apt-get update && \
    apt-get install -y curl tar && \
    rm -rf /var/lib/apt/lists/*

# Download and unpack the Decodable CLI
RUN curl -L $DECO_CLI_URL -o /tmp/decodable-cli.tar.gz && \
    tar -xzf /tmp/decodable-cli.tar.gz -C /tmp && \
    mv /tmp/decodable-cli-linux-amd64-${DECO_CLI_VERSION}/bin/decodable /usr/local/bin && \
    rm -r /tmp/decodable-cli.tar.gz /tmp/decodable-cli-linux-amd64-${DECO_CLI_VERSION}

# Copy the entrypoint script into the image
COPY entrypoint.sh /usr/local/bin/entrypoint.sh

# Make the entrypoint script executable
RUN chmod +x /usr/local/bin/entrypoint.sh

# Verify installation
RUN decodable --version

# Set entrypoint
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]