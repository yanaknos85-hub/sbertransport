function parse_git_dirty() {
  git diff --quiet --ignore-submodules HEAD 2>/dev/null
  [ $? -eq 1 ] && echo "*"
}

function parse_git_branch() {
  git branch --no-color 2>/dev/null | sed -e '/^[^*]/d' -e "s/* \(.*\)/\1$(parse_git_dirty)/"
}

function parse_git_hash() {
  git rev-parse --short HEAD 2>/dev/null | sed "s/\(.*\)/\1/"
}

function parse_git_last_commits() {
  git log --grep "Pull request" --oneline --pretty="- %s" -n 1
}

banner() {
  msg="# $* #"
  edge=$(echo "$msg" | sed 's/./#/g')
  echo "$edge"
  echo "$msg"
  echo "$edge"
  echo '\n'
}

build() {
	VERSION="branch: $(parse_git_branch) | hash: $(parse_git_hash) | date: $(date '+%d-%m-%Y %H:%M:%S')";

	banner "BUILD VERSION: $VERSION"

	file=version.json

	echo "" > $file;
	echo "{ \"ver\": { \"branch\": \"$(parse_git_branch)\", \"hash\": \"$(parse_git_hash)\", \"last commit\": \"$(parse_git_last_commits)\", \"date\": \"$(date '+%d-%m-%Y %H:%M:%S')\" }}" > $file;
}

build