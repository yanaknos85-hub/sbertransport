# gigacode:skill:git-workflow
<!-- gigacode:skill-file: git-workflow -->

## Usage
Invoke with `/git` or `skill: "git-workflow"`

## Capabilities

### Check Repository Status
- `git status` - view changes
- `git diff` - review changes
- `git log` - view commit history

### Manage Branches
- `git branch` - list branches
- `git checkout <branch>` - switch branch
- `git checkout -b <branch>` - create new branch

### Stage and Commit
- `git add .` - stage all changes
- `git add <file>` - stage specific file
- `git commit -m "message"` - commit changes

### Sync with Remote
- `git pull` - fetch and merge
- `git push` - push commits

### Work with Stashes
- `git stash` - save current changes
- `git stash list` - view stashes
- `git stash pop` - restore last stash

### Common Workflows

**Before break:**
```bash
git add .
git stash push -m "Gigacode: [description]"
```

**After resume:**
```bash
git stash list
git stash pop
```

**Start new feature:**
```bash
git checkout development
git pull
git checkout -b feature/feature-name
```

**Update from remote:**
```bash
git checkout development
git pull origin development
```
