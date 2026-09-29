const { params, config } = require('@sber-sbertransport/tool-kit/tslint');

module.exports = config({
  ...params,
  files: ['src/**/*.js*', 'src/**/*.ts*'],
  rules: {
    ...params.rules,
    // ...свои правила
    'no-use-before-define': 'off',
    '@stylistic/max-len': 'off',
    '@typescript-eslint/no-explicit-any': 'off',
    '@typescript-eslint/ban-types': 'off',
    'react-hooks/exhaustive-deps': 'off',
    'sonarjs/cognitive-complexity': 'off',
  }
},{
  ignores: [
    'src/fonts/*',
    'src/styles/*',
    '**/*/*.json',
    '**/*/*.d.ts',
    '**/*/images/*',
    '**/*/img/*',
  ],
});
