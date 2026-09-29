import { LimitColorsPercent } from '../Limit.interface';

export class LimitColorsPercentModel {
  name: string | undefined;

  value: string | undefined;

  constructor(limitSetting: LimitColorsPercent) {
    this.name = limitSetting.name;
    this.value = limitSetting.value;
  }
}
