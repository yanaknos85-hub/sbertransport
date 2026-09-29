import { ContractTypes } from 'constants/constants.app';

interface ContractString {
  [ContractTypes.INCOME]: string;
  [ContractTypes.OUTCOME]: string;
}

export const formatContractText = (contractType: ContractTypes) => {
  return (strings: TemplateStringsArray, ...values: ContractString[]) => {
    const startString = values
      .map((val, i) => `${strings[i]}${val[contractType]}`)
      .join('');

    return `${startString}${strings.at(-1)}`;
  };
};
