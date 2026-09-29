const { params, config } = require('@sber-sbertransport/tool-kit/tslint');

module.exports = config({
  ...params,
  files: ['src/**/*.js*', 'src/**/*.ts*'],
  rules: {
    ...params.rules,
  }
},{
  ignores: [
    '**/*/*.style.tsx',
    '**/*/*.d.ts',
    '**/*/images/*',
    '**/*/img/*',
  ],
});
