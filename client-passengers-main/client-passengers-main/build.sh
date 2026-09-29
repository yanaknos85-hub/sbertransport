IMAGE="front-nginx"
GLOBAL_DOCKER_RELEASE_VERSION=$1

# //////// DOCKER SIGMA ACCESS DATA ////////
REGISTRY_DEV_URL="registry.sigma.sbrf.ru/dev/ci02351878/ci02353146_transport_dev"
REGISTRY_PROD_URL="registry.sigma.sbrf.ru/dev/ci02351878/ci02353146_transport"

REGISTRY_LOGIN="cab-sa-ci000274"
REGISTRY_PASS=$(cat ./build_pass.txt)

# //////// GIT GET DATA START ////////

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
  git log --grep "Pull request" --oneline --pretty="- %s" -n 10 | tr -d '"'
}
# //////// GIT GET DATA END ////////

# //////// DECLARE VARIABLES & METHODS WITH SETTING DEFAULT CONFIGURATION START ////////
# Any subsequent(*) commands which fail will cause the shell script to exit immediately
set -e
# Method for print pretty message
banner() {
  msg="# $* #"
  edge=$(echo "$msg" | sed 's/./#/g')
  echo "$edge"
  echo "$msg"
  echo "$edge"
  echo '\n'
}

frontBuild() {
banner "FRONTEND BUILD START with name: $NEW_BUILD_VERSION"
# Remove old build
echo "MESSAGE: Remove old build"
rm -rf ./build
# Remove node_modules
echo "MESSAGE: Remove node_modules"
rm -rf ./node_modules # !!! Сommenting out this line is unsafe for the final build !!!
# Reinstall packages
echo "MESSAGE: Reinstall packages"
yarn --cwd ./app # !!! Сommenting out this line is unsafe for the final build !!!
# Start build
echo "MESSAGE: Start frontend build for $NETWORK_LOOP"
yarn --cwd ./app build:"$NETWORK_LOOP" # It waits SIGMA, CLOUD and etc
banner "FRONTEND BUILD END"
}

dockerBuild(){
banner "WRITE BUILD VER"
sed -r -i -e "s|<head>|<head><!-- Build: $NEW_BUILD_VERSION \| $(date '+%d-%m-%Y %H:%M:%S') -->|g" build/index.html
# //////// WRITE BUILD VER END ////////

# //////// DOCKER BUILD START ////////
banner "DOCKER BUILD START"
## Docker build & deploy
docker build -t $REGISTRY_URL/transport/$NEW_BUILD_VERSION -t $REGISTRY_URL/transport/$LATEST_BUILD_VERSION .
echo "$REGISTRY_PASS" | docker login $REGISTRY_URL -u $REGISTRY_LOGIN --password-stdin
docker push $REGISTRY_URL/transport/$NEW_BUILD_VERSION
docker push $REGISTRY_URL/transport/$LATEST_BUILD_VERSION
# DOCKER_IMAGE_ID="$(docker images -q $REGISTRY_URL/transport/$NEW_BUILD_VERSION)"
banner "DOCKER BUILD END"
}

sendMessageToSlack() {
banner "SEND MESSAGE TO SLACK"
https://hooks.slack.com/services/XXX
curl -X POST -H 'Content-type: application/json' --data "{\"text\":\"$DEPLOY_MESSAGE\"}" $SLACK_URL
}

updateChangeLog() {
# Windows version
sed -i "1s/^/- - -\n## __$NEW_BUILD_VERSION\__\n/" CHANGELOG.md
MACOS 11.5.2 version
sed -i'.bak' -e "1s/^/----\n\n## __$NEW_BUILD_VERSION\__\n\n/" CHANGELOG.md
rm CHANGELOG.md.bak
# Push info about update CHANGELOG, in git
git add . -A
git commit -m "TRANSPORT-2781 upd CHANGELOG with new build: $NEW_BUILD_VERSION"
}

build() {
# //////// 2. SELECT AND ENTER VERSION ////////
echo "Нужно указывать версию сборки? (например D-01.001.000)?"
select vn in "Нет" "Да";
do
  case $vn in
      'Нет' )
          NEW_BUILD_VERSION="$IMAGE:$(date +%y%m%d)-$(parse_git_hash)"
        break;;
      'Да' )
          echo "Укажите номер версии: "
          read RELEASE_VERSION
          NEW_BUILD_VERSION="$IMAGE:release-$RELEASE_VERSION"
        break;;
  esac
done

echo "NEW_BUILD_VERSION: $NEW_BUILD_VERSION"
echo "REGISTRY_URL: $REGISTRY_URL"
echo "REGISTRY_LOGIN: $REGISTRY_LOGIN"
echo "NETWORK_LOOP: $NETWORK_LOOP"

# Declare variables
LATEST_BUILD_VERSION="$IMAGE:latest"

frontBuild

dockerBuild

GIT_BRANCH="$(parse_git_branch), hash: $(parse_git_hash)"

DEPLOY_MESSAGE="\n
Image: \`$IMAGE\`\n
Created: \`$(date '+%d-%m-%Y %H:%M:%S')\`\n
Version: \`$NEW_BUILD_VERSION\`\n
Network_loop: \`$NETWORK_LOOP\`\n
Git branch: \`$GIT_BRANCH\`\n
Last commits:\n
\`\`\`
$(parse_git_last_commits)
\`\`\`
\n
"

# sendMessageToSlack

# updateChangeLog

echo '======== BUILD SUCCESS ======='
echo $DEPLOY_MESSAGE
echo '=============================='
}

  # //////// 1. SELECT URL, PASSWORD, LOGIN ////////
echo "Для какого стенда будет эта сборка?"
select yn in "DEV/СТ/НТ" "ПСИ";
do
  case $yn in
    'DEV/СТ/НТ' )
        REGISTRY_URL=$REGISTRY_DEV_URL
        REGISTRY_LOGIN=$REGISTRY_LOGIN
        REGISTRY_PASS=$REGISTRY_PASS
        NETWORK_LOOP='sigma'
        build
      break;;
    'ПСИ' )
        REGISTRY_URL=$REGISTRY_PROD_URL
        REGISTRY_LOGIN=$REGISTRY_LOGIN
        REGISTRY_PASS=$REGISTRY_PASS
        NETWORK_LOOP='sigma'
        build
      break;;
  esac
done
