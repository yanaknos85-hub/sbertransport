/* eslint-disable @typescript-eslint/no-var-requires */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { AxiosResponse } from 'axios';

// Mock content-disposition package before importing
jest.mock('content-disposition');

describe('getFilenameFromHeader', () => {
  const createMockResponse = (headers: Partial<AxiosResponse['headers']>): AxiosResponse => ({
    config: {} as unknown,
    data: null as unknown,
    headers: headers as unknown,
    status: 200,
    statusText: 'OK',
  } as AxiosResponse);

  const getFilenameFromHeader = (response: AxiosResponse): string => {
    return require('../downloads').getFilenameFromHeader(response);
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should extract filename from valid Content-Disposition header', () => {
    const mockResponse = createMockResponse({
      'content-disposition': 'attachment; filename="test-file.txt"',
    });

    (jest.requireMock('content-disposition').parse as jest.Mock).mockReturnValue({
      parameters: { filename: 'test-file.txt' },
    });

    const result = getFilenameFromHeader(mockResponse);
    expect(result).toBe('test-file.txt');
    expect(jest.requireMock('content-disposition').parse).toHaveBeenCalledWith(
      'attachment; filename="test-file.txt"'
    );
  });

  it('should handle missing Content-Disposition header', () => {
    const mockResponse = createMockResponse({});

    const result = getFilenameFromHeader(mockResponse);
    expect(result).toBe('');
  });

  it('should handle Content-Disposition with charset and filename*', () => {
    const mockResponse = createMockResponse({
      'content-disposition': 'attachment; filename*=UTF-8\'\'%D1%84%D0%B0%D0%B9%D0%BB.txt',
    });

    (jest.requireMock('content-disposition').parse as jest.Mock).mockReturnValue({
      parameters: { filename: '%D1%84%D0%B0%D0%B9%D0%BB.txt' },
    });

    const result = getFilenameFromHeader(mockResponse);
    expect(result).toBe('файл.txt');
  });

  it('should handle URL-encoded filenames', () => {
    const mockResponse = createMockResponse({
      'content-disposition': 'attachment; filename="file%20with%20spaces.txt"',
    });

    (jest.requireMock('content-disposition').parse as jest.Mock).mockReturnValue({
      parameters: { filename: 'file%20with%20spaces.txt' },
    });

    const result = getFilenameFromHeader(mockResponse);
    expect(result).toBe('file with spaces.txt');
  });

  it('should handle filename with special characters', () => {
    const mockResponse = createMockResponse({
      'content-disposition': 'attachment; filename="file_(special-chars).txt"',
    });

    (jest.requireMock('content-disposition').parse as jest.Mock).mockReturnValue({
      parameters: { filename: 'file_(special-chars).txt' },
    });

    const result = getFilenameFromHeader(mockResponse);
    expect(result).toBe('file_(special-chars).txt');
  });

  it('should handle empty Content-Disposition header', () => {
    const mockResponse = createMockResponse({
      'content-disposition': '',
    });

    const result = getFilenameFromHeader(mockResponse);
    expect(result).toBe('');
  });

  it('should handle Content-Disposition without filename', () => {
    const mockResponse = createMockResponse({
      'content-disposition': 'attachment; format=download',
    });

    (jest.requireMock('content-disposition').parse as jest.Mock).mockReturnValue({
      parameters: {},
    });

    const result = getFilenameFromHeader(mockResponse);
    expect(result).toBe('');
  });
});

describe('handleClick', () => {
  const originalCreateObjectURL = window.URL.createObjectURL;
  const originalRevokeObjectURL = window.URL.revokeObjectURL;
  const originalBlob = window.Blob;

  const mockAppendChild = jest.fn();
  const mockRemoveChild = jest.fn();
  const mockClick = jest.fn();
  const mockSetAttribute = jest.fn();

  const mockUrl = 'blob:test-url';

  beforeEach(() => {
    window.URL.createObjectURL = jest.fn().mockReturnValue(mockUrl);
    window.URL.revokeObjectURL = jest.fn();

    (window as any).Blob = jest.fn().mockImplementation((content, options) => ({
      content,
      options,
    }));

    jest.spyOn(document.body, 'appendChild').mockImplementation(mockAppendChild);
    jest.spyOn(document.body, 'removeChild').mockImplementation(mockRemoveChild);

    const originalCreateElement = document.createElement.bind(document);
    jest.spyOn(document, 'createElement').mockImplementation(tagName => {
      const anchor = originalCreateElement(tagName) as HTMLAnchorElement;
      anchor.style.display = 'none';
      anchor.href = '';
      anchor.download = '';
      anchor.click = mockClick;
      anchor.setAttribute = mockSetAttribute;
      return anchor;
    });

    mockAppendChild.mockClear();
    mockRemoveChild.mockClear();
    mockClick.mockClear();
    mockSetAttribute.mockClear();
  });

  const handleClick = (data: unknown, mimeType: string, fileName: string): void => {
    require('../downloads').handleClick(data, mimeType, fileName);
  };

  afterEach(() => {
    window.URL.createObjectURL = originalCreateObjectURL;
    window.URL.revokeObjectURL = originalRevokeObjectURL;
    (window as any).Blob = originalBlob;
    jest.restoreAllMocks();
  });

  it('should create Blob with correct data and mimetype', () => {
    const data = 'test data';
    const mimeType = 'text/plain';

    handleClick(data, mimeType, 'test.txt');

    expect((window as any).Blob).toHaveBeenCalledWith([data], { type: mimeType });
  });

  it('should create object URL from blob', () => {
    handleClick('data', 'text/plain', 'test.txt');

    expect(window.URL.createObjectURL).toHaveBeenCalled();
  });

  it('should create anchor element with download attribute', () => {
    const fileName = 'test-file.txt';

    handleClick('data', 'text/plain', fileName);

    expect(document.createElement).toHaveBeenCalledWith('a');

    const callArgs = mockAppendChild.mock.calls[0];
    const anchor = callArgs[0] as HTMLAnchorElement;
    expect(anchor.download).toBe(fileName);
  });

  it('should set href to object URL', () => {
    handleClick('data', 'text/plain', 'test.txt');

    const callArgs = mockAppendChild.mock.calls[0];
    const anchor = callArgs[0] as HTMLAnchorElement;
    expect(anchor.href).toBe(mockUrl);
  });

  it('should set display style to none', () => {
    handleClick('data', 'text/plain', 'test.txt');

    const callArgs = mockAppendChild.mock.calls[0];
    const anchor = callArgs[0] as HTMLAnchorElement;
    expect(anchor.style.display).toBe('none');
  });

  it('should append anchor to body', () => {
    handleClick('data', 'text/plain', 'test.txt');

    expect(mockAppendChild).toHaveBeenCalled();
  });

  it('should trigger click event', () => {
    handleClick('data', 'text/plain', 'test.txt');

    expect(mockClick).toHaveBeenCalled();
  });

  it('should revoke object URL after click', () => {
    handleClick('data', 'text/plain', 'test.txt');

    expect(window.URL.revokeObjectURL).toHaveBeenCalledWith(mockUrl);
  });

  it('should handle empty data', () => {
    handleClick('', 'text/plain', 'test.txt');

    expect((window as any).Blob).toHaveBeenCalledWith([''], { type: 'text/plain' });
    expect(window.URL.createObjectURL).toHaveBeenCalled();
    expect(mockClick).toHaveBeenCalled();
  });

  it('should handle filename with special characters', () => {
    const fileName = 'файл_спец_символы.txt';

    handleClick('data', 'text/plain', fileName);

    const callArgs = mockAppendChild.mock.calls[0];
    const anchor = callArgs[0] as HTMLAnchorElement;
    expect(anchor.download).toBe(fileName);
  });

  it('should handle filename with spaces', () => {
    const fileName = 'my test file.txt';

    handleClick('data', 'text/plain', fileName);

    const callArgs = mockAppendChild.mock.calls[0];
    const anchor = callArgs[0] as HTMLAnchorElement;
    expect(anchor.download).toBe(fileName);
  });

  it('should set correct mime type for blob', () => {
    const mimeType = 'application/octet-stream';
    const data = new Uint8Array([1, 2, 3]);

    handleClick(data, mimeType, 'test.bin');

    expect((window as any).Blob).toHaveBeenCalledWith([data], { type: mimeType });
  });
});
