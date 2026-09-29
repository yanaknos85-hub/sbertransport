const { params, config } = require('@sber-sbertransport/tool-kit/tslint');

module.exports = config({
  ...params,
  files: ['src/**/*.js*', 'src/**/*.ts*'],
  rules: {
    ...params.rules,
    // ...свои правила
  }
},{
  ignores: [
    '**/*.json',
    '**/*/*.d.ts',
    '**/*/images/*',
    '**/*/img/*',
    '**/*/*.style.tsx',
  ],
});
