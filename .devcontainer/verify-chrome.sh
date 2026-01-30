#!/bin/bash
# Script to verify Chrome installation in devcontainer

echo "========================================"
echo "Chrome Installation Verification"
echo "========================================"
echo ""

# Check environment variables
echo "1. Environment Variables:"
echo "   CHROME_BIN=${CHROME_BIN:-<not set>}"
echo "   CHROME_PATH=${CHROME_PATH:-<not set>}"
echo ""

# Check for Chrome binaries
echo "2. Chrome Binary Locations:"
if [ -f /usr/bin/google-chrome ]; then
    echo "   ✓ /usr/bin/google-chrome exists"
    ls -lh /usr/bin/google-chrome
else
    echo "   ✗ /usr/bin/google-chrome NOT FOUND"
fi

if [ -f /usr/bin/google-chrome-stable ]; then
    echo "   ✓ /usr/bin/google-chrome-stable exists"
    ls -lh /usr/bin/google-chrome-stable
else
    echo "   ✗ /usr/bin/google-chrome-stable NOT FOUND"
fi
echo ""

# Check which command
echo "3. 'which' command results:"
echo "   google-chrome: $(which google-chrome 2>/dev/null || echo '<not found>')"
echo ""

# Try to get version
echo "4. Chrome Version:"
if command -v google-chrome &> /dev/null; then
    google-chrome --version 2>&1 || echo "   Error running google-chrome --version"
else
    echo "   ✗ Chrome not found in PATH"
fi
echo ""

# Check dependencies
echo "5. Required Dependencies:"
libs=(
    "libnss3"
    "libatk1.0-0"
    "libatk-bridge2.0-0"
    "libcups2"
    "libdrm2"
    "libxkbcommon0"
    "libgbm1"
)

for lib in "${libs[@]}"; do
    if dpkg -l | grep -q "^ii  $lib"; then
        echo "   ✓ $lib installed"
    else
        echo "   ✗ $lib NOT INSTALLED"
    fi
done
echo ""

# Check if running in container
echo "6. Container Info:"
if [ -f /.dockerenv ]; then
    echo "   ✓ Running in Docker container"
else
    echo "   ℹ Not running in Docker container (or /.dockerenv not present)"
fi
echo ""

# Final recommendation
echo "========================================"
echo "Recommendations:"
echo "========================================"

if [ -f /usr/bin/google-chrome ]; then
    echo "✓ Chrome appears to be installed correctly"
    echo ""
    echo "To test Angular with Chrome, run:"
    echo "  cd frontend"
    echo "  npm run test:ci"
else
    echo "✗ Chrome is NOT installed"
    echo ""
    echo "Please rebuild the devcontainer:"
    echo "  1. Press Ctrl+Shift+P"
    echo "  2. Select 'Dev Containers: Rebuild Container'"
    echo ""
    echo "Or manually install Google Chrome:"
    echo "  wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb"
    echo "  sudo apt-get update"
    echo "  sudo apt-get install -y ./google-chrome-stable_current_amd64.deb"
    echo "  rm google-chrome-stable_current_amd64.deb"
fi
echo ""
