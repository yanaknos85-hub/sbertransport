import { declOfNum, declOfNumForSymbols } from '.';

describe('declOfNum', () => {
  const hourWords: [string, string, string] = ['час', 'часа', 'часов'];

  test('return correct word', () => {
    expect(declOfNum(1, hourWords)).toEqual('час');
    expect(declOfNum(2, hourWords)).toEqual('часа');
    expect(declOfNum(3, hourWords)).toEqual('часа');
    expect(declOfNum(4, hourWords)).toEqual('часа');
    expect(declOfNum(5, hourWords)).toEqual('часов');
    expect(declOfNum(6, hourWords)).toEqual('часов');
    expect(declOfNum(11, hourWords)).toEqual('часов');
    expect(declOfNum(121, hourWords)).toEqual('час');
    expect(declOfNum(1156, hourWords)).toEqual('часов');
  });
});

describe('declOfNumForSymbols', () => {
  test('return correct word', () => {
    expect(declOfNumForSymbols(1)).toEqual('символ');
    expect(declOfNumForSymbols(2)).toEqual('символа');
    expect(declOfNumForSymbols(3)).toEqual('символа');
    expect(declOfNumForSymbols(4)).toEqual('символа');
    expect(declOfNumForSymbols(5)).toEqual('символов');
    expect(declOfNumForSymbols(6)).toEqual('символов');
    expect(declOfNumForSymbols(11)).toEqual('символов');
    expect(declOfNumForSymbols(121)).toEqual('символ');
    expect(declOfNumForSymbols(1156)).toEqual('символов');
  });
});
