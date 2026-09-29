/* eslint-disable @typescript-eslint/no-empty-function */
/* eslint-disable @typescript-eslint/no-explicit-any */
import moment from 'moment';
import { FormInstance } from 'antd/lib/form';

// Import the module under test
import {
  ValidationRules,
  MAX_INT,
  MAX_INT_COST,
  MAX_INT_TIME,
  MIN_VEHICLE_MANUFACTURED_YEAR,
  ALLOWED_VEHICLE_REGISTRY_CHARS,
  matchPattern
} from '../../fieldValidationRules/fieldValidationRules';

// Import type definitions
import {
  RangeNumber, RangeNumberPartial, TimeRange, CostRange
} from '../../fieldValidationRules/types/Ranges';
import { CostField } from '../../fieldValidationRules/types/CostField';
import { TimeFields, YearFields } from '../../fieldValidationRules/types/TimeFields';
import { ioTypeFromEnum } from '../../ioTypeFromEnum';

// Mock form instance for validator tests
const createMockForm = (values: Record<string, any> = {}) => ({
  getFieldValue: jest.fn((key: string) => values[key]),
}) as unknown as FormInstance;

describe('fieldValidationRules', () => {
  describe('Constants', () => {
    describe('MAX_INT', () => {
      it('should be 2^31 - 1 (2147483647)', () => {
        expect(MAX_INT).toBe(2147483647);
      });
    });

    describe('MAX_INT_COST', () => {
      it('should be MAX_INT / 100 (21474836.47)', () => {
        expect(MAX_INT_COST).toBe(21474836.47);
      });
    });

    describe('MAX_INT_TIME', () => {
      it('should be floor(MAX_INT / 600) / 100', () => {
        expect(MAX_INT_TIME).toBe(Math.floor(2147483647 / 600) / 100);
      });
    });

    describe('MIN_VEHICLE_MANUFACTURED_YEAR', () => {
      it('should be 1900', () => {
        expect(MIN_VEHICLE_MANUFACTURED_YEAR).toBe(1900);
      });
    });

    describe('ALLOWED_VEHICLE_REGISTRY_CHARS', () => {
      it('should contain Cyrillic and Latin characters for Russian plates', () => {
        expect(ALLOWED_VEHICLE_REGISTRY_CHARS).toBe('[ABEKMHOPCTYXDabekmhopctyxDАВЕКМНОРСТУХавекмнорстух]');
      });

      it('should match expected regex pattern', () => {
        const chars = ALLOWED_VEHICLE_REGISTRY_CHARS.replace(/[[\]]/g, '');
        expect(chars).toContain('A');
        expect(chars).toContain('B');
        expect(chars).toContain('E');
        expect(chars).toContain('K');
        expect(chars).toContain('M');
        expect(chars).toContain('O');
        expect(chars).toContain('P');
        expect(chars).toContain('C');
        expect(chars).toContain('T');
        expect(chars).toContain('Y');
        expect(chars).toContain('X');
        expect(chars).toContain('D');
        expect(chars).toContain('А');
        expect(chars).toContain('В');
        expect(chars).toContain('Е');
        expect(chars).toContain('К');
        expect(chars).toContain('М');
        expect(chars).toContain('Н');
        expect(chars).toContain('О');
        expect(chars).toContain('Р');
        expect(chars).toContain('С');
        expect(chars).toContain('Т');
        expect(chars).toContain('У');
        expect(chars).toContain('Х');
      });
    });
  });

  describe('ValidationRules.general', () => {
    describe('required', () => {
      it('should have required flag set to true', () => {
        expect(ValidationRules.general.required.required).toBe(true);
      });

      it('should have correct Russian error message', () => {
        expect(ValidationRules.general.required.message).toBe('Заполните поле');
      });
    });

    describe('shortPassword', () => {
      it('should have required flag set to true', () => {
        expect(ValidationRules.general.shortPassword.required).toBe(true);
      });

      it('should have correct password length error message', () => {
        expect(ValidationRules.general.shortPassword.message).toBe('Пароль должен иметь минимум 8 символов');
      });
    });

    describe('alphaNum', () => {
      it('should validate alphanumeric with underscore', () => {
        const rule = ValidationRules.general.alphaNum;
        expect(rule.pattern).toBeDefined();
        expect(rule.pattern?.test('ABC123')).toBe(true);
        expect(rule.pattern?.test('test123')).toBe(true);
        expect(rule.pattern?.test('Test_123')).toBe(true);
      });

      it('should reject special characters', () => {
        const rule = ValidationRules.general.alphaNum;
        expect(rule.pattern?.test('test!')).toBe(false);
        expect(rule.pattern?.test('test@')).toBe(false);
        expect(rule.pattern?.test('test.')).toBe(false);
        expect(rule.pattern?.test('test ')).toBe(false);
      });

      it('should have correct Russian error message', () => {
        expect(ValidationRules.general.alphaNum.message).toBe('Поле должно состоять из букв, цифр и символов подчеркивания');
      });
    });

    describe('alphaNumWithSpaces', () => {
      it('should validate alphanumeric with spaces and underscore', () => {
        const rule = ValidationRules.general.alphaNumWithSpaces;
        expect(rule.pattern).toBeDefined();
        expect(rule.pattern?.test('ABC 123')).toBe(true);
        expect(rule.pattern?.test('test 123')).toBe(true);
        expect(rule.pattern?.test('Test_ 123')).toBe(true);
      });

      it('should reject special characters other than space and underscore', () => {
        const rule = ValidationRules.general.alphaNumWithSpaces;
        expect(rule.pattern?.test('test!')).toBe(false);
        expect(rule.pattern?.test('test@')).toBe(false);
      });

      it('should have correct Russian error message', () => {
        expect(ValidationRules.general.alphaNumWithSpaces.message).toBe(
          'Поле должно состоять из букв, цифр, пробелов и символов подчеркивания'
        );
      });
    });

    describe('nameRule', () => {
      it('should validate names with letters only', () => {
        const rule = ValidationRules.general.nameRule;
        expect(rule.pattern).toBeDefined();
        expect(rule.pattern?.test('Иван')).toBe(true);
        expect(rule.pattern?.test('John')).toBe(true);
        expect(rule.pattern?.test('Анна-Мария')).toBe(true);
      });

      it('should reject names with numbers or spaces', () => {
        const rule = ValidationRules.general.nameRule;
        expect(rule.pattern?.test('Иван123')).toBe(false);
        expect(rule.pattern?.test('Иван Петров')).toBe(false);
      });

      it('should have correct Russian error message', () => {
        expect(ValidationRules.general.nameRule.message).toBe('Поле должно содержать только буквы');
      });
    });

    describe('patronymicRule', () => {
      it('should validate patronymics with letters, spaces, and hyphens', () => {
        const rule = ValidationRules.general.patronymicRule;
        expect(rule.pattern).toBeDefined();
        expect(rule.pattern?.test('Иванович')).toBe(true);
        expect(rule.pattern?.test('Алексеевна')).toBe(true);
        expect(rule.pattern?.test('де ла Круз')).toBe(true);
        expect(rule.pattern?.test('ван-дер Валь')).toBe(true);
      });

      it('should reject patronymics with numbers', () => {
        const rule = ValidationRules.general.patronymicRule;
        expect(rule.pattern?.test('Иван123')).toBe(false);
        expect(rule.pattern?.test('Алекс!')).toBe(false);
      });

      it('should have correct Russian error message', () => {
        expect(ValidationRules.general.patronymicRule.message).toBe(
          'Поле должно содержать только буквы, пробелы и дефисы'
        );
      });
    });

    describe('minMaxLength', () => {
      it('should return function that creates min/max rule', () => {
        const rule = ValidationRules.general.minMaxLength(2, 10)();
        expect(rule.min).toBe(2);
        expect(rule.max).toBe(10);
      });

      it('should work with different min/max values', () => {
        const rule = ValidationRules.general.minMaxLength(5, 50)();
        expect(rule.min).toBe(5);
        expect(rule.max).toBe(50);
      });
    });

    describe('email', () => {
      it('should resolve for empty/undefined values', async () => {
        const rule = ValidationRules.general.email();
        await expect(rule.validator?.({} as any, '', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, undefined, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, null as any, () => {})).resolves.toBeUndefined();
      });

      it('should resolve for valid email addresses', async () => {
        const rule = ValidationRules.general.email();
        await expect(rule.validator?.({} as any, 'test@example.com', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'user.name@domain.co.uk', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'user+tag@example.org', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'test@sub.domain.com', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'test@domain.museum', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'test@sub.domain.museum', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'test@123.com', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'test@domain.рф', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'test@доmain.рф', () => {})).resolves.toBeUndefined();
      });

      it('should reject for invalid email addresses', async () => {
        const rule = ValidationRules.general.email();
        await expect(rule.validator?.({} as any, 'invalid.email', () => {})).rejects.toThrow('Введите корректный e-mail');
        await expect(rule.validator?.({} as any, '@example.com', () => {})).rejects.toThrow('Введите корректный e-mail');
        await expect(rule.validator?.({} as any, 'user@', () => {})).rejects.toThrow('Введите корректный e-mail');
        await expect(rule.validator?.({} as any, 'test@domain.toolongtld', () => {})).rejects.toThrow('Введите корректный e-mail');
      });
    });

    describe('onlyDigits', () => {
      it('should resolve for empty/undefined values', async () => {
        const rule = ValidationRules.general.onlyDigits();
        await expect(rule.validator?.({} as any, '', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, undefined, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, null as any, () => {})).resolves.toBeUndefined();
      });

      it('should resolve for valid digit strings', async () => {
        const rule = ValidationRules.general.onlyDigits();
        await expect(rule.validator?.({} as any, '0', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '123', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '000', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '999999', () => {})).resolves.toBeUndefined();
      });

      it('should reject for non-digit strings', async () => {
        const rule = ValidationRules.general.onlyDigits();
        await expect(rule.validator?.({} as any, 'abc', () => {})).rejects.toThrow('Может содержать только цифры!');
        await expect(rule.validator?.({} as any, '123abc', () => {})).rejects.toThrow('Может содержать только цифры!');
        await expect(rule.validator?.({} as any, '12.3', () => {})).rejects.toThrow('Может содержать только цифры!');
        await expect(rule.validator?.({} as any, '-123', () => {})).rejects.toThrow('Может содержать только цифры!');
        await expect(rule.validator?.({} as any, ' ', () => {})).rejects.toThrow('Может содержать только цифры!');
      });
    });

    describe('floatDecimal', () => {
      it('should resolve for empty/undefined values', async () => {
        const rule = ValidationRules.general.floatDecimal();
        await expect(rule.validator?.({} as any, '', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, undefined, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, null as any, () => {})).resolves.toBeUndefined();
      });

      it('should resolve for valid decimal numbers', async () => {
        const rule = ValidationRules.general.floatDecimal();
        await expect(rule.validator?.({} as any, '123', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '0', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '123.4', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '0.5', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '999.9', () => {})).resolves.toBeUndefined();
      });

      it('should reject for invalid decimal numbers', async () => {
        const rule = ValidationRules.general.floatDecimal();
        await expect(rule.validator?.({} as any, '123.45', () => {})).rejects.toThrow(
          'Может содержать только числа с одним знаком после запятой!'
        );
        await expect(rule.validator?.({} as any, 'abc', () => {})).rejects.toThrow(
          'Может содержать только числа с одним знаком после запятой!'
        );
        await expect(rule.validator?.({} as any, '12.345', () => {})).rejects.toThrow(
          'Может содержать только числа с одним знаком после запятой!'
        );
        await expect(rule.validator?.({} as any, '-12.3', () => {})).rejects.toThrow(
          'Может содержать только числа с одним знаком после запятой!'
        );
      });
    });

    describe('floatTwoSymbols', () => {
      it('should resolve for empty/undefined values', async () => {
        const rule = ValidationRules.general.floatTwoSymbols();
        await expect(rule.validator?.({} as any, '', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, undefined, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, null as any, () => {})).resolves.toBeUndefined();
      });

      it('should resolve for valid decimal numbers with 1-2 decimal places', async () => {
        const rule = ValidationRules.general.floatTwoSymbols();
        await expect(rule.validator?.({} as any, '123', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '123.4', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '123.45', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '0.99', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '0.01', () => {})).resolves.toBeUndefined();
      });

      it('should reject for invalid decimal numbers', async () => {
        const rule = ValidationRules.general.floatTwoSymbols();
        await expect(rule.validator?.({} as any, '123.456', () => {})).rejects.toThrow(
          'Может содержать только числа с одним или двумя знаками после запятой!'
        );
        await expect(rule.validator?.({} as any, 'abc', () => {})).rejects.toThrow(
          'Может содержать только числа с одним или двумя знаками после запятой!'
        );
        await expect(rule.validator?.({} as any, '-12.34', () => {})).rejects.toThrow(
          'Может содержать только числа с одним или двумя знаками после запятой!'
        );
      });
    });

    describe('validationFloatingNumbers', () => {
      it('should resolve for empty/undefined values', async () => {
        const rule = ValidationRules.general.validationFloatingNumbers();
        await expect(rule.validator?.({} as any, '', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, undefined, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, null as any, () => {})).resolves.toBeUndefined();
      });

      it('should resolve for valid integer numbers', async () => {
        const rule = ValidationRules.general.validationFloatingNumbers();
        await expect(rule.validator?.({} as any, '0', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '123', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '999999', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '-123', () => {})).resolves.toBeUndefined();
      });

      it('should reject for decimal numbers', async () => {
        const rule = ValidationRules.general.validationFloatingNumbers();
        await expect(rule.validator?.({} as any, '123.4', () => {})).rejects.toThrow('Может содержать только целые числа');
        await expect(rule.validator?.({} as any, '12.34', () => {})).rejects.toThrow('Может содержать только целые числа');
        await expect(rule.validator?.({} as any, '1.5', () => {})).rejects.toThrow('Может содержать только целые числа');
      });
    });

    describe('positiveNumbers', () => {
      it('should resolve for null/undefined values', async () => {
        const rule = ValidationRules.general.positiveNumbers();
        await expect(rule.validator?.({} as any, null as any, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, undefined, () => {})).resolves.toBeUndefined();
      });

      it('should resolve for valid positive integers', async () => {
        const rule = ValidationRules.general.positiveNumbers();
        await expect(rule.validator?.({} as any, '1', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '123', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '999999', () => {})).resolves.toBeUndefined();
      });

      it('should reject for zero, negative numbers, decimals, and non-digits', async () => {
        const rule = ValidationRules.general.positiveNumbers();
        await expect(rule.validator?.({} as any, '0', () => {})).rejects.toThrow('Может содержать только целые числа больше нуля');
        await expect(rule.validator?.({} as any, '-1', () => {})).rejects.toThrow('Может содержать только целые числа больше нуля');
        await expect(rule.validator?.({} as any, '-123', () => {})).rejects.toThrow('Может содержать только целые числа больше нуля');
        await expect(rule.validator?.({} as any, '1.5', () => {})).rejects.toThrow('Может содержать только целые числа больше нуля');
        await expect(rule.validator?.({} as any, '01', () => {})).rejects.toThrow('Может содержать только целые числа больше нуля');
        await expect(rule.validator?.({} as any, 'abc', () => {})).rejects.toThrow('Может содержать только целые числа больше нуля');
      });
    });

    describe('checkPhoneMask', () => {
      it('should resolve for empty/undefined values', async () => {
        const rule = ValidationRules.general.checkPhoneMask();
        await expect(rule.validator?.({} as any, '', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, undefined, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, null as any, () => {})).resolves.toBeUndefined();
      });

      it('should resolve for valid phone numbers in international format', async () => {
        const rule = ValidationRules.general.checkPhoneMask();
        await expect(rule.validator?.({} as any, '+7 (999) 999-99-99', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '+7 (123) 456-78-90', () => {})).resolves.toBeUndefined();
      });

      it('should reject for invalid phone number formats', async () => {
        const rule = ValidationRules.general.checkPhoneMask();
        await expect(rule.validator?.({} as any, '+79999999999', () => {})).rejects.toThrow(
          'Маска ввода должна соответствовать формату +7 (999) 999-99-99'
        );
        await expect(rule.validator?.({} as any, '8 (999) 999-99-99', () => {})).rejects.toThrow(
          'Маска ввода должна соответствовать формату +7 (999) 999-99-99'
        );
        await expect(rule.validator?.({} as any, '9999999999', () => {})).rejects.toThrow(
          'Маска ввода должна соответствовать формату +7 (999) 999-99-99'
        );
      });
    });

    describe('checkPhoneNumber', () => {
      it('should resolve for empty/undefined values', async () => {
        const rule = ValidationRules.general.checkPhoneNumber();
        await expect(rule.validator?.({} as any, '', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, undefined, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, null as any, () => {})).resolves.toBeUndefined();
      });

      it('should resolve for valid domestic phone format', async () => {
        const rule = ValidationRules.general.checkPhoneNumber();
        await expect(rule.validator?.({} as any, '+7 (999) 999-99-99', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '+7 (123) 456-78-90', () => {})).resolves.toBeUndefined();
      });

      it('should reject for invalid phone number formats', async () => {
        const rule = ValidationRules.general.checkPhoneNumber();
        await expect(rule.validator?.({} as any, '+79999999999', () => {})).rejects.toThrow(
          'Номер телефона должен иметь следующий формат 8 (999) 999-99-99'
        );
        await expect(rule.validator?.({} as any, '8 (999) 999-99-99', () => {})).rejects.toThrow(
          'Номер телефона должен иметь следующий формат 8 (999) 999-99-99'
        );
        await expect(rule.validator?.({} as any, '9999999999', () => {})).rejects.toThrow(
          'Номер телефона должен иметь следующий формат 8 (999) 999-99-99'
        );
        await expect(rule.validator?.({} as any, '+7 (999) 999-999', () => {})).rejects.toThrow(
          'Номер телефона должен иметь следующий формат 8 (999) 999-99-99'
        );
      });
    });

    describe('maskRegEx', () => {
      it('should resolve for empty/undefined values', async () => {
        const rule = ValidationRules.general.maskRegEx();
        await expect(rule.validator?.({} as any, '', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, undefined, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, null as any, () => {})).resolves.toBeUndefined();
      });

      it('should resolve for valid 12-character masks', async () => {
        const rule = ValidationRules.general.maskRegEx();
        await expect(rule.validator?.({} as any, '123456789012', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'ABCDEF123456', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'AB12CD34EF56', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'aB12cD34eF56', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '!@.,;:\'?"-12', () => {})).resolves.toBeUndefined();
      });

      it('should reject for invalid mask formats', async () => {
        const rule = ValidationRules.general.maskRegEx();
        await expect(rule.validator?.({} as any, '12345678901', () => {})).rejects.toThrow(
          'Допустим ввод только цифр. Длина ИНН должна быть 12 символов'
        );
        await expect(rule.validator?.({} as any, '1234567890123', () => {})).rejects.toThrow(
          'Допустим ввод только цифр. Длина ИНН должна быть 12 символов'
        );
        await expect(rule.validator?.({} as any, 'ABCD EFGHIJKL', () => {})).rejects.toThrow(
          'Допустим ввод только цифр. Длина ИНН должна быть 12 символов'
        );
      });
    });

    describe('maxLength', () => {
      it('should create validator with specified max length', async () => {
        const rule = ValidationRules.general.maxLength(10)();
        expect(rule.validator).toBeDefined();

        // Test with 5 characters (should pass)
        await expect(rule.validator?.({} as any, '12345', () => {})).resolves.toBeUndefined();
        // Test with 10 characters (should pass)
        await expect(rule.validator?.({} as any, '1234567890', () => {})).resolves.toBeUndefined();
        // Test with 11 characters (should fail)
        await expect(rule.validator?.({} as any, '12345678901', () => {})).rejects.toThrow('Не более 10 символов');
      });

      it('should work with different max lengths', async () => {
        const rule = ValidationRules.general.maxLength(5)();
        await expect(rule.validator?.({} as any, '12345', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '123456', () => {})).rejects.toThrow('Не более 5 символов');
      });
    });

    describe('minLength', () => {
      it('should create validator with specified min length', async () => {
        const rule = ValidationRules.general.minLength(5)();
        expect(rule.validator).toBeDefined();

        // Test with 5 characters (should pass)
        await expect(rule.validator?.({} as any, '12345', () => {})).resolves.toBeUndefined();
        // Test with 6 characters (should pass)
        await expect(rule.validator?.({} as any, '123456', () => {})).resolves.toBeUndefined();
        // Test with 4 characters (should fail)
        await expect(rule.validator?.({} as any, '1234', () => {})).rejects.toThrow('Не менее 5 символов');
      });

      it('should work with different min lengths', async () => {
        const rule = ValidationRules.general.minLength(10)();
        await expect(rule.validator?.({} as any, '1234567890', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '123456789', () => {})).rejects.toThrow('Не менее 10 символов');
      });
    });

    describe('max', () => {
      it('should create validator with specified max value', async () => {
        const rule = ValidationRules.general.max(100)();
        expect(rule.validator).toBeDefined();

        // Test with values below max (should pass)
        await expect(rule.validator?.({} as any, '0', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '50', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '100', () => {})).resolves.toBeUndefined();
        // Test with value above max (should fail)
        await expect(rule.validator?.({} as any, '101', () => {})).rejects.toThrow('Не более 100');
      });

      it('should work with decimal values', async () => {
        const rule = ValidationRules.general.max(10.5)();
        await expect(rule.validator?.({} as any, '10.5', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '10.4', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '10.6', () => {})).rejects.toThrow('Не более 10.5');
      });
    });

    describe('maxInt', () => {
      it('should use MAX_INT for general fields', async () => {
        const rule = ValidationRules.general.maxInt()();
        await expect(rule.validator?.({} as any, MAX_INT.toString(), () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, (MAX_INT + 1).toString(), () => {})).rejects.toThrow(`Не более ${MAX_INT}`);
      });

      it('should use MAX_INT_COST for CostField fields', async () => {
        const rule = ValidationRules.general.maxInt(CostField.factCost)();
        await expect(rule.validator?.({} as any, MAX_INT_COST.toString(), () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, (MAX_INT_COST + 1).toString(), () => {})).rejects.toThrow(`Не более ${MAX_INT_COST} руб.`);
      });

      it('should use MAX_INT_TIME for TimeFields fields', async () => {
        const rule = ValidationRules.general.maxInt(TimeFields.waypointWaitTime)();
        await expect(rule.validator?.({} as any, MAX_INT_TIME.toString(), () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, (MAX_INT_TIME + 0.01).toString(), () => {})).rejects.toThrow(
          `Не более ${MAX_INT_TIME} мин.`
        );
      });

      it('should use current year for YearFields fields', async () => {
        const currentYear = moment().format('gggg');
        const rule = ValidationRules.general.maxInt(YearFields.manufactureYear)();
        await expect(rule.validator?.({} as any, currentYear, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, (parseInt(currentYear) + 1).toString(), () => {})).rejects.toThrow(
          `Не более ${currentYear} года`
        );
      });
    });

    describe('minInt', () => {
      it('should use -MAX_INT for general fields', async () => {
        const rule = ValidationRules.general.minInt()();
        await expect(rule.validator?.({} as any, (-MAX_INT).toString(), () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, (-MAX_INT - 1).toString(), () => {})).rejects.toThrow(`Не менее ${-MAX_INT}`);
      });

      it('should use MIN_VEHICLE_MANUFACTURED_YEAR for YearFields fields', async () => {
        const rule = ValidationRules.general.minInt(YearFields.manufactureYear)();
        await expect(rule.validator?.(
          {} as any, MIN_VEHICLE_MANUFACTURED_YEAR.toString(), () => {})
        ).resolves.toBeUndefined();
        await expect(rule.validator?.(
          {} as any, (MIN_VEHICLE_MANUFACTURED_YEAR - 1).toString(), () => {})
        ).rejects.toThrow(
          `Не менее ${MIN_VEHICLE_MANUFACTURED_YEAR} года`
        );
        await expect(rule.validator?.({} as any, '2000', () => {})).resolves.toBeUndefined();
      });
    });

    describe('passportNumberRule', () => {
      it('should resolve for empty/undefined values', async () => {
        const rule = ValidationRules.general.passportNumberRule();
        await expect(rule.validator?.({} as any, '', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, undefined, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, null as any, () => {})).resolves.toBeUndefined();
      });

      it('should resolve for valid passport number format', async () => {
        const rule = ValidationRules.general.passportNumberRule();
        await expect(rule.validator?.({} as any, '1234 123456', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '9876 543210', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '0000 000000', () => {})).resolves.toBeUndefined();
      });

      it('should reject for invalid passport number formats', async () => {
        const rule = ValidationRules.general.passportNumberRule();
        await expect(rule.validator?.({} as any, '1234567890', () => {})).rejects.toThrow(
          'Номер паспорта должен иметь следующий формат 1234 123456'
        );
        await expect(rule.validator?.({} as any, '1234-123456', () => {})).rejects.toThrow(
          'Номер паспорта должен иметь следующий формат 1234 123456'
        );
        await expect(rule.validator?.({} as any, '123 123456', () => {})).rejects.toThrow(
          'Номер паспорта должен иметь следующий формат 1234 123456'
        );
        await expect(rule.validator?.({} as any, '12345 123456', () => {})).rejects.toThrow(
          'Номер паспорта должен иметь следующий формат 1234 123456'
        );
      });
    });

    describe('driverLicenseRule', () => {
      it('should resolve for empty/undefined values', async () => {
        const rule = ValidationRules.general.driverLicenseRule();
        await expect(rule.validator?.({} as any, '', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, undefined, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, null as any, () => {})).resolves.toBeUndefined();
      });

      it('should resolve for valid driver license format', async () => {
        const rule = ValidationRules.general.driverLicenseRule();
        await expect(rule.validator?.({} as any, '12 12 123456', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '99 99 987654', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '00 00 000000', () => {})).resolves.toBeUndefined();
      });

      it('should reject for invalid driver license formats', async () => {
        const rule = ValidationRules.general.driverLicenseRule();
        await expect(rule.validator?.({} as any, '1212123456', () => {})).rejects.toThrow(
          'Номер водительских прав должен иметь следующий формат 12 12 123456'
        );
        await expect(rule.validator?.({} as any, '12-12-123456', () => {})).rejects.toThrow(
          'Номер водительских прав должен иметь следующий формат 12 12 123456'
        );
        await expect(rule.validator?.({} as any, '1 12 123456', () => {})).rejects.toThrow(
          'Номер водительских прав должен иметь следующий формат 12 12 123456'
        );
        await expect(rule.validator?.({} as any, '123 12 123456', () => {})).rejects.toThrow(
          'Номер водительских прав должен иметь следующий формат 12 12 123456'
        );
      });
    });

    describe('serviceLicenseRule', () => {
      it('should resolve for empty/undefined values', async () => {
        const rule = ValidationRules.general.serviceLicenseRule();
        await expect(rule.validator?.({} as any, '', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, undefined, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, null as any, () => {})).resolves.toBeUndefined();
      });

      it('should resolve for valid service license format', async () => {
        const rule = ValidationRules.general.serviceLicenseRule();
        await expect(rule.validator?.({} as any, 'ABC-12-123456', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'XYZ-99-987654', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'aaa-00-000000', () => {})).resolves.toBeUndefined();
      });

      it('should reject for invalid service license formats', async () => {
        const rule = ValidationRules.general.serviceLicenseRule();
        await expect(rule.validator?.({} as any, 'ABC12123456', () => {})).rejects.toThrow(
          'Номер лицензии должен иметь следующий формат AAA-12-123456, где A - латинская буква/цифра'
        );
        await expect(rule.validator?.({} as any, 'ABC-12-12345', () => {})).rejects.toThrow(
          'Номер лицензии должен иметь следующий формат AAA-12-123456, где A - латинская буква/цифра'
        );
        await expect(rule.validator?.({} as any, 'AB-12-123456', () => {})).rejects.toThrow(
          'Номер лицензии должен иметь следующий формат AAA-12-123456, где A - латинская буква/цифра'
        );
      });
    });

    describe('checkStateNumber', () => {
      it('should resolve for empty/undefined values', async () => {
        const rule = ValidationRules.general.checkStateNumber();
        await expect(rule.validator?.({} as any, '', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, undefined, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, null as any, () => {})).resolves.toBeUndefined();
      });

      it('should resolve for valid state number format', async () => {
        const rule = ValidationRules.general.checkStateNumber();
        await expect(rule.validator?.({} as any, 'A123AA 123 RUS', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'X999XX 999 rus', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'A123AA123RUS', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'A123AA123rus', () => {})).resolves.toBeUndefined();
      });

      it('should handle underscores in input', async () => {
        const rule = ValidationRules.general.checkStateNumber();
        await expect(rule.validator?.({} as any, 'A123AA_123_RUS', () => {})).resolves.toBeUndefined();
      });

      it('should reject for invalid state number formats', async () => {
        const rule = ValidationRules.general.checkStateNumber();
        await expect(rule.validator?.({} as any, '123ABC123RUS', () => {})).rejects.toThrow(
          'Регистрационный знак должен иметь следующий формат: A123AA 123 RUS'
        );
        await expect(rule.validator?.({} as any, 'A123A 123 RUS', () => {})).rejects.toThrow(
          'Регистрационный знак должен иметь следующий формат: A123AA 123 RUS'
        );
      });
    });

    describe('checkSemitrailerNumber', () => {
      it('should resolve for empty/undefined values', async () => {
        const rule = ValidationRules.general.checkSemitrailerNumber();
        await expect(rule.validator?.({} as any, '', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, undefined, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, null as any, () => {})).resolves.toBeUndefined();
      });

      it('should resolve for valid semitrailer number format', async () => {
        const rule = ValidationRules.general.checkSemitrailerNumber();
        await expect(rule.validator?.({} as any, 'AA12345 123 RUS', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'XX99999 999 rus', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'AA12345123RUS', () => {})).resolves.toBeUndefined();
      });

      it('should handle underscores in input', async () => {
        const rule = ValidationRules.general.checkSemitrailerNumber();
        await expect(rule.validator?.({} as any, 'AA12345_123_RUS', () => {})).resolves.toBeUndefined();
      });

      it('should reject for invalid semitrailer number formats', async () => {
        const rule = ValidationRules.general.checkSemitrailerNumber();
        await expect(rule.validator?.({} as any, 'A123456 123 RUS', () => {})).rejects.toThrow(
          'Регистрационный знак должен иметь следующий формат: AА12345 123 RUS'
        );
        await expect(rule.validator?.({} as any, 'A12345 123 RUS', () => {})).rejects.toThrow(
          'Регистрационный знак должен иметь следующий формат: AА12345 123 RUS'
        );
      });
    });

    describe('checkVin', () => {
      it('should resolve for empty/undefined values', async () => {
        const rule = ValidationRules.general.checkVin();
        await expect(rule.validator?.({} as any, '', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, undefined, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, null as any, () => {})).resolves.toBeUndefined();
      });

      it('should resolve for valid VIN formats', async () => {
        const rule = ValidationRules.general.checkVin();
        await expect(rule.validator?.({} as any, 'ABC123456A1234567', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'XYZ789012X3456789', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'ABC-123456-A-1234567', () => {})).resolves.toBeUndefined();
      });

      it('should reject for invalid VIN formats', async () => {
        const rule = ValidationRules.general.checkVin();
        await expect(rule.validator?.({} as any, 'ABC123456A123456', () => {})).rejects.toThrow(
          'Идентификационный номер должен иметь следующий формат AAA-AAAAAA-A-AAAAAAA, где A - латинская буква/цифра'
        );
        await expect(rule.validator?.({} as any, 'ABCDEFGHIA1234567', () => {})).rejects.toThrow(
          'Идентификационный номер должен иметь следующий формат AAA-AAAAAA-A-AAAAAAA, где A - латинская буква/цифра'
        );
      });
    });

    describe('checkInsuranceNumber', () => {
      it('should resolve for empty/undefined values', async () => {
        const rule = ValidationRules.general.checkInsuranceNumber();
        await expect(rule.validator?.({} as any, '', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, undefined, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, null as any, () => {})).resolves.toBeUndefined();
      });

      it('should resolve for valid insurance number formats', async () => {
        const rule = ValidationRules.general.checkInsuranceNumber();
        await expect(rule.validator?.({} as any, 'ABC 1234567890', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'XYZ 0987654321', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'ABC1234567890', () => {})).resolves.toBeUndefined();
      });

      it('should reject for invalid insurance number formats', async () => {
        const rule = ValidationRules.general.checkInsuranceNumber();
        await expect(rule.validator?.({} as any, 'AB 1234567890', () => {})).rejects.toThrow(
          'Полис страхования должен иметь следующий формат AAA 1234567890, где A - латинская буква'
        );
        await expect(rule.validator?.({} as any, 'ABCD 1234567890', () => {})).rejects.toThrow(
          'Полис страхования должен иметь следующий формат AAA 1234567890, где A - латинская буква'
        );
        await expect(rule.validator?.({} as any, 'ABC 123456789', () => {})).rejects.toThrow(
          'Полис страхования должен иметь следующий формат AAA 1234567890, где A - латинская буква'
        );
      });
    });

    describe('checkVehiclePassport', () => {
      it('should resolve for empty/undefined values', async () => {
        const rule = ValidationRules.general.checkVehiclePassport();
        await expect(rule.validator?.({} as any, '', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, undefined, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, null as any, () => {})).resolves.toBeUndefined();
      });

      it('should resolve for valid vehicle passport formats', async () => {
        const rule = ValidationRules.general.checkVehiclePassport();
        await expect(rule.validator?.({} as any, '12 AB 123456', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '99 XY 987654', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '00 AA 000000', () => {})).resolves.toBeUndefined();
      });

      it('should reject for invalid vehicle passport formats', async () => {
        const rule = ValidationRules.general.checkVehiclePassport();
        await expect(rule.validator?.({} as any, '123AB123456', () => {})).rejects.toThrow(
          'Паспорт транспортного средства должен иметь следующий формат 12 AA 123456, где A - латинская буква'
        );
        await expect(rule.validator?.({} as any, '12A123456', () => {})).rejects.toThrow(
          'Паспорт транспортного средства должен иметь следующий формат 12 AA 123456, где A - латинская буква'
        );
        await expect(rule.validator?.({} as any, '12 AB 12345', () => {})).rejects.toThrow(
          'Паспорт транспортного средства должен иметь следующий формат 12 AA 123456, где A - латинская буква'
        );
      });
    });

    describe('minManufactureYear', () => {
      it('should create validator with specified minimum year', async () => {
        const rule = ValidationRules.general.minManufactureYear(2000)();
        expect(rule.validator).toBeDefined();

        // Test with year below minimum (should fail)
        await expect(rule.validator?.({} as any, '1999', () => {})).rejects.toThrow('Год производства не ранее 2000 года');
        // Test with year at minimum (should pass)
        await expect(rule.validator?.({} as any, '2000', () => {})).resolves.toBeUndefined();
        // Test with year above minimum (should pass)
        await expect(rule.validator?.({} as any, '2024', () => {})).resolves.toBeUndefined();
      });

      it('should work with different minimum years', async () => {
        const rule = ValidationRules.general.minManufactureYear(1900)();
        await expect(rule.validator?.({} as any, '1900', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '1899', () => {})).rejects.toThrow('Год производства не ранее 1900 года');
      });
    });

    describe('validatorStartDate', () => {
      it('should resolve when end date is not set', async () => {
        const form = createMockForm({});
        const rule = ValidationRules.general.validatorStartDate(form, 'endDate');
        await expect(rule.validator?.({} as any, moment(), () => {})).resolves.toBeUndefined();
      });

      it('should resolve when start date is before end date (isSameDay=false)', async () => {
        const form = createMockForm({ endDate: moment('2024-01-02') });
        const rule = ValidationRules.general.validatorStartDate(form, 'endDate', false);
        await expect(rule.validator?.({} as any, moment('2024-01-01'), () => {})).resolves.toBeUndefined();
      });

      it('should resolve when start date equals end date (isSameDay=true)', async () => {
        const form = createMockForm({ endDate: moment('2024-01-01') });
        const rule = ValidationRules.general.validatorStartDate(form, 'endDate', true);
        await expect(rule.validator?.({} as any, moment('2024-01-01'), () => {})).resolves.toBeUndefined();
      });

      it('should reject when start date is after end date (isSameDay=false)', async () => {
        const form = createMockForm({ endDate: moment('2024-01-01') });
        const rule = ValidationRules.general.validatorStartDate(form, 'endDate', false);
        await expect(rule.validator?.({} as any, moment('2024-01-02'), () => {})).rejects.toThrow(
          'Дата начала не может быть раньше конечной даты'
        );
      });

      it('should reject when start date equals end date (isSameDay=false)', async () => {
        const form = createMockForm({ endDate: moment('2024-01-01') });
        const rule = ValidationRules.general.validatorStartDate(form, 'endDate', false);
        await expect(rule.validator?.({} as any, moment('2024-01-01'), () => {})).rejects.toThrow(
          'Дата начала не может быть раньше конечной даты'
        );
      });
    });

    describe('validatorEndDate', () => {
      it('should resolve when start date is not set', async () => {
        const form = createMockForm({});
        const rule = ValidationRules.general.validatorEndDate(form, 'startDate');
        await expect(rule.validator?.({} as any, moment(), () => {})).resolves.toBeUndefined();
      });

      it('should resolve when end date is before start date + diff', async () => {
        const form = createMockForm({ startDate: moment('2024-01-01') });
        const rule = ValidationRules.general.validatorEndDate(form, 'startDate', [1, 'month']);
        await expect(rule.validator?.({} as any, moment('2024-01-31'), () => {})).resolves.toBeUndefined();
      });

      it('should reject when end date equals or exceeds start date + diff', async () => {
        const form = createMockForm({ startDate: moment('2024-01-01') });
        const rule = ValidationRules.general.validatorEndDate(form, 'startDate', [1, 'month']);
        await expect(rule.validator?.({} as any, moment('2024-02-01'), () => {})).rejects.toThrow('Превышен допустимый диапазон');
        await expect(rule.validator?.({} as any, moment('2024-02-15'), () => {})).rejects.toThrow('Превышен допустимый диапазон');
      });
    });

    describe('maxWaitingTime', () => {
      it('should resolve for empty/undefined values', async () => {
        const rule = ValidationRules.general.maxWaitingTime();
        await expect(rule.validator?.({} as any, '', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, undefined, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, null as any, () => {})).resolves.toBeUndefined();
      });

      it('should resolve for valid waiting times (0-500)', async () => {
        const rule = ValidationRules.general.maxWaitingTime();
        await expect(rule.validator?.({} as any, '0', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '100', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '500', () => {})).resolves.toBeUndefined();
      });

      it('should reject for values outside 0-500 range', async () => {
        const rule = ValidationRules.general.maxWaitingTime();
        await expect(rule.validator?.({} as any, '-1', () => {})).rejects.toThrow('Укажите время ожидания от 0 до 500 минут');
        await expect(rule.validator?.({} as any, '501', () => {})).rejects.toThrow('Укажите время ожидания от 0 до 500 минут');
        await expect(rule.validator?.({} as any, 'abc', () => {})).rejects.toThrow('Укажите время ожидания от 0 до 500 минут');
      });
    });

    describe('maxLoadersTime', () => {
      it('should resolve for values within limit', async () => {
        const rule = ValidationRules.general.maxLoadersTime();
        await expect(rule.validator?.({} as any, '0', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '100', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, '500', () => {})).resolves.toBeUndefined();
      });

      it('should reject for values exceeding 500', async () => {
        const rule = ValidationRules.general.maxLoadersTime();
        await expect(rule.validator?.({} as any, '501', () => {})).rejects.toThrow('Максимальное время работы грузчиков 500 минут');
        await expect(rule.validator?.({} as any, '1000', () => {})).rejects.toThrow('Максимальное время работы грузчиков 500 минут');
      });
    });

    describe('checkTrimmedField', () => {
      it('should resolve for empty/undefined values', async () => {
        const rule = ValidationRules.general.checkTrimmedField();
        await expect(rule.validator?.({} as any, '', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, undefined, () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, null as any, () => {})).resolves.toBeUndefined();
      });

      it('should resolve for trimmed fields', async () => {
        const rule = ValidationRules.general.checkTrimmedField();
        await expect(rule.validator?.({} as any, 'hello', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'hello world', () => {})).resolves.toBeUndefined();
        await expect(rule.validator?.({} as any, 'hello world!', () => {})).resolves.toBeUndefined();
      });

      it('should reject for fields with leading or trailing spaces', async () => {
        const rule = ValidationRules.general.checkTrimmedField();
        await expect(rule.validator?.({} as any, ' hello', () => {})).rejects.toThrow(Error('Поле не должно содержать пробелы'));
        await expect(rule.validator?.({} as any, 'hello ', () => {})).rejects.toThrow(Error('Поле не должно содержать пробелы'));
        await expect(rule.validator?.({} as any, ' hello ', () => {})).rejects.toThrow(Error('Поле не должно содержать пробелы'));
      });
    });
  });

  describe('ValidationRules rules', () => {
    it('should contain required rule', () => {
      expect(ValidationRules.general.required).toBeDefined();
      expect(ValidationRules.general.required.required).toBe(true);
    });

    it('should contain shortPassword rule', () => {
      expect(ValidationRules.general.shortPassword).toBeDefined();
      expect(ValidationRules.general.shortPassword.required).toBe(true);
    });

    it('should contain alphaNum rule', () => {
      expect(ValidationRules.general.alphaNum).toBeDefined();
      expect(ValidationRules.general.alphaNum.pattern).toBeDefined();
    });

    it('should contain alphaNumWithSpaces rule', () => {
      expect(ValidationRules.general.alphaNumWithSpaces).toBeDefined();
      expect(ValidationRules.general.alphaNumWithSpaces.pattern).toBeDefined();
    });

    it('should contain nameRule', () => {
      expect(ValidationRules.general.nameRule).toBeDefined();
      expect(ValidationRules.general.nameRule.pattern).toBeDefined();
    });

    it('should contain patronymicRule', () => {
      expect(ValidationRules.general.patronymicRule).toBeDefined();
      expect(ValidationRules.general.patronymicRule.pattern).toBeDefined();
    });

    it('should contain minMaxLength function', () => {
      expect(ValidationRules.general.minMaxLength).toBeInstanceOf(Function);
    });

    it('should contain email function', () => {
      expect(ValidationRules.general.email).toBeInstanceOf(Function);
    });

    it('should contain onlyDigits function', () => {
      expect(ValidationRules.general.onlyDigits).toBeInstanceOf(Function);
    });

    it('should contain floatDecimal function', () => {
      expect(ValidationRules.general.floatDecimal).toBeInstanceOf(Function);
    });

    it('should contain floatTwoSymbols function', () => {
      expect(ValidationRules.general.floatTwoSymbols).toBeInstanceOf(Function);
    });

    it('should contain validationFloatingNumbers function', () => {
      expect(ValidationRules.general.validationFloatingNumbers).toBeInstanceOf(Function);
    });

    it('should contain positiveNumbers function', () => {
      expect(ValidationRules.general.positiveNumbers).toBeInstanceOf(Function);
    });

    it('should contain checkPhoneMask function', () => {
      expect(ValidationRules.general.checkPhoneMask).toBeInstanceOf(Function);
    });

    it('should contain checkPhoneNumber function', () => {
      expect(ValidationRules.general.checkPhoneNumber).toBeInstanceOf(Function);
    });

    it('should contain maskRegEx function', () => {
      expect(ValidationRules.general.maskRegEx).toBeInstanceOf(Function);
    });

    it('should contain maxLength function', () => {
      expect(ValidationRules.general.maxLength).toBeInstanceOf(Function);
    });

    it('should contain minLength function', () => {
      expect(ValidationRules.general.minLength).toBeInstanceOf(Function);
    });

    it('should contain max function', () => {
      expect(ValidationRules.general.max).toBeInstanceOf(Function);
    });

    it('should contain maxInt function', () => {
      expect(ValidationRules.general.maxInt).toBeInstanceOf(Function);
    });

    it('should contain minInt function', () => {
      expect(ValidationRules.general.minInt).toBeInstanceOf(Function);
    });

    it('should contain passportNumberRule function', () => {
      expect(ValidationRules.general.passportNumberRule).toBeInstanceOf(Function);
    });

    it('should contain driverLicenseRule function', () => {
      expect(ValidationRules.general.driverLicenseRule).toBeInstanceOf(Function);
    });

    it('should contain serviceLicenseRule function', () => {
      expect(ValidationRules.general.serviceLicenseRule).toBeInstanceOf(Function);
    });

    it('should contain checkStateNumber function', () => {
      expect(ValidationRules.general.checkStateNumber).toBeInstanceOf(Function);
    });

    it('should contain checkSemitrailerNumber function', () => {
      expect(ValidationRules.general.checkSemitrailerNumber).toBeInstanceOf(Function);
    });

    it('should contain checkVin function', () => {
      expect(ValidationRules.general.checkVin).toBeInstanceOf(Function);
    });

    it('should contain checkInsuranceNumber function', () => {
      expect(ValidationRules.general.checkInsuranceNumber).toBeInstanceOf(Function);
    });

    it('should contain checkVehiclePassport function', () => {
      expect(ValidationRules.general.checkVehiclePassport).toBeInstanceOf(Function);
    });

    it('should contain minManufactureYear function', () => {
      expect(ValidationRules.general.minManufactureYear).toBeInstanceOf(Function);
    });

    it('should contain validatorStartDate function', () => {
      expect(ValidationRules.general.validatorStartDate).toBeInstanceOf(Function);
    });

    it('should contain validatorEndDate function', () => {
      expect(ValidationRules.general.validatorEndDate).toBeInstanceOf(Function);
    });

    it('should contain maxWaitingTime function', () => {
      expect(ValidationRules.general.maxWaitingTime).toBeInstanceOf(Function);
    });

    it('should contain maxLoadersTime function', () => {
      expect(ValidationRules.general.maxLoadersTime).toBeInstanceOf(Function);
    });

    it('should contain checkTrimmedField function', () => {
      expect(ValidationRules.general.checkTrimmedField).toBeInstanceOf(Function);
    });
  });

  describe('matchPattern', () => {
    it('should return a function', () => {
      const handler = matchPattern(/\d/g);
      expect(handler).toBeInstanceOf(Function);
    });

    it('should filter input to match pattern', () => {
      const handler = matchPattern(/\d/g);
      const mockEvent = {
        target: {
          value: 'abc123def',
        },
      } as unknown as React.ChangeEvent<HTMLInputElement>;

      handler(mockEvent);
      expect(mockEvent.target.value).toBe('123');
    });

    it('should handle empty string input', () => {
      const handler = matchPattern(/\d/g);
      const mockEvent = {
        target: {
          value: '',
        },
      } as unknown as React.ChangeEvent<HTMLInputElement>;

      handler(mockEvent);
      expect(mockEvent.target.value).toBe('');
    });

    it('should handle input with no matching characters', () => {
      const handler = matchPattern(/\d/g);
      const mockEvent = {
        target: {
          value: 'abcdef',
        },
      } as unknown as React.ChangeEvent<HTMLInputElement>;

      handler(mockEvent);
      expect(mockEvent.target.value).toBe('');
    });

    it('should work with letter pattern', () => {
      const handler = matchPattern(/[a-zA-Z]/g);
      const mockEvent = {
        target: {
          value: '123abc456def',
        },
      } as unknown as React.ChangeEvent<HTMLInputElement>;

      handler(mockEvent);
      expect(mockEvent.target.value).toBe('abcdef');
    });

    it('should work with alphanumeric pattern', () => {
      const handler = matchPattern(/[a-zA-Z0-9]/g);
      const mockEvent = {
        target: {
          value: 'abc!@#123def',
        },
      } as unknown as React.ChangeEvent<HTMLInputElement>;

      handler(mockEvent);
      expect(mockEvent.target.value).toBe('abc123def');
    });
  });
});

describe('Type Definitions', () => {
  describe('RangeNumber codec', () => {
    it('should validate valid RangeNumber object', () => {
      const valid: RangeNumber = { start: 0, end: 100 };
      const result = RangeNumber.decode(valid);
      expect(result._tag).toBe('Right');
      if (result._tag === 'Right') {
        expect(result.right).toEqual(valid);
      }
    });

    it('should reject missing properties', () => {
      const invalid = { start: 0 };
      const result = RangeNumber.decode(invalid as any);
      expect(result._tag).toBe('Left');
    });

    it('should reject invalid types', () => {
      const invalid = { start: '0', end: 100 };
      const result = RangeNumber.decode(invalid as any);
      expect(result._tag).toBe('Left');
    });
  });

  describe('RangeNumberPartial codec', () => {
    it('should validate valid RangeNumberPartial object with both properties', () => {
      const valid: RangeNumberPartial = { start: 0, end: 100 };
      const result = RangeNumberPartial.decode(valid);
      expect(result._tag).toBe('Right');
    });

    it('should validate RangeNumberPartial with only start', () => {
      const valid = { start: 0 };
      const result = RangeNumberPartial.decode(valid as any);
      expect(result._tag).toBe('Right');
    });

    it('should validate RangeNumberPartial with only end', () => {
      const valid = { end: 100 };
      const result = RangeNumberPartial.decode(valid as any);
      expect(result._tag).toBe('Right');
    });

    it('should validate RangeNumberPartial with neither property', () => {
      const valid = {};
      const result = RangeNumberPartial.decode(valid as any);
      expect(result._tag).toBe('Right');
    });

    it('should reject invalid types', () => {
      const invalid = { start: '0' };
      const result = RangeNumberPartial.decode(invalid as any);
      expect(result._tag).toBe('Left');
    });
  });

  describe('TimeRange codec', () => {
    it('should validate valid TimeRange object', () => {
      const valid: TimeRange = { start: Date.now(), end: Date.now() };
      const result = TimeRange.decode(valid);
      expect(result._tag).toBe('Right');
    });

    it('should reject missing properties', () => {
      const invalid = { start: moment() };
      const result = TimeRange.decode(invalid as any);
      expect(result._tag).toBe('Left');
    });
  });

  describe('CostRange codec', () => {
    it('should validate valid CostRange object', () => {
      const valid: CostRange = { start: 100.5, end: 200.75 };
      const result = CostRange.decode(valid);
      expect(result._tag).toBe('Right');
    });

    it('should reject missing properties', () => {
      const invalid = { start: 100.5 };
      const result = CostRange.decode(invalid as any);
      expect(result._tag).toBe('Left');
    });
  });

  describe('CostField enum', () => {
    it('should have expected values', () => {
      expect(CostField.factCost).toBe('factCost');
      expect(CostField.expectedCost).toBe('expectedCost');
    });

    it('should work with ioTypeFromEnum', () => {
      const costFieldType = ioTypeFromEnum('CostField', CostField);
      expect(costFieldType.is(CostField.factCost)).toBe(true);
      expect(costFieldType.is(CostField.expectedCost)).toBe(true);
      expect(costFieldType.is('invalid' as any)).toBe(false);
    });
  });

  describe('TimeFields enum', () => {
    it('should have expected values', () => {
      expect(TimeFields.waypointWaitTime).toBe('waypointWaitTime');
    });

    it('should work with ioTypeFromEnum', () => {
      const timeFieldType = ioTypeFromEnum('TimeFields', TimeFields);
      expect(timeFieldType.is(TimeFields.waypointWaitTime)).toBe(true);
      expect(timeFieldType.is('invalid' as any)).toBe(false);
    });
  });

  describe('YearFields enum', () => {
    it('should have expected values', () => {
      expect(YearFields.manufactureYear).toBe('manufactureYear');
    });

    it('should work with ioTypeFromEnum', () => {
      const yearFieldType = ioTypeFromEnum('YearFields', YearFields);
      expect(yearFieldType.is(YearFields.manufactureYear)).toBe(true);
      expect(yearFieldType.is('invalid' as any)).toBe(false);
    });
  });
});
