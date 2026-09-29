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
    '<rootDir>/src/**/*.{js,jsx,ts,tsx}'
  ],
  coveragePathIgnorePatterns: [
    '<rootDir>/src/api/'
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