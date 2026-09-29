const { params, config } = require('@sber-sbertransport/tool-kit/tslint');
const eslintPluginSimpleImportSort = require("eslint-plugin-simple-import-sort");

module.exports = config({
  ...params,
  files: ['src/**/*.js*', 'src/**/*.ts*'],
  plugins: { ...params.plugins, "simple-import-sort": eslintPluginSimpleImportSort },
  rules: {
    ...params.rules,
    // ...свои правила
    'no-use-before-define': 'off',
    '@stylistic/max-len': 'off',
    '@typescript-eslint/no-explicit-any': 'off',
    '@typescript-eslint/ban-types': 'off',
    'react-hooks/exhaustive-deps': 'off',
    'sonarjs/cognitive-complexity': 'off',
    "simple-import-sort/imports": [
        "error",
        {
          "groups": [
            // Side effect imports
            ["^\\u0000"],
            // Packages. Order matters.
            ["^react", "^@?\\w", "^[^.]"],
            // Absolute imports and other imports
            ["^api\/?", "^stores\/", "^types\/", "^interfaces\/", "^constants\/", "^context\/", "^hooks\/", "^utils\/", "^components\/", "^modules\/", "^pages\/", "^assets\/", "^icons\/"],
            // Relative imports
            ["^\\."],
            // Style imports
            ["^.+\\.module.s?css$"]
          ]
        }
      ],
    "simple-import-sort/exports": "error",
    "sort-imports": "off",
    "import/order": "off",
  }
},{
  ignores: [
    'src/fonts/*',
    'src/styles/*',
    '**/*/*.d.ts',
    '**/*/images/*',
    '**/*/img/*',
    '**/*/*.test.*',
    '**/*/*.spec.*',
  ],
});