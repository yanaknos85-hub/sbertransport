/* eslint-disable prefer-promise-reject-errors */
import { RuleObject } from 'antd/lib/form';
import { StoreValue } from 'antd/lib/form/interface';
import { phoneNumberRegexp, settingsPhoneNumber } from 'utils';

export enum CostField {
  factCost = 'factCost',
  expectedCost = 'expectedCost',
}

export enum PercentFields {
  economyPercent = 'economyPercent',
}

export enum TimeFields {
  waypointWaitTime = 'waypointWaitTime',
}

export const MAX_INT = 2147483647;
export const MIN_INT = 0;
export const MAX_INT_COST = MAX_INT / 100;
export const MAX_INT_TIME = Math.floor(MAX_INT / 600) / 100;
export const MAX_INT_PERCENT = 100;
export const MIN_INT_PERCENT = 1;

export const ValidationRules = {
  general: {
    required: {
      required: true,
      message: 'Заполните поле',
    },
    shortPassword: {
      required: true,
      message: 'Пароль должен иметь минимум 8 символов',
    },
    alphaNum: {
      pattern: /^[а-яА-Я\w]+$/,
      message: 'Поле должно состоять из букв, цифр и символов подчеркивания',
    },
    alphaNumWithSpaces: {
      pattern: /^[а-яА-Я\w\s]+$/,
      message: 'Поле должно состоять из букв, цифр, пробелов и символов подчеркивания',
    },
    minMaxLength: (min: number, max: number) => () => ({
      min,
      max,
    }),
    onlyDigits: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => /^\d*$/.test(value) ? Promise.resolve() : Promise.reject('Может содержать только цифры!'),
    }),
    greaterThanZero: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (value === undefined || value === null || value === '') {
          return Promise.resolve();
        }
        return value > 0 ? Promise.resolve() : Promise.reject('Нужно ввести число > 0');
      },
    }),
    validationFloatingNumbers: (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        return /^\d*$/.test(value) ? Promise.resolve() : Promise.reject('Может содержать только целые числа');
      },
    }),
    validationDoubleNumbers: (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        return /^-?\d*(\.\d+)?$/.test(value) ? Promise.resolve() : Promise.reject('Может содержать только числа');
      },
    }),
    validationNumericNonZero: (): RuleObject => ({
      validator(_, value: string) {
        if (Number.isNaN(value)) {
          return Promise.reject(new Error('Пожалуйста, укажите числовое значение'));
        }
        if (value != null && Number(value) === 0) {
          return Promise.reject(new Error('Значение не может быть равно нулю'));
        }
        return Promise.resolve();
      },
    }),
    validationPhone: (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        return settingsPhoneNumber.test(value) ? Promise.resolve() : Promise.reject(settingsPhoneNumber.warning());
      },
    }),
    checkPhoneMask: (customMessage?: string): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        return phoneNumberRegexp.test(value)
          ? Promise.resolve()
          : Promise.reject(customMessage || 'Маска ввода должна соответствовать формату +7 (999) 999-99-99');
      },
    }),
    maskRegEx: (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        return /^[0-9A-Za-z!@.,;:'"?-]{13}$/.test(value)
          ? Promise.resolve()
          : Promise.reject('Допустим ввод только цифр. Длина ИНН должна быть до 13 символов');
      },
    }),
    maxLength: (n: number) => (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        return value.toString().length <= n ? Promise.resolve() : Promise.reject(`Не более ${n} символов`);
      },
    }),
    minLength: (n: number) => (): RuleObject => ({
      validator: (_, value) => value.toString().length >= n ? Promise.resolve() : Promise.reject(`Не менее ${n} символов`),
    }),
    max: (n: number) => (): RuleObject => ({
      validator: (_, value) => (parseFloat(value) <= n ? Promise.resolve() : Promise.reject(`Не более ${n}`)),
    }),
    maxInt: (name?: string) => (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        let max = MAX_INT;
        let message = `Не более ${max}`;
        if (name && name in CostField) {
          max = MAX_INT_COST;
          message = `Не более ${max} руб.`;
        }
        if (name && name in TimeFields) {
          max = MAX_INT_TIME;
          message = `Не более ${max} мин.`;
        }
        if (name && name in PercentFields) {
          max = MAX_INT_PERCENT;
          message = `Не более ${max}%`;
        }
        return parseFloat(value) <= max ? Promise.resolve() : Promise.reject(message);
      },
    }),
    minInt: (name?: string) => (): RuleObject => ({
      validator: (_, value) => {
        if (!value && value !== 0) {
          return Promise.resolve();
        }
        let min = MIN_INT;
        let message = `Не менее ${min}`;

        if (name && name in PercentFields) {
          min = MIN_INT_PERCENT;
          message = `Не менее ${min}%`;
        }

        return parseFloat(value) >= min ? Promise.resolve() : Promise.reject(message);
      },
    }),
  },
};

export const validationPatterns = {
  phoneValidation: /^[+\d]+$/,
  countValidation: /\S{8}/,
};
