FROM eclipse-temurin:25-jdk

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# Make user-local installs (e.g., zoxide) usable
ENV PATH="/root/.local/bin:${PATH}"

# Install essential tools and requested applications
RUN apt-get update && apt-get install -y \
    git \
    curl \
    wget \
    unzip \
    p7zip-full \
    jq \
    poppler-utils \
    fd-find \
    ripgrep \
    fzf \
    ca-certificates \
    fontconfig \
    && rm -rf /var/lib/apt/lists/*

# Install Maven
ARG MAVEN_VERSION=3.9.6
RUN curl -fsSL "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
    | tar -xzC /opt \
    && ln -s "/opt/apache-maven-${MAVEN_VERSION}" /opt/maven \
    && ln -s /opt/maven/bin/mvn /usr/local/bin/mvn

ENV MAVEN_HOME=/opt/maven
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

# Create symlink for fd (fd-find is the package name in Debian/Ubuntu)
RUN ln -sf "$(command -v fdfind)" /usr/local/bin/fd

# Install Neovim
RUN apt-get update && apt-get install -y neovim \
    && rm -rf /var/lib/apt/lists/*

# Install Zoxide (installs into /root/.local/bin)
RUN curl -sS https://raw.githubusercontent.com/ajeetdsouza/zoxide/main/install.sh | bash

# Install Yazi (only on amd64; skip on arm64 to avoid failures)
RUN set -eux; \
    arch="$(dpkg --print-architecture)"; \
    if [ "$arch" = "amd64" ]; then \
      curl -fsSL https://github.com/sxyazi/yazi/releases/latest/download/yazi-x86_64-unknown-linux-gnu.zip -o /tmp/yazi.zip; \
      unzip /tmp/yazi.zip -d /tmp/yazi; \
      mv /tmp/yazi/yazi-x86_64-unknown-linux-gnu/yazi /usr/local/bin/yazi; \
      mv /tmp/yazi/yazi-x86_64-unknown-linux-gnu/ya /usr/local/bin/ya; \
      chmod +x /usr/local/bin/yazi /usr/local/bin/ya; \
      rm -rf /tmp/yazi.zip /tmp/yazi; \
    else \
      echo "Skipping yazi on $arch"; \
    fi

# Install resvg (only on amd64; skip on arm64 to avoid 404s)
RUN set -eux; \
    arch="$(dpkg --print-architecture)"; \
    if [ "$arch" = "amd64" ]; then \
      curl -fsSL https://github.com/RazrFalcon/resvg/releases/latest/download/resvg-linux-x86_64.tar.gz -o /tmp/resvg.tar.gz; \
      tar -xzf /tmp/resvg.tar.gz -C /tmp; \
      mv /tmp/resvg /usr/local/bin/resvg; \
      chmod +x /usr/local/bin/resvg; \
      rm -rf /tmp/resvg.tar.gz; \
    else \
      echo "Skipping resvg on $arch"; \
    fi

# Install Nerd Font (Symbols Only)
RUN mkdir -p /usr/share/fonts/truetype/nerd-fonts \
    && curl -fsSL https://github.com/ryanoasis/nerd-fonts/releases/latest/download/NerdFontsSymbolsOnly.zip -o /tmp/nerd-font.zip \
    && unzip /tmp/nerd-font.zip -d /usr/share/fonts/truetype/nerd-fonts \
    && fc-cache -fv \
    && rm -rf /tmp/nerd-font.zip

# Install Node.js 24 LTS and Angular CLI
ARG NODE_VERSION=24
RUN curl -fsSL "https://deb.nodesource.com/setup_${NODE_VERSION}.x" | bash - \
    && apt-get install -y nodejs \
    && npm install -g @angular/cli@21 \
    && npm install -g npm@latest \
    && rm -rf /var/lib/apt/lists/*

# Verify Node.js and Angular CLI installations
RUN node --version && npm --version && ng version

WORKDIR /workspace

# Keep container running
CMD ["sleep", "infinity"]
