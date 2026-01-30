FROM eclipse-temurin:25-jdk

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
    && rm -rf /var/lib/apt/lists/*

# Install Maven
ARG MAVEN_VERSION=3.9.6
RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    | tar -xzC /opt \
    && ln -s /opt/apache-maven-${MAVEN_VERSION} /opt/maven \
    && ln -s /opt/maven/bin/mvn /usr/local/bin/mvn

ENV MAVEN_HOME=/opt/maven
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

# Create symlink for fd (fd-find is the package name in Debian/Ubuntu)
RUN ln -s $(which fdfind) /usr/local/bin/fd

# Install Neovim
RUN apt-get update && apt-get install -y neovim \
    && rm -rf /var/lib/apt/lists/*

# Install Zoxide
RUN curl -sS https://raw.githubusercontent.com/ajeetdsouza/zoxide/main/install.sh | bash

# Install Yazi (file manager)
RUN curl -fsSL https://github.com/sxyazi/yazi/releases/latest/download/yazi-x86_64-unknown-linux-gnu.zip -o /tmp/yazi.zip \
    && unzip /tmp/yazi.zip -d /tmp/yazi \
    && mv /tmp/yazi/yazi-x86_64-unknown-linux-gnu/yazi /usr/local/bin/yazi \
    && chmod +x /usr/local/bin/yazi \
    && rm -rf /tmp/yazi.zip /tmp/yazi

# Install resvg
RUN curl -fsSL https://github.com/RazrFalcon/resvg/releases/latest/download/resvg-linux-x86_64.tar.gz -o /tmp/resvg.tar.gz \
    && tar -xzf /tmp/resvg.tar.gz -C /tmp \
    && mv /tmp/resvg /usr/local/bin/resvg \
    && chmod +x /usr/local/bin/resvg \
    && rm -rf /tmp/resvg.tar.gz

# Install Nerd Font (Symbols Only)
RUN mkdir -p /usr/share/fonts/truetype/nerd-fonts \
    && curl -fsSL https://github.com/ryanoasis/nerd-fonts/releases/latest/download/NerdFontsSymbolsOnly.zip -o /tmp/nerd-font.zip \
    && unzip /tmp/nerd-font.zip -d /usr/share/fonts/truetype/nerd-fonts \
    && fc-cache -fv \
    && rm -rf /tmp/nerd-font.zip

# Install Node.js 22 LTS and Angular CLI
ARG NODE_VERSION=22
RUN curl -fsSL https://deb.nodesource.com/setup_${NODE_VERSION}.x | bash - \
    && apt-get install -y nodejs \
    && npm install -g @angular/cli@19 \
    && npm install -g npm@latest \
    && rm -rf /var/lib/apt/lists/*

# Verify Node.js and Angular CLI installations
RUN node --version && npm --version && ng version

# Install Chrome dependencies for Angular tests
# Note: Ubuntu's chromium package is often just a snap wrapper
# We install Google Chrome instead which includes the actual binary
RUN apt-get update && apt-get install -y \
    wget \
    ca-certificates \
    libnss3 \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libcups2 \
    libdrm2 \
    libxkbcommon0 \
    libxcomposite1 \
    libxdamage1 \
    libxfixes3 \
    libxrandr2 \
    libgbm1 \
    libasound2t64 \
    libpangocairo-1.0-0 \
    libgtk-3-0 \
    libxss1 \
    libappindicator3-1 \
    fonts-liberation \
    fonts-noto-color-emoji \
    xdg-utils \
    && rm -rf /var/lib/apt/lists/*

# Download and install Google Chrome (stable)
# This avoids the snap wrapper issue with Ubuntu's chromium package
RUN wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb \
    && apt-get update \
    && apt-get install -y ./google-chrome-stable_current_amd64.deb \
    && rm google-chrome-stable_current_amd64.deb \
    && rm -rf /var/lib/apt/lists/*

# Set Chrome environment variables for testing
ENV CHROME_BIN=/usr/bin/google-chrome
ENV CHROME_PATH=/usr/bin/google-chrome

WORKDIR /workspace

# Keep container running
CMD ["sleep", "infinity"]
