/**
 * @jest-environment jsdom
 */
import * as t from 'io-ts';
import * as Either from 'fp-ts/lib/Either';

import { wrapped } from '../../io-ts';

describe('wrapped', () => {
  class User {
    name: string;
    age?: number;

    constructor(data: { name: string; age?: number }) {
      this.name = data.name;
      this.age = data.age;
    }

    greet(): string {
      return `Hello, I'm ${this.name}`;
    }
  }

  class Product {
    id: string;
    price: number;

    constructor(data: { id: string; price: number }) {
      this.id = data.id;
      this.price = data.price;
    }

    getTotal(qty: number): number {
      return this.price * qty;
    }
  }

  class EmptyClass {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    constructor() {}
  }

  describe('basic wrapping', () => {
    test('should decode valid data and wrap in class', () => {
      const UserCodec = wrapped(t.type({ name: t.string }), User);
      const result = UserCodec.decode({ name: 'Alice' });

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBeInstanceOf(User);
        expect(result.right.name).toBe('Alice');
        expect(result.right.greet()).toBe('Hello, I\'m Alice');
      }
    });

    test('should decode data with optional properties', () => {
      const UserCodec = wrapped(
        t.type({
          name: t.string,
          age: t.number,
        }),
        User
      );
      const result = UserCodec.decode({ name: 'Bob', age: 30 });

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBeInstanceOf(User);
        expect(result.right.name).toBe('Bob');
        expect(result.right.age).toBe(30);
      }
    });

    test('should reject invalid data', () => {
      const UserCodec = wrapped(t.type({ name: t.string }), User);
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const result = UserCodec.decode({ name: 123 } as any);

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should reject missing required property', () => {
      const UserCodec = wrapped(t.type({ name: t.string }), User);
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const result = UserCodec.decode({} as any);

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should encode class back to object', () => {
      const UserCodec = wrapped(t.type({ name: t.string }), User);
      const user = new User({ name: 'Charlie' });

      const result = UserCodec.encode(user);

      expect(result).toEqual({ name: 'Charlie' });
    });

    test('should use class name as codec name', () => {
      const UserCodec = wrapped(t.type({ name: t.string }), User);

      expect(UserCodec.name).toBe('User');
    });
  });

  describe('with nested objects', () => {
    class Address {
      street: string;
      city: string;

      constructor(data: { street: string; city: string }) {
        this.street = data.street;
        this.city = data.city;
      }
    }

    class Person {
      name: string;
      address: Address;

      constructor(data: { name: string; address: Address }) {
        this.name = data.name;
        this.address = data.address;
      }
    }

    test('should decode nested objects and wrap in classes', () => {
      const AddressCodec = wrapped(t.type({ street: t.string, city: t.string }), Address);
      const PersonCodec = wrapped(
        t.type({ name: t.string, address: AddressCodec }),
        Person
      );

      const result = PersonCodec.decode({
        name: 'David',
        address: { street: '123 Main St', city: 'New York' },
      });

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBeInstanceOf(Person);
        expect(result.right.name).toBe('David');
        expect(result.right.address).toBeInstanceOf(Address);
        expect(result.right.address.street).toBe('123 Main St');
        expect(result.right.address.city).toBe('New York');
      }
    });
  });

  describe('with arrays', () => {
    class Item {
      value: string;

      constructor(data: { value: string }) {
        this.value = data.value;
      }
    }

    test('should decode array of wrapped objects (using t.array)', () => {
      const ItemCodec = t.array(wrapped(t.type({ value: t.string }), Item));

      const result = ItemCodec.decode([{ value: 'a' }, { value: 'b' }, { value: 'c' }]);

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right.length).toBe(3);
        expect(result.right[0]).toBeInstanceOf(Item);
        expect(result.right[0].value).toBe('a');
        expect(result.right[1].value).toBe('b');
        expect(result.right[2].value).toBe('c');
      }
    });

    test('should reject array with invalid item (using t.array)', () => {
      const ItemCodec = t.array(wrapped(t.type({ value: t.string }), Item));

      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const result = ItemCodec.decode([{ value: 'valid' }, { value: 123 } as any]);

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should encode array of wrapped objects (using t.array)', () => {
      const ItemCodec = t.array(wrapped(t.type({ value: t.string }), Item));
      const items = [new Item({ value: 'a' }), new Item({ value: 'b' })];

      const result = ItemCodec.encode(items);

      expect(result).toEqual([{ value: 'a' }, { value: 'b' }]);
    });
  });

  describe('edge cases', () => {
    test('should handle empty class with constructor', () => {
      const EmptyCodec = wrapped(t.type({}), EmptyClass);
      const result = EmptyCodec.decode({});

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right).toBeInstanceOf(EmptyClass);
      }
    });

    test('should handle class with optional properties', () => {
      class PartialUser {
        name?: string;

        constructor(data: { name?: string } = {}) {
          this.name = data.name;
        }
      }

      const PartialUserCodec = wrapped(t.type({ name: t.string }), PartialUser);

      // With required property
      const result1 = PartialUserCodec.decode({ name: 'Test' });
      expect(Either.isRight(result1)).toBe(true);

      // With missing property - should fail because codec requires it
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const result2 = PartialUserCodec.decode({} as any);
      expect(Either.isLeft(result2)).toBe(true);
    });

    test('should handle class with methods', () => {
      class Calculator {
        value: number;

        constructor(data: { value: number }) {
          this.value = data.value;
        }

        add(n: number): number {
          return this.value + n;
        }

        multiply(n: number): number {
          return this.value * n;
        }
      }

      const CalculatorCodec = wrapped(t.type({ value: t.number }), Calculator);
      const result = CalculatorCodec.decode({ value: 10 });

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right.add(5)).toBe(15);
        expect(result.right.multiply(3)).toBe(30);
      }
    });

    test('should handle class with default values in constructor', () => {
      class DefaultUser {
        name: string;
        role: string;

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        constructor(data: any = {}) {
          this.name = data.name;
          this.role = data.role || 'user';
        }
      }

      const DefaultUserCodec = wrapped(
        t.type({ name: t.string }),
        DefaultUser
      );

      const result = DefaultUserCodec.decode({ name: 'Eve' });

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right.name).toBe('Eve');
        expect(result.right.role).toBe('user');
      }
    });

    test('should handle circular references in class', () => {
      class Node {
        value: number;
        // eslint-disable-next-line no-use-before-define
        next?: Node;

        constructor(data: { value: number; next?: Node }) {
          this.value = data.value;
          this.next = data.next;
        }
      }

      const NodeCodec = wrapped(t.type({ value: t.number }), Node);

      const result = NodeCodec.decode({
        value: 1,
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        next: { value: 2 } as any,
      });

      expect(Either.isRight(result)).toBe(true);
    });
  });

  describe('type checking', () => {
    test('should correctly identify wrapped instances', () => {
      const UserCodec = wrapped(t.type({ name: t.string }), User);
      const user = new User({ name: 'Test' });

      // The is() check should work correctly
      const isValid = UserCodec.is(user);

      expect(isValid).toBe(true);
    });

    test('should reject non-class instances', () => {
      const UserCodec = wrapped(t.type({ name: t.string }), User);

      const isValid = UserCodec.is({ name: 'Test' });

      expect(isValid).toBe(false);
    });

    test('should reject wrong class type', () => {
      const UserCodec = wrapped(t.type({ name: t.string }), User);
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const ProductCodec = wrapped(t.type({ id: t.string, price: t.number }), Product);

      const product = new Product({ id: 'P1', price: 99 });

      // UserCodec should not accept Product
      const isValid = UserCodec.is(product);

      expect(isValid).toBe(false);
    });
  });

  describe('error handling', () => {
    test('should preserve error context from inner codec', () => {
      const UserCodec = wrapped(t.type({ name: t.string }), User);
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const result = UserCodec.decode({} as any);

      expect(Either.isLeft(result)).toBe(true);
      if (Either.isLeft(result)) {
        // The error should come from the inner t.string validation
        expect(result.left).toBeDefined();
      }
    });

    test('should handle deeply nested validation errors', () => {
      const NestedCodec = wrapped(
        t.type({
          user: t.type({ name: t.string }),
        }),
        class {
          user: { name: string };
          constructor(data: { user: { name: string } }) {
            this.user = data.user;
          }
        }
      );

      const result = NestedCodec.decode({
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        user: { name: 123 } as any,
      });

      expect(Either.isLeft(result)).toBe(true);
    });
  });
});
