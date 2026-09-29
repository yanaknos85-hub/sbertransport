/* eslint-disable @typescript-eslint/no-explicit-any */
/**
 * @jest-environment jsdom
 */
import * as t from 'io-ts';
import * as Either from 'fp-ts/lib/Either';

// Mock the SortDirection constant
jest.mock('constants/app.constants', () => ({
  SortDirection: 'DESC' as const,
}));

// Import after mocking
import { createPagination, PaginationParams, SortParams } from '../../io-ts/pagination';

describe('pagination', () => {
  describe('Sort', () => {
    const Sort = t.partial({
      sorted: t.boolean,
      unsorted: t.boolean,
      empty: t.boolean,
    });

    test('should decode Sort with all properties', () => {
      const result = Sort.decode({
        sorted: true,
        unsorted: false,
        empty: false,
      });

      expect(Either.isRight(result)).toBe(true);
    });

    test('should decode Sort with partial properties', () => {
      const result = Sort.decode({
        sorted: true,
      });

      expect(Either.isRight(result)).toBe(true);
    });

    test('should decode Sort with empty object', () => {
      const result = Sort.decode({});

      expect(Either.isRight(result)).toBe(true);
    });

    test('should reject invalid boolean values', () => {
      const result = Sort.decode({
        sorted: 'true' as any,
      } as any);

      expect(Either.isLeft(result)).toBe(true);
    });
  });

  describe('Pageable', () => {
    const Pageable = t.type({
      sort: t.partial({
        sorted: t.boolean,
        unsorted: t.boolean,
        empty: t.boolean,
      }),
      offset: t.number,
      pageNumber: t.number,
      pageSize: t.number,
      paged: t.boolean,
      unpaged: t.boolean,
    });

    test('should decode valid Pageable object', () => {
      const result = Pageable.decode({
        sort: { sorted: true },
        offset: 0,
        pageNumber: 0,
        pageSize: 10,
        paged: true,
        unpaged: false,
      });

      expect(Either.isRight(result)).toBe(true);
    });

    test('should reject missing required property', () => {
      const result = Pageable.decode({
        sort: { sorted: true },
        offset: 0,
        // missing pageNumber
        pageSize: 10,
        paged: true,
        unpaged: false,
      } as any);

      expect(Either.isLeft(result)).toBe(true);
    });
  });

  describe('createPagination', () => {
    const UserCodec = t.type({
      id: t.number,
      name: t.string,
    });

    const UserPagination = createPagination(UserCodec);

    test('should decode valid pagination response', () => {
      const result = UserPagination.decode({
        content: [
          { id: 1, name: 'Alice' },
          { id: 2, name: 'Bob' },
        ],
        empty: false,
        first: true,
        last: false,
        number: 0,
        numberOfElements: 2,
        pageable: {
          sort: { sorted: true },
          offset: 0,
          pageNumber: 0,
          pageSize: 10,
          paged: true,
          unpaged: false,
        },
        size: 10,
        sort: { sorted: true },
        totalElements: 100,
        totalPages: 10,
      });

      expect(Either.isRight(result)).toBe(true);
      if (Either.isRight(result)) {
        expect(result.right.content.length).toBe(2);
        expect(result.right.totalElements).toBe(100);
        expect(result.right.numberOfElements).toBe(2);
      }
    });

    test('should decode pagination with empty content', () => {
      const result = UserPagination.decode({
        content: [],
        empty: true,
        first: true,
        last: true,
        number: 0,
        numberOfElements: 0,
        pageable: {
          sort: { sorted: false },
          offset: 0,
          pageNumber: 0,
          pageSize: 0,
          paged: false,
          unpaged: true,
        },
        size: 0,
        sort: { unsorted: true },
        totalElements: 0,
        totalPages: 0,
      });

      expect(Either.isRight(result)).toBe(true);
    });

    test('should decode pagination with large page number', () => {
      const result = UserPagination.decode({
        content: [],
        empty: true,
        first: false,
        last: true,
        number: 99,
        numberOfElements: 0,
        pageable: {
          sort: { sorted: false },
          offset: 990,
          pageNumber: 99,
          pageSize: 10,
          paged: false,
          unpaged: true,
        },
        size: 10,
        sort: { unsorted: true },
        totalElements: 1000,
        totalPages: 100,
      });

      expect(Either.isRight(result)).toBe(true);
    });

    test('should reject invalid content array', () => {
      const result = UserPagination.decode({
        content: [{ id: 'not-a-number', name: 'Alice' }], // invalid id type
        empty: false,
        first: true,
        last: true,
        number: 0,
        numberOfElements: 1,
        pageable: {
          sort: { sorted: false },
          offset: 0,
          pageNumber: 0,
          pageSize: 10,
          paged: true,
          unpaged: false,
        },
        size: 10,
        sort: { unsorted: true },
        totalElements: 1,
        totalPages: 1,
      } as any);

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should reject invalid content type', () => {
      const result = UserPagination.decode({
        content: 'not-an-array' as any,
        empty: false,
        first: true,
        last: true,
        number: 0,
        numberOfElements: 0,
        pageable: {
          sort: { sorted: false },
          offset: 0,
          pageNumber: 0,
          pageSize: 10,
          paged: true,
          unpaged: false,
        },
        size: 10,
        sort: { unsorted: true },
        totalElements: 0,
        totalPages: 1,
      });

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should reject missing required property', () => {
      const result = UserPagination.decode({
        content: [],
        empty: false,
        first: true,
        // missing last property
        number: 0,
        numberOfElements: 0,
        pageable: {
          sort: { sorted: false },
          offset: 0,
          pageNumber: 0,
          pageSize: 10,
          paged: true,
          unpaged: false,
        },
        size: 10,
        sort: { unsorted: true },
        totalElements: 0,
        totalPages: 1,
      } as any);

      expect(Either.isLeft(result)).toBe(true);
    });

    test('should encode pagination correctly', () => {
      const data = {
        content: [{ id: 1, name: 'Alice' }],
        empty: false,
        first: true,
        last: true,
        number: 0,
        numberOfElements: 1,
        pageable: {
          sort: { sorted: true },
          offset: 0,
          pageNumber: 0,
          pageSize: 10,
          paged: true,
          unpaged: false,
        },
        size: 10,
        sort: { sorted: true },
        totalElements: 1,
        totalPages: 1,
      };

      const result = UserPagination.encode(data);

      expect(result).toEqual(data);
    });

    test('should work with different content types', () => {
      const ProductCodec = t.type({
        productId: t.string,
        price: t.number,
      });

      const ProductPagination = createPagination(ProductCodec);

      const result = ProductPagination.decode({
        content: [
          { productId: 'P001', price: 99.99 },
          { productId: 'P002', price: 149.99 },
        ],
        empty: false,
        first: true,
        last: false,
        number: 0,
        numberOfElements: 2,
        pageable: {
          sort: { sorted: true },
          offset: 0,
          pageNumber: 0,
          pageSize: 10,
          paged: true,
          unpaged: false,
        },
        size: 10,
        sort: { sorted: true },
        totalElements: 50,
        totalPages: 5,
      });

      expect(Either.isRight(result)).toBe(true);
    });

    test('should work with nested object content types', () => {
      const AddressCodec = t.type({
        street: t.string,
        city: t.string,
      });

      const UserWithAddressCodec = t.type({
        id: t.number,
        name: t.string,
        address: AddressCodec,
      });

      const UserPagination = createPagination(UserWithAddressCodec);

      const result = UserPagination.decode({
        content: [
          {
            id: 1,
            name: 'Alice',
            address: {
              street: '123 Main St',
              city: 'New York',
            },
          },
        ],
        empty: false,
        first: true,
        last: true,
        number: 0,
        numberOfElements: 1,
        pageable: {
          sort: { sorted: false },
          offset: 0,
          pageNumber: 0,
          pageSize: 10,
          paged: true,
          unpaged: false,
        },
        size: 10,
        sort: { unsorted: true },
        totalElements: 1,
        totalPages: 1,
      });

      expect(Either.isRight(result)).toBe(true);
    });

    test('should handle null and undefined in content when optional', () => {
      // Using optional to allow null/undefined in content
      const OptionalUserCodec = t.union([UserCodec, t.null, t.undefined]);

      const UserPagination = createPagination(OptionalUserCodec);

      const result = UserPagination.decode({
        content: [
          { id: 1, name: 'Alice' },
          null,
          undefined,
        ],
        empty: false,
        first: true,
        last: true,
        number: 0,
        numberOfElements: 3,
        pageable: {
          sort: { sorted: false },
          offset: 0,
          pageNumber: 0,
          pageSize: 10,
          paged: true,
          unpaged: false,
        },
        size: 10,
        sort: { unsorted: true },
        totalElements: 3,
        totalPages: 1,
      });

      expect(Either.isRight(result)).toBe(true);
    });
  });

  describe('PaginationParams', () => {
    test('should have required page and size properties', () => {
      const params: PaginationParams = {
        page: 1,
        size: 10,
      };

      expect(params.page).toBe(1);
      expect(params.size).toBe(10);
    });

    test('should accept optional field and direction', () => {
      const params: PaginationParams & SortParams = {
        page: 1,
        size: 10,
        field: 'name',
        direction: 'ASC',
      };

      expect(params.field).toBe('name');
      expect(params.direction).toBe('ASC');
    });
  });

  describe('SortParams', () => {
    test('should have optional field property', () => {
      const params: PaginationParams & SortParams<string> = {
        page: 1,
        size: 10,
      };

      expect(params.field).toBeUndefined();
    });

    test('should have optional direction property', () => {
      const params: PaginationParams & SortParams<string> = {
        page: 1,
        size: 10,
        direction: 'ASC',
      };

      expect(params.direction).toBe('ASC');
    });

    test('should work with custom field type', () => {
      type CustomField = 'createdAt' | 'name' | 'price';

      const params: PaginationParams & SortParams<CustomField> = {
        page: 1,
        size: 10,
        field: 'name',
        direction: 'ASC',
      };

      expect(params.field).toBe('name');
    });
  });
});
