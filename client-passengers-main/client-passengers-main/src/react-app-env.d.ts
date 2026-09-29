/* eslint-disable @typescript-eslint/no-explicit-any */
// eslint-disable-next-line spaced-comment
/// <reference types="react-scripts" />

interface Window {
  MobxStores: any;
  _IS_BASIC_AUTH: boolean;
}

declare module '*.module.less' {
  const classes: { [key: string]: string };
  export default classes;
}

declare module '*.json' {
  const value: any;
  export default value;
}

declare module '*.css';
declare module '*.less';
