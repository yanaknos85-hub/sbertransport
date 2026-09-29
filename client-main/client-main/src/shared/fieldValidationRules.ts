import { RuleObject } from 'antd/lib/form';
import { StoreValue } from 'antd/lib/form/interface';
import { clearPhone } from 'modules/ProfilePage/components/MainInformation/components/PhoneModal/PhoneModal';
import { declOfNumForSymbols, formatRubles } from 'utils';

export const ValidationRules = {
  general: {
    required: {
      required: true,
      message: 'Пожалуйста, заполните это поле',
    },
    lattersAndDigits: {
      pattern: new RegExp(/^[\u0400-\u04FFa-zA-Z\d\s]+$/),
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
    checkingEditingPhoneNumber: (n: string): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => clearPhone(value) !== n ? Promise.resolve() : Promise.reject(new Error(`Сохранить возможно только при редактировании номера`)),
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

export const validationPatterns = {
  phoneValidation: phoneCodeReg,
  numberValidation: phoneReg,
};
