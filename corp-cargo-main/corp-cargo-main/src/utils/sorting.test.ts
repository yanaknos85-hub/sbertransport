import { stringSorter, defaultSorter, compareBy } from './sorting';

describe('stringSorter', () => {
  describe('основная функциональность', () => {
    test('для одинаковых строк должен вернуть 0', () => {
      expect(stringSorter('apple', 'apple')).toBe(0);
    });

    test('для "apple" и "banana" должен вернуть -1 (a < b)', () => {
      expect(stringSorter('apple', 'banana')).toBe(-1);
    });

    test('для "banana" и "apple" должен вернуть 1 (a > b)', () => {
      expect(stringSorter('banana', 'apple')).toBe(1);
    });

    test('для пустых строк должен вернуть 0', () => {
      expect(stringSorter('', '')).toBe(0);
    });

    test('для пустой строки и "a" должен вернуть -1', () => {
      expect(stringSorter('', 'a')).toBe(-1);
    });

    test('для "a" и пустой строки должен вернуть 1', () => {
      expect(stringSorter('a', '')).toBe(1);
    });
  });

  describe('numeric sorting', () => {
    test('для строк с цифрами "item2" и "item10" должен вернуть -1 (numeric sorting)', () => {
      expect(stringSorter('item2', 'item10')).toBe(-1);
    });

    test('для строк с цифрами "item10" и "item2" должен вернуть 1 (numeric sorting)', () => {
      expect(stringSorter('item10', 'item2')).toBe(1);
    });

    test('для "version2" и "version10" должен вернуть -1', () => {
      expect(stringSorter('version2', 'version10')).toBe(-1);
    });

    test('для "file1" и "file100" должен вернуть -1', () => {
      expect(stringSorter('file1', 'file100')).toBe(-1);
    });

    test('для "100" и "20" должен вернуть 1 (строковое сравнение с numeric: "100" > "20")', () => {
      expect(stringSorter('100', '20')).toBe(1);
    });

    test('для "20" и "100" должен вернуть -1 (строковое сравнение с numeric: "20" < "100")', () => {
      expect(stringSorter('20', '100')).toBe(-1);
    });
  });

  describe('русский язык', () => {
    test('для русских букв "яблоко" и "банан" должен вернуть 1 (я > б в unicode)', () => {
      expect(stringSorter('яблоко', 'банан')).toBe(1);
    });

    test('для русских букв в обратном порядке должен вернуть -1', () => {
      expect(stringSorter('банан', 'яблоко')).toBe(-1);
    });

    test('для "абв" и "где" должен вернуть -1', () => {
      expect(stringSorter('абв', 'где')).toBe(-1);
    });

    test('для одинаковых русских строк должен вернуть 0', () => {
      expect(stringSorter('тест', 'тест')).toBe(0);
    });
  });

  describe('смешанные символы', () => {
    test('для "A" и "a" должен вернуть 1 (заглавные идут после строчных в unicode)', () => {
      expect(stringSorter('A', 'a')).toBe(1);
    });

    test('для "Test1" и "Test2" должен вернуть -1', () => {
      expect(stringSorter('Test1', 'Test2')).toBe(-1);
    });

    test('для "abc123" и "abc124" должен вернуть -1', () => {
      expect(stringSorter('abc123', 'abc124')).toBe(-1);
    });
  });

  describe('спецсимволы и пробелы', () => {
    test('для "a b" и "ab" должен вернуть -1', () => {
      expect(stringSorter('a b', 'ab')).toBe(-1);
    });

    test('для "a-b" и "ab" должен вернуть -1', () => {
      expect(stringSorter('a-b', 'ab')).toBe(-1);
    });
  });
});

describe('defaultSorter', () => {
  describe('строки', () => {
    test('для одинаковых строк должен вернуть 0', () => {
      expect(defaultSorter('apple', 'apple')).toBe(0);
    });

    test('для "apple" и "banana" должен вернуть -1', () => {
      expect(defaultSorter('apple', 'banana')).toBe(-1);
    });

    test('для "banana" и "apple" должен вернуть 1', () => {
      expect(defaultSorter('banana', 'apple')).toBe(1);
    });
  });

  describe('числа', () => {
    test('для одинаковых чисел должен вернуть 0', () => {
      expect(defaultSorter(5, 5)).toBe(0);
    });

    test('для 5 и 10 должен вернуть -1', () => {
      expect(defaultSorter(5, 10)).toBe(-1);
    });

    test('для 10 и 5 должен вернуть 1', () => {
      expect(defaultSorter(10, 5)).toBe(1);
    });

    test('для 0 и 0 должен вернуть 0', () => {
      expect(defaultSorter(0, 0)).toBe(0);
    });

    test('для -5 и 5 должен вернуть -1', () => {
      expect(defaultSorter(-5, 5)).toBe(-1);
    });

    test('для 5 и -5 должен вернуть 1', () => {
      expect(defaultSorter(5, -5)).toBe(1);
    });

    test('для дробных чисел 1.5 и 2.5 должен вернуть -1', () => {
      expect(defaultSorter(1.5, 2.5)).toBe(-1);
    });

    test('для дробных чисел 2.5 и 1.5 должен вернуть 1', () => {
      expect(defaultSorter(2.5, 1.5)).toBe(1);
    });

    test('для дробных чисел 1.5 и 1.5 должен вернуть 0', () => {
      expect(defaultSorter(1.5, 1.5)).toBe(0);
    });
  });

  describe('смешанные типы', () => {
    test('для числа и строки (число < строка) должен вернуть -1', () => {
      expect(defaultSorter(5, '10' as unknown as number)).toBe(-1);
    });

    test('для строки и числа (строка > число) должен вернуть 1', () => {
      expect(defaultSorter('10' as unknown as string, '5')).toBe(1);
    });
  });

  describe('null и undefined', () => {
    test('для undefined и undefined должен вернуть 0', () => {
      expect(defaultSorter(undefined as unknown as undefined, undefined as unknown as undefined)).toBe(0);
    });

    test('для null и null должен вернуть 0', () => {
      expect(defaultSorter(null as unknown as null, null as unknown as null)).toBe(0);
    });

    test('для NaN и NaN должен вернуть 1 (NaN === NaN это false, поэтому NaN < NaN = false и NaN > NaN = false, но в defaultSorter NaN !== NaN = true, NaN < NaN = false => возвращает 1)', () => {
      expect(defaultSorter(NaN, NaN)).toBe(1);
    });

    test('для null и undefined должен вернуть 1 (null > undefined в defaultSorter)', () => {
      expect(defaultSorter(null as unknown as undefined, undefined as unknown as undefined)).toBe(1);
    });

    test('для 0 и null должен вернуть 1 (0 > null)', () => {
      expect(defaultSorter(0, null as unknown as number)).toBe(1);
    });

    test('для null и 0 должен вернуть 1 (null и 0 не сравнимы через < и >, поэтому возвращается 1)', () => {
      expect(defaultSorter(null as unknown as number, 0)).toBe(1);
    });
  });

  describe('booleans', () => {
    test('для true и true должен вернуть 0', () => {
      expect(defaultSorter(true as unknown as boolean, true as unknown as boolean)).toBe(0);
    });

    test('для false и true должен вернуть -1 (false < true)', () => {
      expect(defaultSorter(false as unknown as boolean, true as unknown as boolean)).toBe(-1);
    });

    test('для true и false должен вернуть 1 (true > false)', () => {
      expect(defaultSorter(true as unknown as boolean, false as unknown as boolean)).toBe(1);
    });
  });
});

describe('compareBy', () => {
  describe('сортировка по строковому ключу', () => {
    test('для массива объектов по ключу name должен отсортировать по алфавиту', () => {
      const data = [
        { name: 'Charlie', age: 30 },
        { name: 'Alice', age: 25 },
        { name: 'Bob', age: 35 },
      ];
      const sorted = [...data].sort(compareBy('name'));
      expect(sorted).toEqual([
        { name: 'Alice', age: 25 },
        { name: 'Bob', age: 35 },
        { name: 'Charlie', age: 30 },
      ]);
    });

    test('для массива объектов с одинаковыми именами должен сохранить порядок', () => {
      const data = [
        { name: 'Alice', age: 30 },
        { name: 'Alice', age: 25 },
        { name: 'Alice', age: 35 },
      ];
      const sorted = [...data].sort(compareBy('name'));
      expect(sorted).toEqual([
        { name: 'Alice', age: 30 },
        { name: 'Alice', age: 25 },
        { name: 'Alice', age: 35 },
      ]);
    });

    test('для пустого массива должен вернуть пустой массив', () => {
      const data: { name: string }[] = [];
      const sorted = [...data].sort(compareBy('name'));
      expect(sorted).toEqual([]);
    });
  });

  describe('сортировка по числовому ключу', () => {
    test('для массива объектов по ключу age должен отсортировать по возрастанию', () => {
      const data = [
        { name: 'Charlie', age: 30 },
        { name: 'Alice', age: 25 },
        { name: 'Bob', age: 35 },
      ];
      const sorted = [...data].sort(compareBy('age'));
      expect(sorted).toEqual([
        { name: 'Alice', age: 25 },
        { name: 'Charlie', age: 30 },
        { name: 'Bob', age: 35 },
      ]);
    });

    test('для массива объектов по ключу age в обратном порядке', () => {
      const data = [
        { name: 'Charlie', age: 30 },
        { name: 'Alice', age: 25 },
        { name: 'Bob', age: 35 },
      ];
      const sorted = [...data].sort(compareBy('age'));
      expect(sorted[0].age).toBe(25);
      expect(sorted[1].age).toBe(30);
      expect(sorted[2].age).toBe(35);
    });

    test('для массива с нулевыми значениями должен поставить нули первыми', () => {
      const data = [
        { name: 'Charlie', age: 30 },
        { name: 'Alice', age: 0 },
        { name: 'Bob', age: 35 },
      ];
      const sorted = [...data].sort(compareBy('age'));
      expect(sorted[0].age).toBe(0);
    });
  });

  describe('с кастомным сортером', () => {
    test('должен использовать переданную функцию сортировки', () => {
      const data = [
        { name: 'Charlie', age: 30 },
        { name: 'Alice', age: 25 },
        { name: 'Bob', age: 35 },
      ];
      // Сортировка по убыванию
      const sorted = [...data].sort(compareBy('age', (a, b) => defaultSorter(b, a)));
      expect(sorted).toEqual([
        { name: 'Bob', age: 35 },
        { name: 'Charlie', age: 30 },
        { name: 'Alice', age: 25 },
      ]);
    });

    test('для строк с кастомным сортером должен отсортировать по убыванию', () => {
      const data = [
        { name: 'Charlie', value: 1 },
        { name: 'Alice', value: 2 },
        { name: 'Bob', value: 3 },
      ];
      // Сортировка строк по убыванию
      const sorted = [...data].sort(compareBy('name', (a, b) => defaultSorter(b, a)));
      expect(sorted[0].name).toBe('Charlie');
      expect(sorted[1].name).toBe('Bob');
      expect(sorted[2].name).toBe('Alice');
    });
  });

  describe(' объекты с разными ключами', () => {
    test('для массива с отсутствующим ключом должен использовать undefined', () => {
      const data = [
        { name: 'Charlie', age: 30 },
        { name: 'Alice' },
        { name: 'Bob', age: 35 },
      ];
      // Сортировка по age, там где нет ключа - undefined
      const sorted = [...data].sort(compareBy('age'));
      // undefined сравнивается как равный undefined, так что порядок неопределен
      expect(sorted.length).toBe(3);
    });
  });

});
