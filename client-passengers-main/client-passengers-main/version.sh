function parse_git_dirty() {
  git diff --quiet --ignore-submodules HEAD 2>/dev/null
  [ $? -eq 1 ] && echo "*"
}

function parse_git_branch() {
  git branch --no-color 2>/dev/null | sed -e '/^[^*]/d' -e "s/* \(.*\)/\1$(parse_git_dirty)/" | tr -d \"\'\\&
}

function parse_git_hash() {
  git rev-parse --short HEAD 2>/dev/null | sed "s/\(.*\)/\1/"
}

function parse_git_last_commits() {
  git log --grep "Pull request" --oneline --pretty="- %s" -n 1 | tr -d \"\'\\&
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

	branch=$(parse_git_branch)
	hash_val=$(parse_git_hash)
	last_commit=$(parse_git_last_commits)
	build_date=$(date '+%d-%m-%Y %H:%M:%S')

	echo "{ \"ver\": { \"branch\": \"$branch\", \"hash\": \"$hash_val\", \"last commit\": \"$last_commit\", \"date\": \"$build_date\" }}" > $file;
}

build