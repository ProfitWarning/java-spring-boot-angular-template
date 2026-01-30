# Cleanup Scripts - Troubleshooting

## Issue: `EBUSY: resource busy or locked` when cleaning node_modules

This error occurs when a process has files locked in `node_modules`, typically:
1. Angular dev server is running
2. VS Code is indexing files
3. A test runner is active

## Solutions

### Option 1: Stop all processes first (Recommended)

**In your devcontainer terminal:**
```bash
# Stop any running dev servers (Ctrl+C if running in terminal)
# Or find and kill processes:
pkill -f "ng serve"
pkill -f "mvn spring-boot:run"

# Then run clean
npm run clean:all:full
```

### Option 2: Use safer clean commands

We provide different levels of cleanup:

```bash
# Clean build artifacts only (safe, always works)
npm run clean              # Backend + frontend dist/.angular
npm run clean:frontend     # Frontend dist/.angular only
npm run clean:backend      # Backend target/ only

# Clean everything including node_modules (requires no running processes)
npm run clean:all:full          # Everything including all node_modules
npm run clean:frontend:full     # Frontend including node_modules
```

### Option 3: Manual cleanup (when all else fails)

**In your devcontainer terminal:**
```bash
# Stop all Node/Java processes
pkill -f node
pkill -f java

# Wait a moment for processes to fully exit
sleep 2

# Then manually remove
rm -rf frontend/node_modules
rm -rf node_modules
rm -rf frontend/dist
rm -rf frontend/.angular
cd backend && mvn clean
```

## Recommended Workflow

1. **Daily development:** Use `npm run clean` (doesn't touch node_modules)
2. **Fresh start needed:** Stop all servers → `npm run clean:all:full` → `npm run install:all`
3. **Stuck node_modules:** Close VS Code → kill processes → manual rm -rf

## Scripts Reference

| Script | What it cleans | Safe while dev server running? |
|--------|----------------|--------------------------------|
| `clean:backend` | Backend `target/` only | ✅ Yes |
| `clean:frontend` | Frontend `dist/`, `.angular/` | ✅ Yes |
| `clean` | Both backend + frontend artifacts | ✅ Yes |
| `clean:all` | All artifacts (no node_modules) | ✅ Yes |
| `clean:frontend:full` | Frontend + node_modules | ❌ No - stop server first |
| `clean:all:full` | Everything + all node_modules | ❌ No - stop all processes first |

## Why does this happen?

- **Windows:** File locking is more aggressive
- **Linux:** Less common but can happen when processes hold file handles
- **Devcontainer:** VS Code may index files from the Windows host
- **node_modules:** Contains thousands of files, higher chance of locks

## Prevention

- Stop dev servers before cleaning node_modules
- Use the `:full` variants only when needed
- Keep `npm run clean` (without `:full`) for regular use
