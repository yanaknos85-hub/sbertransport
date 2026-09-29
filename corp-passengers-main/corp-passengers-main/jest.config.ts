import { Config } from "jest";

const jestConfig: Config = {
  preset: 'ts-jest',
  testEnvironment: 'jsdom',
  testMatch: [
      "<rootDir>/src/**/*(*.)@(spec|test).[tj]s?(x)",
      "<rootDir>/src/**/?(*.)(spec|test).{ts,tsx}"
  ],
  moduleDirectories: [
      "node_modules",
      "src"
  ],
  transform: {
     "^.+\\.(ts|tsx)?$": "ts-jest",
      "^.+\\.(js|jsx)$": ["babel-jest", {configFile: './.babelrc'}]
  },
  moduleNameMapper: {
    "\\.(jpg|ico|jpeg|png|gif|eot|otf|webp|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$": "<rootDir>/__mocks__/fileMock.js",
    "\\.(css|less|scss)$": "<rootDir>/__mocks__/styleMock.js",
    "\\.svg$": "<rootDir>/__mocks__/svgMock.jsx"
  },
  transformIgnorePatterns: [
    "/node_modules/(?!@sber-sbertransport)"
  ],
  testTimeout: 50000,
  maxWorkers: 5,
  setupFilesAfterEnv: [
    "<rootDir>/src/setupTests.ts"
  ],
  collectCoverageFrom: [
    '<rootDir>/src/**/*.{js,jsx,ts,tsx}',

    // Исключения
    '!<rootDir>/src/api/**',
    '!<rootDir>/src/app/**',
    '!<rootDir>/src/context/**',
    '!<rootDir>/src/Context/**',
    '!<rootDir>/src/i18n/**',
    '!<rootDir>/src/ioc/**',
    '!<rootDir>/src/mf/**',
    '!<rootDir>/src/Services/**',
    '!<rootDir>/src/styles/**',
    '!<rootDir>/src/index.ts',

    '!**/*.d.ts',                    // исключить TypeScript декларации
    '!**/*.config.{js,ts}',          // исключить конфиги
    '!**/*.setup.{js,ts}',           // исключить setup файлы
    '!**/constants/**',              // исключить константы
    '!**/Сonstants/**',              // исключить константы
    '!**/*.constants.{js,ts}',       // исключить константы
    '!**/types/**',                  // исключить типы
    '!**/Types/**',                  // исключить типы
    '!**/*.types.{js,ts}',           // исключить типы
    '!**/*.type.{js,ts}',            // исключить типы
    '!**/*.interfaces.{js,ts}',      // исключить интерфейсы
    '!**/*.interface.{js,ts}',       // исключить интерфейсы
    '!**/interfaces/**',             // исключить интерфейсы
    '!**/Interfaces/**',             // исключить интерфейсы
    '!**/__mocks__/**',              // исключить моки
    '!**/__fixtures__/**',           // исключить фикстуры
    '!**/__tests__/**',              // исключить тесты
    '!**/*.test.{js,jsx,ts,tsx}',    // исключить тесты
    '!**/*.spec.{js,jsx,ts,tsx}',    // исключить тесты
    '!**/*.stories.{js,jsx,ts,tsx}', // исключить storybook
  ],
  coveragePathIgnorePatterns: [
    '/src/setupTests.ts',
  ],
  coverageReporters: [
    'text',
    'lcov',
    'clover',
    ['cobertura', {file: 'sonar-report.xml'}]
  ],
  coverageProvider: 'v8',
}

export default jestConfig
