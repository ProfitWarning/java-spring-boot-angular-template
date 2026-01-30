# Monorepo Reorganization Plan

**Date:** January 15, 2026  
**Project:** Spring Boot + Angular Full-Stack Application  
**Objective:** Reorganize codebase into clean monorepo structure with dedicated backend/ folder

---

## 📋 Executive Summary

This plan reorganizes the current mixed-root structure into a clean monorepo with:
- Dedicated `backend/` folder for Spring Boot application
- Existing `frontend/` folder for Angular application
- Enhanced root `package.json` with 25+ convenient npm scripts
- Comprehensive documentation (3 new README files)
- Organized planning documents in `docs/planning/`

**Total Changes:**
- 4 directories/files moved
- 6 configuration files updated
- 4 documentation files created
- 4 planning documents reorganized

---

## 🎯 Goals

1. **Separation of Concerns:** Backend and frontend in dedicated folders
2. **Developer Experience:** Convenient npm scripts for all common tasks
3. **Documentation:** Comprehensive READMEs for quick onboarding
4. **Organization:** Clean root directory with organized docs
5. **Zero Breaking Changes:** All functionality preserved

---

## 📊 Current vs Target Structure

### Current Structure
```
with_angular/
├── src/                    ⚠️ Backend (to move)
├── target/                 ⚠️ Backend (to move)
├── pom.xml                 ⚠️ Backend (to move)
├── Dockerfile              ⚠️ Backend (to move)
├── frontend/               ✅ Stays
├── PLAN_*.md              ⚠️ To organize
└── package.json           ⚠️ To enhance
```

### Target Structure
```
with_angular/
├── backend/               🆕 Backend application
│   ├── src/
│   ├── target/
│   ├── pom.xml
│   ├── Dockerfile
│   └── README.md          🆕
├── frontend/              ✅ Unchanged
│   └── README.md          🆕
├── docs/                  🆕
│   └── planning/          🆕
├── package.json           ⚠️ Enhanced
└── README.md              🆕
```

---

## 📝 Detailed Execution Steps

### PHASE 1: File Reorganization

#### Step 1: Create Backend Directory
```bash
mkdir backend
```

#### Step 2: Move Backend Files
```bash
move src backend\
move target backend\
move pom.xml backend\
move Dockerfile backend\
```

**Result:** Backend files cleanly isolated in `backend/` folder

---

### PHASE 2: Configuration Updates

#### Step 3: Update .gitignore

**File:** `.gitignore`

**Changes:**
```diff
- # Maven #
- target/
+ # Backend (Maven)
+ backend/target/
```

**Lines affected:** Line 27

---

#### Step 4: Update root package.json

**File:** `package.json`

**Major additions:**
- Database scripts (db:migrate, db:clean, db:info)
- Docker scripts (docker:up, docker:down, docker:logs, docker:restart)
- Enhanced clean scripts (clean:all)
- Section comments for organization
- All backend scripts use `cd backend &&` prefix

**Total scripts:** 25+ (from original 10)

---

#### Step 5: Update .devcontainer/devcontainer.json

**File:** `.devcontainer/devcontainer.json`

**Changes:**
```diff
- "postCreateCommand": "mvn dependency:resolve",
+ "postCreateCommand": "cd backend && mvn dependency:resolve",
```

**Lines affected:** Line 76

---

#### Step 6: Update .devcontainer/start-servers.sh

**File:** `.devcontainer/start-servers.sh`

**Changes:**
```diff
- cd /workspace
+ cd /workspace/backend
```

**Lines affected:** Line 19

---

#### Step 7: Update .vscode/tasks.json

**File:** `.vscode/tasks.json`

**Changes:** Update 3 Maven task commands
- Line 7: `"command": "cd backend && mvn spring-boot:run"`
- Line 70: `"command": "cd backend && mvn clean package"`
- Line 93: `"command": "cd backend && mvn test"`

---

### PHASE 3: Documentation Organization

#### Step 8: Create docs/planning/ Directory
```bash
mkdir docs
mkdir docs\planning
```

#### Step 9: Move Planning Documents
```bash
move PLAN_ANGULAR_INTEGRATION.md docs\planning\ANGULAR_INTEGRATION.md
move PLAN_DEVCONTAINER.md docs\planning\DEVCONTAINER.md
move PLAN_OPENSHIFT_DEPLOYMENT.md docs\planning\OPENSHIFT_DEPLOYMENT.md
move PLAN_WINDOWS_LOCAL.md docs\planning\WINDOWS_LOCAL.md
```

---

### PHASE 4: Documentation Creation

#### Step 10: Create Root README.md

**File:** `README.md` (new)

**Sections:**
- Architecture overview
- Project structure
- Quick start guide
- Available npm scripts (all 25+)
- Database information
- Technology stack
- API endpoints table
- Links to backend/frontend docs

**Length:** ~200 lines

---

#### Step 11: Create Backend README.md

**File:** `backend/README.md` (new)

**Sections:**
- Architecture and folder structure
- Running locally (multiple methods)
- Testing strategies
- Building (dev, prod, Docker)
- Database & Flyway migrations
- Configuration profiles
- Environment variables table
- API documentation links
- Dependencies overview
- Known issues (Lombok, field mapping)

**Length:** ~250 lines

---

#### Step 12: Create Frontend README.md

**File:** `frontend/README.md` (new)

**Sections:**
- Architecture and folder structure
- Running locally
- Testing (watch, CI, coverage)
- Building (dev, prod)
- Styling with Tailwind CSS v4
- API integration and proxy
- Environment configuration
- State management (Signals)
- Services overview
- Components overview
- Code quality tools
- Scripts table
- Configuration files
- Key dependencies
- Features checklist

**Length:** ~280 lines

---

### PHASE 5: Verification

#### Step 13: Verify File Structure
```bash
# Check backend files
dir backend\src
dir backend\pom.xml
dir backend\Dockerfile
dir backend\README.md

# Check documentation
dir docs\planning
dir README.md
dir frontend\README.md
```

#### Step 14: Test NPM Scripts
```bash
# Test new scripts (without actually running servers)
npm run --silent 2>nul | findstr "db:"
npm run --silent 2>nul | findstr "docker:"
npm run --silent 2>nul | findstr "clean:"
```

---

### PHASE 6: Git Commit

#### Step 15: Review and Commit
```bash
git status
git add .
git commit -m "refactor: reorganize into monorepo structure

- Move backend files to dedicated backend/ directory
- Enhance root package.json with convenient npm scripts (25+ scripts)
- Add comprehensive documentation (root, backend, frontend READMEs)
- Organize planning documents into docs/planning/
- Update all configuration paths (.devcontainer, .vscode, .gitignore)
- Add database, Docker, and cleanup scripts

BREAKING CHANGE: Backend files moved from root to backend/ folder.
All Maven commands now run from backend/ directory."
```

---

## 📦 Files Modified/Created Summary

### Moved (4 items)
| From | To |
|------|-----|
| `src/` | `backend/src/` |
| `target/` | `backend/target/` |
| `pom.xml` | `backend/pom.xml` |
| `Dockerfile` | `backend/Dockerfile` |

### Updated (6 items)
| File | Change Type |
|------|-------------|
| `package.json` | Enhanced with 15+ new scripts |
| `.gitignore` | Backend paths updated |
| `.devcontainer/devcontainer.json` | Path update (1 line) |
| `.devcontainer/start-servers.sh` | Path update (1 line) |
| `.vscode/tasks.json` | Path updates (3 lines) |
| Planning docs → `docs/planning/` | Reorganized (4 files) |

### Created (4 items)
| File | Purpose | Size |
|------|---------|------|
| `README.md` | Root documentation | ~200 lines |
| `backend/README.md` | Backend documentation | ~250 lines |
| `frontend/README.md` | Frontend documentation | ~280 lines |
| `docs/planning/` | Planning docs folder | 4 files |

---

## ⚠️ Breaking Changes

### What Changes
- Backend files moved from root to `backend/` directory
- All Maven commands require `cd backend &&` prefix
- Configuration file paths updated

### What Stays the Same
- Frontend structure unchanged
- Git history preserved
- All functionality works identically
- No code logic changes
- Shared configs remain at root

---

## ✅ Testing Checklist

After execution, verify:

- [ ] Backend files exist in `backend/` folder
- [ ] Planning docs in `docs/planning/` folder
- [ ] All 3 README files created
- [ ] `npm run backend` works
- [ ] `npm run frontend` works
- [ ] `npm run dev` starts both servers
- [ ] `npm run db:info` shows Flyway info
- [ ] `npm run docker:up` starts containers
- [ ] VS Code tasks work correctly
- [ ] Git commit created successfully

---

## 🎯 Success Criteria

1. ✅ Clean monorepo structure with backend/ and frontend/ folders
2. ✅ 25+ convenient npm scripts available
3. ✅ Comprehensive documentation for quick onboarding
4. ✅ Organized planning documents
5. ✅ All functionality preserved
6. ✅ All tests pass
7. ✅ Single git commit with all changes

---

## 📚 New NPM Scripts Reference

### Development
- `npm run dev` - Start both servers
- `npm run backend` - Spring Boot only
- `npm run frontend` - Angular only

### Build
- `npm run build` - Build both
- `npm run build:backend` - Maven package
- `npm run build:frontend` - Angular prod build

### Testing
- `npm test` - Run all tests
- `npm run test:backend` - Maven test
- `npm run test:frontend` - Angular test

### Database
- `npm run db:migrate` - Run Flyway migrations
- `npm run db:clean` - Clean database
- `npm run db:info` - Migration status

### Docker
- `npm run docker:up` - Start containers
- `npm run docker:down` - Stop containers
- `npm run docker:logs` - View logs
- `npm run docker:restart` - Restart all

### Code Quality
- `npm run lint` - Lint frontend
- `npm run format` - Format frontend

### Cleanup
- `npm run clean` - Clean build artifacts
- `npm run clean:backend` - Maven clean
- `npm run clean:frontend` - Remove dist/node_modules
- `npm run clean:all` - Deep clean everything

---

## 📅 Execution Timeline

**Estimated Duration:** 10-15 minutes

1. **File Operations:** 2 minutes
2. **Configuration Updates:** 3 minutes
3. **Documentation Creation:** 5 minutes
4. **Verification:** 3 minutes
5. **Git Commit:** 2 minutes

---

## 🔄 Rollback Plan

If issues occur:

```bash
# Restore from git
git reset --hard HEAD

# Or manually move files back
move backend\src .\
move backend\target .\
move backend\pom.xml .\
move backend\Dockerfile .\
rmdir backend
```

---

## 📝 Post-Reorganization Tasks

After successful reorganization:

1. **Rebuild Devcontainer** (optional but recommended)
2. **Test full stack** (create message, list messages)
3. **Update team documentation** if applicable
4. **Consider setting up:**
   - Pre-commit hooks (Husky)
   - GitHub Actions CI/CD
   - SonarQube integration
   - ESLint/Prettier for frontend

---

## ✨ Benefits

### For Developers
- Clear separation between frontend and backend
- Convenient scripts for all common tasks
- Comprehensive documentation
- Easier onboarding

### For Project
- Scalable monorepo structure
- Better organization
- Professional structure
- Ready for team collaboration

---

**Plan Status:** READY FOR EXECUTION  
**Approved By:** User  
**Execution Date:** January 15, 2026  

---

*This plan document will be kept as a reference after execution.*
