import { getFullVehicle } from '../getFullVehicle';

interface Vehicle {
  brand?: string | null;
  model?: string | null;
}

interface ExtendedVehicle extends Vehicle {
  customField?: string;
}

describe('getFullVehicle', () => {
  test('should return full vehicle name with both fields', () => {
    const vehicle = {
      brand: 'Toyota',
      model: 'Camry',
    };
    expect(getFullVehicle(vehicle)).toBe('Toyota Camry');
  });

  test('should return vehicle with brand only', () => {
    const vehicle = {
      brand: 'Toyota',
      model: null,
    };
    expect(getFullVehicle(vehicle)).toBe('Toyota');
  });

  test('should return vehicle with model only', () => {
    const vehicle = {
      brand: null,
      model: 'Camry',
    };
    expect(getFullVehicle(vehicle)).toBe('Camry');
  });

  test('should return empty string for neither field', () => {
    const vehicle = {
      brand: null,
      model: null,
    };
    expect(getFullVehicle(vehicle)).toBe('');
  });

  test('should handle undefined values', () => {
    const vehicle = {
      brand: undefined,
      model: undefined,
    };
    expect(getFullVehicle(vehicle)).toBe('');
  });

  test('should handle null values', () => {
    const vehicle = {
      brand: null,
      model: null,
    };
    expect(getFullVehicle(vehicle)).toBe('');
  });

  test('should handle mixed null and undefined values', () => {
    const vehicle = {
      brand: null,
      model: undefined,
    };
    expect(getFullVehicle(vehicle)).toBe('');
  });

  test('should handle empty strings', () => {
    const vehicle = {
      brand: '',
      model: '',
    };
    expect(getFullVehicle(vehicle)).toBe('');
  });

  test('should handle whitespace-only strings', () => {
    const vehicle = {
      brand: '   ',
      model: '   ',
    };
    expect(getFullVehicle(vehicle)).toBe('       ');
  });

  test('should handle single character brand and model', () => {
    const vehicle = {
      brand: 'T',
      model: 'C',
    };
    expect(getFullVehicle(vehicle)).toBe('T C');
  });

  test('should handle names with special characters', () => {
    const vehicle = {
      brand: 'BMW-M Performance',
      model: 'X5-Drive',
    };
    expect(getFullVehicle(vehicle)).toBe('BMW-M Performance X5-Drive');
  });

  test('should handle Cyrillic brand and model', () => {
    const vehicle = {
      brand: 'ВАЗ',
      model: 'Калина',
    };
    expect(getFullVehicle(vehicle)).toBe('ВАЗ Калина');
  });

  test('should handle Cyrillic with apostrophes', () => {
    const vehicle = {
      brand: 'О\'ПЕК',
      model: 'Д\'Арк',
    };
    expect(getFullVehicle(vehicle)).toBe('О\'ПЕК Д\'Арк');
  });

  test('should handle uppercase brand and model', () => {
    const vehicle = {
      brand: 'TOYOTA',
      model: 'CAMRY',
    };
    expect(getFullVehicle(vehicle)).toBe('TOYOTA CAMRY');
  });

  test('should handle names with numbers', () => {
    const vehicle = {
      brand: 'Brand2023',
      model: 'Model4',
    };
    expect(getFullVehicle(vehicle)).toBe('Brand2023 Model4');
  });

  test('should handle empty object', () => {
    expect(getFullVehicle({})).toBe('');
  });

  test('should handle object with only brand field', () => {
    const vehicle = {
      brand: 'Toyota',
    };
    expect(getFullVehicle(vehicle)).toBe('Toyota');
  });

  test('should handle object with only model field', () => {
    const vehicle = {
      model: 'Camry',
    };
    expect(getFullVehicle(vehicle)).toBe('Camry');
  });

  test('should handle extended vehicle interface with custom field', () => {
    const extendedVehicle: ExtendedVehicle = {
      brand: 'Toyota',
      model: 'Camry',
      customField: 'some value',
    };
    expect(getFullVehicle(extendedVehicle)).toBe('Toyota Camry');
  });

  test('should handle extended vehicle with only required fields', () => {
    const extendedVehicle: ExtendedVehicle = {
      brand: 'Toyota',
      customField: 'some value',
    };
    expect(getFullVehicle(extendedVehicle)).toBe('Toyota');
  });

  test('should handle very long brand and model', () => {
    const longBrand = 'Toyota'.repeat(10);
    const longModel = 'Camry'.repeat(10);
    const vehicle = {
      brand: longBrand,
      model: longModel,
    };
    expect(getFullVehicle(vehicle)).toBe(`${longBrand} ${longModel}`);
  });

  test('should preserve spaces within brand and model', () => {
    const vehicle = {
      brand: 'Toyota Motor',
      model: 'Camry Hybrid',
    };
    expect(getFullVehicle(vehicle)).toBe('Toyota Motor Camry Hybrid');
  });

  test('should handle brand with hyphens', () => {
    const vehicle = {
      brand: 'BMW-M',
      model: 'X5',
    };
    expect(getFullVehicle(vehicle)).toBe('BMW-M X5');
  });

  test('should handle model with hyphens', () => {
    const vehicle = {
      brand: 'Toyota',
      model: 'Camry-Hybrid',
    };
    expect(getFullVehicle(vehicle)).toBe('Toyota Camry-Hybrid');
  });

  test('should handle brand with multiple spaces', () => {
    const vehicle = {
      brand: 'Toyota  Motor',
      model: 'Camry',
    };
    expect(getFullVehicle(vehicle)).toBe('Toyota  Motor Camry');
  });

  test('should handle null brand with valid model', () => {
    const vehicle = {
      brand: null,
      model: 'Camry',
    };
    expect(getFullVehicle(vehicle)).toBe('Camry');
  });

  test('should handle undefined model with valid brand', () => {
    const vehicle = {
      brand: 'Toyota',
      model: undefined,
    };
    expect(getFullVehicle(vehicle)).toBe('Toyota');
  });

  test('should handle empty string brand with valid model', () => {
    const vehicle = {
      brand: '',
      model: 'Camry',
    };
    expect(getFullVehicle(vehicle)).toBe('Camry');
  });

  test('should handle valid brand with empty string model', () => {
    const vehicle = {
      brand: 'Toyota',
      model: '',
    };
    expect(getFullVehicle(vehicle)).toBe('Toyota');
  });
});
