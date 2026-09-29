require('dotenv').config();

const fs = require("fs");

const { MF_DEFAULT_CONFIG, mfConfigPath } = require('../constants');

const init = (data) =>
  fs.readFile(mfConfigPath, "utf8", () => (
    fs.writeFile(
      mfConfigPath,
      JSON.stringify(data),
        err => err && console.error(err)
    )
  ));

((data) =>
    fs.existsSync(mfConfigPath)
      ? fs.stat(mfConfigPath, err =>
        err
          ? console.error(err)
          : fs.unlink(
            mfConfigPath,
              err => err ? console.error(err) : init(data)
          )
      )
      : init(data)
)(process.argv[2] === "prod"
  ? {
    ...MF_DEFAULT_CONFIG,
    remotes: Object.assign({},
      ...Object
        .keys(MF_DEFAULT_CONFIG.remotes)
        .map(remote => ({
          [remote]: `${remote}@[window.${process.env.REACT_APP_MF_LINK}.${remote}]/${remote}.js?v=${[Date.now()]}`
        }))
    )
  } : MF_DEFAULT_CONFIG
);
