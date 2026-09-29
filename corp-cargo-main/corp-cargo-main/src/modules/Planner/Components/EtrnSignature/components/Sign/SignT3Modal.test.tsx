import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';

// ========================
//    Моки
// ========================

const mockGetSystemInfo = jest.fn();
const mockGetCertificates = jest.fn();
const mockSignFileByCertificate = jest.fn();
const mockSendEtrn = jest.fn();
const mockAddMessage = jest.fn();
const mockHistoryPush = jest.fn();

jest.mock('@sber-sbertransport/mf-core', () => ({
  useHistory: () => ({ push: mockHistoryPush }),
}));

jest.mock('crypto-pro-actual-cades-plugin', () => ({
  getSystemInfo: (...args: unknown[]) => mockGetSystemInfo(...args),
  __esModule: true,
}));

jest.mock('modules/Planner/Components/EtrnSignature/utils/cryptoPro', () => ({
  getCertificates: (...args: unknown[]) => mockGetCertificates(...args),
  signFileByCertificate: (...args: unknown[]) => mockSignFileByCertificate(...args),
  __esModule: true,
}));

jest.mock('api/etrn-signature/etrn-signature', () => ({
  useSendEtrn: () => [mockSendEtrn, { status: 'idle', data: null, error: null, isLoading: false }],
  useGetEtrnTitle: () => ({
    data: null,
    isLoading: false,
    isError: false,
    error: null,
    refetch: jest.fn(),
  }),
}));

jest.mock('i18n', () => ({
  useTranslation: () => ({
    t: {
      Etrn: {
        signModal: {
          title: 'Подписание титула Т3',
          okText: 'Подписать и отправить',
          loading: {
            init: 'Инициализация КриптоПро...',
            signing: 'Происходит подписание...',
            warning: 'Не закрывайте вкладку браузера',
          },
          error: {
            invalidWorkplace: 'Не удалось инициализировать рабочее место',
            getSystemInfoError: 'Не удалось получить информацию о системе',
            noCspProvider: 'КриптоПро CSP не установлен',
            noCadesPlugin: 'Плагин КриптоПро для браузера не установлен',
            getCertificatesError: 'Не удалось получить список сертификатов',
            noCertificates: 'Сертификаты не найдены',
            signingError: 'Ошибка при формировании подписи',
          },
          certificates: { validTo: 'Действителен до:' },
        },
      },
    },
  }),
}));

jest.mock('antd', () => {
  const MockModal = ({
    visible, onCancel, onOk, okText, okButtonProps, children, confirmLoading,
  }: any) => {
    if (!visible) return null;
    return (
      <div data-testid="antd-modal" data-confirm-loading={String(!!confirmLoading)}>
        <div data-testid="modal-content">{children}</div>
        <button data-testid="modal-cancel" onClick={onCancel}>Cancel</button>
        <button
          data-testid="modal-ok"
          onClick={onOk}
          disabled={okButtonProps?.disabled}
        >
          {okText}
        </button>
      </div>
    );
  };
  return { Modal: MockModal };
});

jest.mock('shared/components/SpinWrapped/SpinWrapped', () => ({
  SpinWrapped: () => <div data-testid="spin-wrapped" />,
}));

// Мокаем дочерние компоненты, чтобы не зависеть от КриптоПро и i18n внутри
jest.mock('./Loading/Loading', () => ({
  __esModule: true,
  default: ({ type }: { type: 'init' | 'signing' }) =>
    <div data-testid="loading" data-type={type} />,
}));

jest.mock('./Error/Error', () => ({
  __esModule: true,
  default: ({ messages }: { messages: string[] }) =>
    <div data-testid="error" data-messages={JSON.stringify(messages)} />,
}));

jest.mock('./Certificates/Certificates', () => ({
  __esModule: true,
  default: ({ selected, list, onSelect }: any) => (
    <div data-testid="certificates">
      {list?.map((cert: any) => (
        <button
          key={cert.thumbprint}
          data-testid={`cert-${cert.thumbprint}`}
          data-active={String(selected === cert.thumbprint)}
          data-cert-active={String(cert.active)}
          onClick={() => cert.active && onSelect(cert.thumbprint)}
        >
          {cert.name}
        </button>
      ))}
    </div>
  ),
}));

// ========================
//    Helpers
// ========================

const baseProps = {
  cardId: 'card-123',
  title: {
    fileName: 'title_T3_card-123.xml',
    // base64 от '<xml>title-content</xml>' — SignT3Modal вызывает
    // b64DecodeUnicode перед передачей в КриптоПро, поэтому в content
    // должна лежать именно base64-строка (как приходит из GET /etrn-cargo/:etrnId/title/).
    content: 'PHhtbD50aXRsZS1jb250ZW50PC94bWw+',
    creationTime: '2026-08-21T10:00:00.000+03:00',
  },
  onClose: jest.fn(),
  onSigned: jest.fn(),
};

const activeCerts = [
  {
    thumbprint: 'thumb-1',
    name: 'Иванов Иван',
    validTo: '2027-01-01T00:00:00',
    active: true,
  },
  {
    thumbprint: 'thumb-2',
    name: 'Петров Пётр',
    validTo: '2027-06-01T00:00:00',
    active: true,
  },
];

const renderSignT3Modal = (props = {}) => {
  return render(
    <SignT3Modal
      {...baseProps}
      {...props}
      visible
    />
  );
};

import SignT3Modal from './SignT3Modal';

// ========================
//    Тесты
// ========================

describe('SignT3Modal', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockAddMessage.mockClear();
  });

  it('не рендерится, когда visible=false', () => {
    render(<SignT3Modal {...baseProps} visible={false} />);
    expect(screen.queryByTestId('antd-modal')).not.toBeInTheDocument();
  });

  it('показывает экран init, пока getSystemInfo не завершился', () => {
    // getSystemInfo резолвится в MicroTask, до этого messages === null
    mockGetSystemInfo.mockReturnValue(new Promise(() => {}));
    mockGetCertificates.mockReturnValue(new Promise(() => {}));

    renderSignT3Modal();

    expect(screen.getByTestId('loading')).toBeInTheDocument();
    expect(screen.getByTestId('loading')).toHaveAttribute('data-type', 'init');
  });

  it('после успешной инициализации показывает список сертификатов', async () => {
    mockGetSystemInfo.mockResolvedValue({ cspVersion: '5.0', cadesVersion: '2.0' });
    mockGetCertificates.mockResolvedValue(activeCerts);

    renderSignT3Modal();

    await waitFor(() => {
      expect(screen.getByTestId('certificates')).toBeInTheDocument();
    });
    expect(screen.getByTestId('cert-thumb-1')).toBeInTheDocument();
    expect(screen.getByTestId('cert-thumb-2')).toBeInTheDocument();
  });

  it('отображает сообщение об ошибке, если не установлен CSP', async () => {
    mockGetSystemInfo.mockResolvedValue({ cspVersion: '', cadesVersion: '2.0' });
    mockGetCertificates.mockResolvedValue(activeCerts);

    renderSignT3Modal();

    await waitFor(() => {
      expect(screen.getByTestId('error')).toBeInTheDocument();
    });
    const errorEl = screen.getByTestId('error');
    expect(JSON.parse(errorEl.getAttribute('data-messages')!)).toContain('noCspProvider');
  });

  it('отображает сообщение об ошибке, если getSystemInfo бросил исключение', async () => {
    mockGetSystemInfo.mockRejectedValue(new Error('boom'));

    renderSignT3Modal();

    await waitFor(() => {
      expect(screen.getByTestId('error')).toBeInTheDocument();
    });
    const errorEl = screen.getByTestId('error');
    expect(JSON.parse(errorEl.getAttribute('data-messages')!)).toContain('getSystemInfoError');
  });

  it('отображает сообщение об ошибке, если нет сертификатов', async () => {
    mockGetSystemInfo.mockResolvedValue({ cspVersion: '5.0', cadesVersion: '2.0' });
    mockGetCertificates.mockResolvedValue([]);

    renderSignT3Modal();

    await waitFor(() => {
      expect(screen.getByTestId('error')).toBeInTheDocument();
    });
    const errorEl = screen.getByTestId('error');
    expect(JSON.parse(errorEl.getAttribute('data-messages')!)).toContain('noCertificates');
  });

  it('кнопка «Подписать и отправить» заблокирована, пока не выбран сертификат', async () => {
    mockGetSystemInfo.mockResolvedValue({ cspVersion: '5.0', cadesVersion: '2.0' });
    mockGetCertificates.mockResolvedValue(activeCerts);

    renderSignT3Modal();

    await waitFor(() => {
      expect(screen.getByTestId('modal-ok')).toBeInTheDocument();
    });
    expect(screen.getByTestId('modal-ok')).toBeDisabled();
  });

  it('клик по сертификату активирует кнопку «Подписать и отправить»', async () => {
    mockGetSystemInfo.mockResolvedValue({ cspVersion: '5.0', cadesVersion: '2.0' });
    mockGetCertificates.mockResolvedValue(activeCerts);

    renderSignT3Modal();

    await waitFor(() => {
      expect(screen.getByTestId('cert-thumb-1')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByTestId('cert-thumb-1'));

    await waitFor(() => {
      expect(screen.getByTestId('modal-ok')).not.toBeDisabled();
    });
    expect(screen.getByTestId('cert-thumb-1')).toHaveAttribute('data-active', 'true');
  });

  it('успешное подписание вызывает signFileByCertificate, useSendEtrn и onSigned', async () => {
    mockGetSystemInfo.mockResolvedValue({ cspVersion: '5.0', cadesVersion: '2.0' });
    mockGetCertificates.mockResolvedValue(activeCerts);
    mockSignFileByCertificate.mockResolvedValue('SIGNED_PAYLOAD');

    mockSendEtrn.mockImplementation((_vars, options) => {
      options?.onSuccess?.();
    });

    renderSignT3Modal();

    await waitFor(() => {
      expect(screen.getByTestId('cert-thumb-1')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByTestId('cert-thumb-1'));
    fireEvent.click(screen.getByTestId('modal-ok'));

    await waitFor(() => {
      expect(mockSignFileByCertificate).toHaveBeenCalledWith('thumb-1', '<xml>title-content</xml>');
    });

    await waitFor(() => {
      expect(mockSendEtrn).toHaveBeenCalledWith(
        {
          cardId: 'card-123',
          titleType: 'T3',
          fileName: 'title_T3_card-123.xml',
          file: 'PHhtbD50aXRsZS1jb250ZW50PC94bWw+',
          signature: 'SIGNED_PAYLOAD',
          creationTime: '2026-08-21T10:00:00.000+03:00',
        },
        expect.objectContaining({ onSuccess: expect.any(Function), onError: expect.any(Function) })
      );
    });

    expect(baseProps.onSigned).toHaveBeenCalled();
    expect(mockHistoryPush).toHaveBeenCalledWith('/client/cargo/multi-logistics?tab=etrn');
  });

  it('если signFileByCertificate бросает исключение, модалка остаётся открытой', async () => {
    mockGetSystemInfo.mockResolvedValue({ cspVersion: '5.0', cadesVersion: '2.0' });
    mockGetCertificates.mockResolvedValue(activeCerts);
    mockSignFileByCertificate.mockRejectedValue(new Error('sign boom'));

    renderSignT3Modal();

    await waitFor(() => {
      expect(screen.getByTestId('cert-thumb-1')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByTestId('cert-thumb-1'));
    fireEvent.click(screen.getByTestId('modal-ok'));

    await waitFor(() => {
      expect(screen.getByTestId('error')).toBeInTheDocument();
    });

    expect(mockSendEtrn).not.toHaveBeenCalled();
    expect(baseProps.onSigned).not.toHaveBeenCalled();
  });

  it('onClose вызывается при клике по Cancel', async () => {
    mockGetSystemInfo.mockResolvedValue({ cspVersion: '5.0', cadesVersion: '2.0' });
    mockGetCertificates.mockResolvedValue(activeCerts);

    renderSignT3Modal();

    await waitFor(() => {
      expect(screen.getByTestId('modal-cancel')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByTestId('modal-cancel'));

    expect(baseProps.onClose).toHaveBeenCalled();
  });

  it('сбрасывает выбранный сертификат при повторном открытии', async () => {
    mockGetSystemInfo.mockResolvedValue({ cspVersion: '5.0', cadesVersion: '2.0' });
    mockGetCertificates.mockResolvedValue(activeCerts);

    const { rerender } = render(
      <SignT3Modal {...baseProps} visible={false} />
    );

    // Первый рендер с visible=true
    rerender(<SignT3Modal {...baseProps} visible={true} />);

    await waitFor(() => {
      expect(screen.getByTestId('cert-thumb-1')).toBeInTheDocument();
    });

    // Выбираем сертификат
    fireEvent.click(screen.getByTestId('cert-thumb-1'));
    await waitFor(() => {
      expect(screen.getByTestId('cert-thumb-1')).toHaveAttribute('data-active', 'true');
    });

    // Закрываем
    rerender(<SignT3Modal {...baseProps} visible={false} />);
    expect(screen.queryByTestId('antd-modal')).not.toBeInTheDocument();

    // Открываем снова — выбранный сертификат должен быть сброшен
    rerender(<SignT3Modal {...baseProps} visible={true} />);

    await waitFor(() => {
      expect(screen.getByTestId('cert-thumb-1')).toBeInTheDocument();
    });
    expect(screen.getByTestId('cert-thumb-1')).toHaveAttribute('data-active', 'false');
  });
});
