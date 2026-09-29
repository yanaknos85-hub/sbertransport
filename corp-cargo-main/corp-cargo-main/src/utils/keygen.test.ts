import keyGen from './keygen';

describe('keyGen', () => {
  describe('основная функциональность', () => {
    test('должен начинать с "0"', () => {
      const generator = keyGen();
      const result = generator.next();
      expect(result.value).toBe('0');
    });

    test('должен возвращать инкрементированные значения', () => {
      const generator = keyGen();
      
      expect(generator.next().value).toBe('0');
      expect(generator.next().value).toBe('1');
      expect(generator.next().value).toBe('2');
      expect(generator.next().value).toBe('3');
      expect(generator.next().value).toBe('4');
    });

    test('должен работать бесконечно (проверка первых 10 значений)', () => {
      const generator = keyGen();
      const values: string[] = [];
      
      for (let i = 0; i < 10; i++) {
        values.push(generator.next().value);
      }
      
      expect(values).toEqual(['0', '1', '2', '3', '4', '5', '6', '7', '8', '9']);
    });

    test('должен корректно генерировать большие числа', () => {
      const generator = keyGen();
      
      for (let i = 0; i < 100; i++) {
        generator.next();
      }
      
      expect(generator.next().value).toBe('100');
    });

    test('должен генерировать строковые представления чисел', () => {
      const generator = keyGen();
      
      expect(typeof generator.next().value).toBe('string');
      expect(typeof generator.next().value).toBe('string');
      expect(typeof generator.next().value).toBe('string');
    });
  });

  describe('структура генератора', () => {
    test('next().value должно быть строкой', () => {
      const generator = keyGen();
      const result = generator.next();
      
      expect(typeof result.value).toBe('string');
    });

    test('next().done должно быть false (незавершаемый генератор)', () => {
      const generator = keyGen();
      
      for (let i = 0; i < 10; i++) {
        const result = generator.next();
        expect(result.done).toBe(false);
      }
    });

    test('должен возвращать объект с полями value и done', () => {
      const generator = keyGen();
      const result = generator.next();
      
      expect(result).toHaveProperty('value');
      expect(result).toHaveProperty('done');
    });
  });

  describe('изоляция экземпляров', () => {
    test('каждый генератор должен быть независимым', () => {
      const generator1 = keyGen();
      const generator2 = keyGen();
      
      expect(generator1.next().value).toBe('0');
      expect(generator1.next().value).toBe('1');
      
      expect(generator2.next().value).toBe('0');
      expect(generator2.next().value).toBe('1');
      
      expect(generator1.next().value).toBe('2');
      expect(generator1.next().value).toBe('3');
    });

    test('множественные генераторы не должны влиять друг на друга', () => {
      const genA = keyGen();
      const genB = keyGen();
      const genC = keyGen();
      
      genA.next();
      genA.next();
      genA.next();
      
      genB.next();
      
      expect(genA.next().value).toBe('3');
      expect(genB.next().value).toBe('1');
      expect(genC.next().value).toBe('0');
    });
  });

  describe('edge cases', () => {
    test('должен продолжать генерацию после нескольких вызовов next()', () => {
      const generator = keyGen();
      
      generator.next();
      generator.next();
      generator.next();
      generator.next();
      generator.next();
      
      expect(generator.next().value).toBe('5');
    });

    test('должен корректно генерировать значения через ручной вызов next()', () => {
      const generator = keyGen();
      const values: string[] = [];
      
      for (let i = 0; i < 5; i++) {
        values.push(generator.next().value);
      }
      
      expect(values).toEqual(['0', '1', '2', '3', '4']);
    });
  });
});
