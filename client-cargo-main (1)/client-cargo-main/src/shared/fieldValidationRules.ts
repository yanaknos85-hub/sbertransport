import { RuleObject } from 'antd/lib/form';
import { StoreValue } from 'antd/lib/form/interface';
import { declOfNumForSymbols, formatRubles } from 'utils';

import { AddressLoadType } from 'stores/Cargos/typesMulti';

export const ValidationRules = {
  general: {
    required: {
      required: true,
      message: 'Пожалуйста, заполните это поле',
    },
    lattersAndDigits: {
      // eslint-disable-next-line no-useless-escape
      pattern: new RegExp(/^[\u0400-\u04FFa-zA-Z\d\s",\'&+\u00AB\u00BB()-]+$/),
      message: 'Недопустимое значение',
    },
    minMaxLength: (min: number, max: number) => (): RuleObject => ({
      min,
      max,
      message: `Значения поле должны быть между ${min} и ${max} ${declOfNumForSymbols(max)}`,
    }),
    min: (n: number) => (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        return Number(value) >= n ? Promise.resolve() : Promise.reject(new Error(`Минимальное значение - ${n}`));
      },
    }),
    greaterThanZero: () => (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (value > 0) {
          return Promise.resolve();
        }
        return Promise.reject(new Error('Значение должно быть больше ноля!'));
      },
    }),
    minLength: (n: number) => (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        return value.toString().length >= n
          ? Promise.resolve()
          : Promise.reject(new Error(`Не менее чем ${n} ${declOfNumForSymbols(n)}`));
      },
    }),
    maxLength: (n: number) => (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        return value.toString().length <= n
          ? Promise.resolve()
          : Promise.reject(new Error(`Не более ${n} ${declOfNumForSymbols(n)}`));
      },
    }),
    maxMoneyValue: (n: number): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => +value <= n ? Promise.resolve() : Promise.reject(new Error(`Не более ${formatRubles(n)} рублей`)),
    }),
    includes:
      (list: (string | number)[] = []) => (): RuleObject => ({
        validator: (_: RuleObject, value: StoreValue): Promise<void> => {
          if (!value) {
            return Promise.resolve();
          }
          return list.includes(value) ? Promise.resolve() : Promise.reject(new Error('Недопустимое значение'));
        },
      }),
    empty: (): RuleObject => ({
      validator: (_: RuleObject, value: string): Promise<void> => {
        if (!value || value === '') {
          return Promise.reject(new Error('Пожалуйста, заполните это поле'));
        }
        return Promise.resolve();
      },
    }),
    addressType: (waypointsLength: number, name: number): RuleObject => ({
      validator: (_: RuleObject, value) => {
        if (name > 0 && name === waypointsLength - 1 && value === AddressLoadType.LOAD) {
          return Promise.reject(new Error('Последний адрес в маршруте не может быть с типом "Сбор"'));
        }
        if (name === 0 && value === AddressLoadType.UNLOAD) {
          return Promise.reject(new Error('Первый адрес в маршруте не может быть с типом "Доставка"'));
        }
        return Promise.resolve();
      },
    }),
    phone: (): RuleObject => ({
      validator: (_: RuleObject, value: string) => {
        const numberPhoneLength = value?.replace(/\D/g, '').length;

        if (numberPhoneLength < 11) {
          return Promise.reject(new Error('Пожалуйста, укажите полный номер телефона'));
        }

        if (!value || value === '+7 (___) ___ __ __') {
          return Promise.reject(new Error('Пожалуйста, заполните это поле'));
        }
        return Promise.resolve();
      },
    }
    ),
  },
};

export const availablePhoneCode = [
  '73',
  '78',
  '79',
  '74',
  '70',
  '372',
  '77',
  '374',
  '352',
  '995',
  '371',
  '370',
  '375',
  '998',
  '996',
  '44',
  '43',
  '33',
  '1',
  '38',
  '972',
  '381',
  '373',
  '358',
  '49',
  '420',
  '421',
  '36',
];

const availablePhoneCodeReg = `(${availablePhoneCode.join('|')})`;
const phoneCodeReg = new RegExp(`^[+]${availablePhoneCodeReg}[0-9]{9}$`);

const phoneReg = new RegExp(/[+\d+\s()]{18}/g);

const vspCodeReg = new RegExp(/\d{4}\//);

const vspCodeFullReg = new RegExp(/^\d{2,3}_\d{2,4}_\d{2,5}$/);

const emptyStringValidation = new RegExp(/^(?!\s*$).+/);

export const validationPatterns = {
  phoneValidation: phoneCodeReg,
  numberValidation: phoneReg,
  vspValidation: vspCodeReg,
  vspValidationFull: vspCodeFullReg,
  emptyStringValidation: emptyStringValidation,
};
