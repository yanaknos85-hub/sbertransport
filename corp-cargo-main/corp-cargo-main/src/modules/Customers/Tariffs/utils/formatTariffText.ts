import { TariffTypes } from 'constants/constants.app';

interface TariffString {
  [TariffTypes.INCOME]: string;
  [TariffTypes.OUTCOME]: string;
}

export const formatTariffText = (contractType: TariffTypes) => {
  return (strings: TemplateStringsArray, ...values: TariffString[]) => {
    const startString = values
      .map((val, i) => `${strings[i]}${val[contractType]}`)
      .join('');

    return `${startString}${strings.at(-1)}`;
  };
};
