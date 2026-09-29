import { DesignVersion } from 'constants/constants.app';

export interface ISettingsStore {
  isUploaderVisible: boolean;
  designVersion: DesignVersion;
  menuCounterList: Record<string, number>;
  setIsUploaderVisible(val: boolean): void;
}

export enum Test {
  test = 'test',
}
