import { sizesToUnits, toMillimeters } from './Misc';

describe('toMillimeters', () => {
  it('should convert centimeters to millimeters', () => {
    expect(toMillimeters(1)).toBe(10);
    expect(toMillimeters(10)).toBe(100);
    expect(toMillimeters(100)).toBe(1000);
    expect(toMillimeters(0)).toBe(0);
    expect(toMillimeters(5.5)).toBe(55);
  });
});

describe('sizesToUnits', () => {
  it('should convert length, width, height from cm to mm', () => {
    const result = sizesToUnits({
      length: 10,
      width: 5,
      height: 3,
    });

    expect(result.length).toBe(100);
    expect(result.width).toBe(50);
    expect(result.height).toBe(30);
  });

  it('should handle zero values', () => {
    const result = sizesToUnits({
      length: 0,
      width: 0,
      height: 0,
    });

    expect(result.length).toBe(0);
    expect(result.width).toBe(0);
    expect(result.height).toBe(0);
  });

  it('should handle decimal values', () => {
    const result = sizesToUnits({
      length: 10.5,
      width: 5.2,
      height: 3.7,
    });

    expect(result.length).toBe(105);
    expect(result.width).toBe(52);
    expect(result.height).toBe(37);
  });
});
