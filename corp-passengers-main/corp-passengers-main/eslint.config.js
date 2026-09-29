const { params, config } = require('@sber-sbertransport/tool-kit/tslint');

module.exports = config({
  ...params,
  files: ['src/**/*.js*', 'src/**/*.ts*'],
  rules: {
    ...params.rules,
    // ...свои правила
    "no-use-before-define": ["off"],
  }
},{
  ignores: [
    'src/fonts/*',
    'src/styles/*',
    '**/*/*.d.ts',
    '**/*/images/*',
    '**/*/img/*',
  ],
});
