import { RuleObject } from 'antd-mobile/es/components/form';

const clearPhone = (phone: string): string => phone?.replace(/[^+\d]/g, '');

const clearDriverLicense = (driverLicense: string): string => driverLicense?.replace(/\D/g, '');

export const validationPatterns = {
  numberValidation: new RegExp(/[+\d+\s()]{18}/g),
  driverLicenseRegexp: /^\d{2}\s\d{2}\s\d{6}$/,
};

export const ValidationRules = {
  checkingEditingPhoneNumber: (phone: string): RuleObject => ({
    validator: (_: RuleObject, value: string): Promise<void> => clearPhone(value) !== clearPhone(phone)
      ? Promise.resolve()
      : Promise.reject(new Error(`Сохранить возможно только при редактировании номера`)),
  }),
  driverLicenseRule: (driverLicense = ''): RuleObject => ({
    validator: (_: RuleObject, value: string) => {
      if (!value) {
        return Promise.resolve();
      }

      if (clearDriverLicense(value) === clearDriverLicense(driverLicense)) {
        return Promise.reject(new Error(`Сохранить возможно только при редактировании водительского удостоверения`));
      }

      return validationPatterns.driverLicenseRegexp.test(value)
        ? Promise.resolve()
        : Promise.reject('Номер водительского удостоверения должен иметь следующий формат 12 12 123456');
    },
  }),
};
