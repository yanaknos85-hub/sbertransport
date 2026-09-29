module.exports = {
  extends: ['./node_modules/@sber-sbertransport/tool-kit/commitlint'],
  parserPreset: {
    parserOpts: {
      issuePrefixes: ['TRANSPORT-'],
    },
  },
  rules: {
    'references-empty': [1, 'never'],
  },
};
