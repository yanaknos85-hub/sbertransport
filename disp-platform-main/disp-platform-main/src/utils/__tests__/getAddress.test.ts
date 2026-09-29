import { Waypoint } from 'api/trips/trips.types';
import { getAddress } from '../getAddress';

describe('getAddress', () => {
  describe('with complete address', () => {
    const completeWaypoint: Waypoint = {
      country: 'Россия',
      region: 'Московская область',
      city: 'Москва',
      street: 'Тверская улица',
      house: '7',
      building: '2',
    } as Waypoint;

    it('should return full address string', () => {
      expect(getAddress(completeWaypoint)).toBe(
        'Россия, Московская область, Москва, Тверская улица, 7, 2'
      );
    });
  });

  describe('with missing optional fields', () => {
    it('should skip empty country', () => {
      const waypoint: Waypoint = {
        country: '',
        region: 'Московская область',
        city: 'Москва',
        street: 'Тверская',
        house: '1',
        building: 'А',
      } as Waypoint;
      expect(getAddress(waypoint)).toBe('Московская область, Москва, Тверская, 1, А');
    });

    it('should skip empty building', () => {
      const waypoint: Waypoint = {
        country: 'Россия',
        region: 'Московская область',
        city: 'Москва',
        street: 'Тверская',
        house: '1',
        building: '',
      } as Waypoint;
      expect(getAddress(waypoint)).toBe('Россия, Московская область, Москва, Тверская, 1');
    });
  });

  describe('edge cases', () => {
    it('should return empty string for all empty fields', () => {
      const emptyWaypoint: Waypoint = {
        country: '',
        region: '',
        city: '',
        street: '',
        house: '',
        building: '',
      } as Waypoint;
      expect(getAddress(emptyWaypoint)).toBe('');
    });

    it('should handle whitespace-only fields as non-empty', () => {
      const waypoint: Waypoint = {
        country: ' ',
        region: '  ',
        city: 'Москва',
        street: 'Тверская',
        house: '1',
        building: '',
      } as Waypoint;
      expect(getAddress(waypoint)).toBe(' ,   , Москва, Тверская, 1');
    });

    it('should handle numeric values converted to strings', () => {
      const waypoint: Waypoint = {
        country: 'Россия',
        region: '77',
        city: 'Москва',
        street: 'Тверская',
        house: '1',
        building: '123',
      } as Waypoint;
      expect(getAddress(waypoint)).toBe('Россия, 77, Москва, Тверская, 1, 123');
    });
  });
});
