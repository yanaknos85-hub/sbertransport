import { act, render, screen, fireEvent, waitFor } from '@testing-library/react';
import React from 'react';
import { AxiosError } from 'axios';

import EtrnModal from './EtrnModal';

// ========================
//    Моки
// ========================

const mockUseEtrnCard = jest.fn();
const mockUseAcquireLock = jest.fn();
const mockUseReleaseLock = jest.fn();
const mockUseProfile = jest.fn();
const mockUseTranslation = jest.fn(() => ({ t: (key: string) => key }));
const mockSignT3Modal = jest.fn();
const mockUseCheckSigningEligibility = jest.fn();
const mockUseGetEtrnTitle = jest.fn();
const mockLogger = { toMessage: jest.fn(), toNotify: jest.fn(), toConsole: jest.fn() };

jest.mock('mobx-react', () => ({
  observer: (component: React.ComponentType) => component,
}));

jest.mock('shared/hooks/useAppStoreContext', () => ({
  useAppStoreContext: () => ({ logger: mockLogger }),
}));

jest.mock('api/etrn-signature/etrn-signature', () => ({
  useEtrnCard: (...args: unknown[]) => mockUseEtrnCard(...args),
  useAcquireLock: (...args: unknown[]) => mockUseAcquireLock(...args),
  useReleaseLock: (...args: unknown[]) => mockUseReleaseLock(...args),
  useCheckSigningEligibility: (...args: unknown[]) => mockUseCheckSigningEligibility(...args),
  useGetEtrnTitle: (...args: unknown[]) => mockUseGetEtrnTitle(...args),
}));

jest.mock('i18n', () => {
  const translationTree: Record<string, unknown> = {
    Etrn: {
      card: {
        lockConflictBanner: 'lockConflictBanner',
        humanReadableIdPlaceholder: 'humanReadableIdPlaceholder',
        tabs: { general: 'general', documents: 'documents', checks: 'checks', history: 'history' },
        viewDocument: 'viewDocument',
        close: 'Закрыть',
        signUkep: 'signUkep',
        documentsContent: 'documentsContent',
        checksContent: 'checksContent',
        historyContent: 'historyContent',
        titleContentEmpty: 'Не удалось получить содержимое титула для подписания. Попробуйте позже.',
      },
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
      sign: { success: 'Титул Т3 успешно подписан' },
    },
  };
  return {
    useTranslation: () => ({
      t: translationTree,
      // Старый тестовый код использует `t(key)` (как функцию), поэтому проксируем:
      // возвращаем строковый ключ как fallback, чтобы не падать.
      // (актуальные тесты используют t.Etrn.card.signUkep и т.п.)
    }),
  };
});

jest.mock('../Sign/SignT3Modal', () => ({
  __esModule: true,
  default: (props: { visible: boolean }) => {
    mockSignT3Modal(props);
    return props.visible ? <div data-testid="sign-t3-modal" /> : null;
  },
}));

jest.mock('antd', () => {
  const MockTabPane = ({ tab, key, children }: { tab: string; key: string; children: React.ReactNode }) => (
    <div data-testid={`tab-pane-${key}`} data-tab={tab}>
      {children}
    </div>
  );

  const MockTabs = ({
    activeKey,
    onChange,
    children,
  }: {
    activeKey: string;
    onChange: (key: string) => void;
    children: React.ReactNode;
  }) => (
    <div data-testid="tabs" data-activekey={activeKey}>
      <button type="button" data-testid="tab-general" onClick={() => onChange('general')}>
        general
      </button>
      <button type="button" data-testid="tab-documents" onClick={() => onChange('documents')}>
        documents
      </button>
      {children}
    </div>
  );

  MockTabs.TabPane = MockTabPane;

  return {
    Modal: ({
      children,
      visible,
      onCancel,
      width,
      footer,
    }: {
      children: React.ReactNode;
      visible: boolean;
      onCancel?: () => void;
      width?: string;
      footer?: unknown;
    }) =>
      visible
        ? (
            <div data-testid="antd-modal">
              {footer !== null && footer && (
                <div data-testid="modal-footer">{footer}</div>
              )}
              {children}
            </div>
          )
        : null,
    Button: ({ children }: { children: React.ReactNode }) => <button>{children}</button>,
    Tabs: MockTabs as typeof MockTabs & { TabPane: typeof MockTabPane },
    Divider: () => <hr data-testid="divider" />,
  };
});

jest.mock('./components/TitleChain', () => ({
  TitleChain: ({ titles }: { titles: unknown[] }) =>
    !titles || titles.length === 0
      ? <div data-testid="title-chain">Ожидание данных от Корус</div>
      : <div data-testid="title-chain">Цепочка титулов ЭТрН</div>,
}));

jest.mock('./components/ParticipantBlock', () => ({
  ParticipantBlock: ({ data }: { data: unknown }) => <div data-testid="participant-block">Участники</div>,
}));

jest.mock('./components/PepBlock/PepBlock', () => ({
  PepBlock: ({ titles }: { titles: unknown }) => <div data-testid="pep-block">Основание ПЭП</div>,
}));

jest.mock('./components/CargoAndRoute', () => ({
  __esModule: true,
  default: () => <div data-testid="cargo-route">Груз и маршрут</div>,
}));

jest.mock('./styles.module.scss', () => ({
  wrapper: 'wrapper',
  footer: 'footer',
  actionBar: 'actionBar',
  actionBarRight: 'actionBarRight',
  viewDocButton: 'viewDocButton',
  closeButton: 'closeButton',
  signButton: 'signButton',
  disable: 'disable',
}));

// ========================
//    Типы
// ========================

type MutationResult<T> = {
  data: T | null;
  isLoading: boolean;
  isSuccess: boolean;
  isError: boolean;
};

type MutationTuple<T> = [jest.Mock, MutationResult<T>];

// ========================
//    Тестовые данные
// ========================

const defaultCardData = {
  id: 'card-123',
  humanReadableId: 'ЭТрН-772812',
  applicationNumber: 'OT-0001-0002931',
  routeNumber: 'CT-0001-0002931',
  status: 'IDENTIFIED',
  sla: '00:42',
  timeZone: 'Asia/Yekaterinburg',
  currentTitle: 'T2',
  senderName: 'ООО Грузоотправитель',
  receiverName: 'АО Грузополучатель',
  carrierName: 'ООО ТК Перевозчик',
  cargoDescription: 'Оборудование, 5 мест',
  cargoPlaces: 5,
  cargoWeightKg: 1250,
  route: 'Екатеринбург — Москва',
  sesFullName: 'Иванов Иван Иванович',
  sesRole: 'Генеральный директор',
  sesEventDatetime: '2026-07-21T10:00:00',
  sesEventId: 'PE-001',
  titleChain: [
    { title: 'T1', signedAt: '2026-07-21T10:00:00', signedBy: 'uuid' },
    { title: 'T2', signedAt: '2026-07-21T11:00:00', signedBy: 'uuid' },
    { title: 'T3', signedAt: null, signedBy: null },
  ],
  createdAt: '2026-07-21T10:00:00',
  updatedAt: '2026-07-22T12:30:00',
  version: 0,
  active: true,
};

const defaultEligibilityState: MutationResult<unknown> = {
  data: null,
  isLoading: false,
  isSuccess: false,
  isError: false,
};

// ========================
//    Хелперы для поиска кнопок
// ========================

const findActionButtons = (): HTMLElement[] => {
  return Array.from(document.querySelectorAll('button'))
    .filter((button) => {
      const text = button.textContent || '';
      return text === 'viewDocument' || text === 'signUkep';
    });
};

// ========================
//    Tests
// ========================

describe('EtrnModal', () => {
  beforeEach(() => {
    jest.clearAllMocks();

    mockUseEtrnCard.mockReturnValue({ data: defaultCardData, isLoading: false });
    mockUseAcquireLock.mockReturnValue([
      jest.fn((_vars: unknown, options?: { onSuccess?: () => void; onError?: (e: unknown) => void }) => {
        options?.onSuccess?.();
      }),
      { status: 'success', data: defaultCardData, error: null, isLoading: false },
    ]);
    mockUseReleaseLock.mockReturnValue([
      jest.fn(),
      { status: 'idle', data: null, error: null, isLoading: false },
    ]);
    mockUseCheckSigningEligibility.mockReturnValue([
      jest.fn(),
      { ...defaultEligibilityState, isSuccess: true } as MutationResult<unknown>,
    ]);
    mockUseGetEtrnTitle.mockReturnValue({
      data: null,
      isLoading: false,
      isError: false,
      error: null,
      refetch: jest.fn().mockResolvedValue({
        fileName: 'first_title_20260821.xml',
        content: 'PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPEZpcnN0VGl0bGU+PC9GaXJzdFRpdGxlPg==',
        creationTime: '2026-08-21T10:30:00',
      }),
    });
  });

  // ==============================
  //  Рендер и базовое поведение
  // ==============================

  describe('рендер', () => {
    it('рендерит модальное окно при visible=true', () => {
      render(<EtrnModal cardId="card-123" visible onClose={jest.fn()} />);

      expect(screen.getByTestId('antd-modal')).toBeInTheDocument();
    });

    it('не рендерит модальное окно при visible=false', () => {
      render(<EtrnModal cardId="card-123" visible={false} onClose={jest.fn()} />);

      expect(screen.queryByTestId('antd-modal')).not.toBeInTheDocument();
    });

    it('показывает humanReadableId карточки', () => {
      render(<EtrnModal cardId="card-123" visible onClose={jest.fn()} />);

      expect(screen.getByText('ЭТрН-772812')).toBeInTheDocument();
    });

    it('отображает titleChain при наличии данных', () => {
      render(<EtrnModal cardId="card-123" visible onClose={jest.fn()} />);

      expect(screen.getByTestId('title-chain')).toBeInTheDocument();
    });

    it('отображает ParticipantBlock', () => {
      render(<EtrnModal cardId="card-123" visible onClose={jest.fn()} />);

      expect(screen.getByTestId('participant-block')).toBeInTheDocument();
    });

    it('отображает PepBlock', () => {
      render(<EtrnModal cardId="card-123" visible onClose={jest.fn()} />);

      expect(screen.getByTestId('pep-block')).toBeInTheDocument();
    });

    it('отображает CargoAndRoute', () => {
      render(<EtrnModal cardId="card-123" visible onClose={jest.fn()} />);

      expect(screen.getByTestId('cargo-route')).toBeInTheDocument();
    });

    it('передаёт suspense:false в useEtrnCard', () => {
      render(<EtrnModal cardId="card-123" visible onClose={jest.fn()} />);

      expect(mockUseEtrnCard).toHaveBeenCalledWith('card-123', { suspense: false });
    });

    it('не передаёт cardId в useEtrnCard при visible=false', () => {
      render(<EtrnModal cardId="card-123" visible={false} onClose={jest.fn()} />);

      expect(mockUseEtrnCard).toHaveBeenCalledWith(null, expect.any(Object));
    });
  });

  // ==============================
  //  Signing Eligibility — TRANSPORT-44818
  // ==============================

  describe('Signing Eligibility', () => {
    it('при visible=true вызывает checkEligibility при монтировании', () => {
      const checkEligibility = jest.fn();
      mockUseCheckSigningEligibility.mockReturnValue([
        checkEligibility,
        { ...defaultEligibilityState, isSuccess: true } as MutationResult<unknown>,
      ]);

      render(<EtrnModal cardId="card-123" visible onClose={jest.fn()} />);

      expect(checkEligibility).toHaveBeenCalledTimes(1);
    });

    it('не вызывает checkEligibility при visible=false', () => {
      const checkEligibility = jest.fn();
      mockUseCheckSigningEligibility.mockReturnValue([
        checkEligibility,
        { ...defaultEligibilityState, isSuccess: true } as MutationResult<unknown>,
      ]);

      render(<EtrnModal cardId="card-123" visible={false} onClose={jest.fn()} />);

      expect(checkEligibility).not.toHaveBeenCalled();
    });

    it('возвращает кортеж [mutate, state] из useCheckSigningEligibility', () => {
      const checkEligibility = jest.fn();
      const state = { ...defaultEligibilityState, isSuccess: true } as MutationResult<unknown>;
      mockUseCheckSigningEligibility.mockReturnValue([checkEligibility, state]);

      render(<EtrnModal cardId="card-123" visible onClose={jest.fn()} />);

      expect(mockUseCheckSigningEligibility).toHaveBeenCalled();
      const calls = mockUseCheckSigningEligibility.mock.results;
      const lastResult = calls[calls.length - 1];
      expect(Array.isArray(lastResult.value)).toBe(true);
      expect(lastResult.value[0]).toBe(checkEligibility);
      expect(lastResult.value[1]).toBe(state);
    });

    // ------------------------------------------
    //  Кнопки: disabled/active состояния
    // ------------------------------------------

    describe('кнопки action bar', () => {
      it('кнопки disabled по умолчанию (initial state: isSuccess=false, isError=false)', () => {
        mockUseCheckSigningEligibility.mockReturnValue([
          jest.fn(),
          { ...defaultEligibilityState, isSuccess: false, isError: false } as MutationResult<unknown>,
        ]);

        render(<EtrnModal cardId="card-123" visible onClose={jest.fn()} />);

        const buttons = findActionButtons();
        expect(buttons).toHaveLength(2);
        buttons.forEach((button) => {
          expect(button).toBeDisabled();
        });
      });

      it('кнопки disabled во время загрузки (isLoading=true)', () => {
        mockUseCheckSigningEligibility.mockReturnValue([
          jest.fn(),
          { ...defaultEligibilityState, isLoading: true, isSuccess: false, isError: false } as MutationResult<unknown>,
        ]);

        render(<EtrnModal cardId="card-123" visible onClose={jest.fn()} />);

        const buttons = findActionButtons();
        buttons.forEach((button) => {
          expect(button).toBeDisabled();
        });
      });

      it('кнопки active при успешной проверке (isSuccess=true, isLoading=false)', () => {
        mockUseCheckSigningEligibility.mockReturnValue([
          jest.fn(),
          { ...defaultEligibilityState, isLoading: false, isSuccess: true, isError: false } as MutationResult<unknown>,
        ]);

        render(<EtrnModal cardId="card-123" visible onClose={jest.fn()} />);

        const buttons = findActionButtons();
        buttons.forEach((button) => {
          expect(button).not.toBeDisabled();
        });
      });

      it('кнопки disabled при ошибке (isError=true)', () => {
        mockUseCheckSigningEligibility.mockReturnValue([
          jest.fn(),
          { ...defaultEligibilityState, isLoading: false, isSuccess: false, isError: true } as MutationResult<unknown>,
        ]);

        render(<EtrnModal cardId="card-123" visible onClose={jest.fn()} />);

        const buttons = findActionButtons();
        buttons.forEach((button) => {
          expect(button).toBeDisabled();
        });
      });

      it('кнопки disabled при isLoading=true и isSuccess=false одновременно', () => {
        mockUseCheckSigningEligibility.mockReturnValue([
          jest.fn(),
          { ...defaultEligibilityState, isLoading: true, isSuccess: false } as MutationResult<unknown>,
        ]);

        render(<EtrnModal cardId="card-123" visible onClose={jest.fn()} />);

        const buttons = findActionButtons();
        buttons.forEach((button) => {
          expect(button).toBeDisabled();
        });
      });
    });
  });

  // ==============================
  //  Закрытие модалки
  // ==============================

  describe('закрытие', () => {
    it('вызывает releaseLock при закрытии модалки', () => {
      const handleClose = jest.fn();

      // Мокаем releaseLock: onSettled вызывается в конце мутации
      const releaseOnSettledRef: { value?: () => void } = {};
      const releaseMutate = jest.fn((_vars: unknown, opts?: { onSettled?: () => void }) => {
        if (opts?.onSettled) {
          releaseOnSettledRef.value = opts.onSettled;
        }
      });
      mockUseReleaseLock.mockReturnValue([releaseMutate, { data: null, isLoading: false, isSuccess: false, isError: false }]);

      // Мокаем acquireLock: onSuccess вызывается для получения блокировки
      mockUseAcquireLock.mockReturnValue([
        jest.fn(({ onSuccess }) => { onSuccess?.(); }),
        { data: defaultCardData, isLoading: false, isSuccess: true, isError: false },
      ]);

      render(<EtrnModal cardId="card-123" visible onClose={handleClose} />);

      fireEvent.click(screen.getByText('Закрыть'));

      // releaseLock вызван с правильными аргументами
      expect(releaseMutate).toHaveBeenCalledWith({ cardId: 'card-123' }, expect.any(Object));

      // onSettled вызывается асинхронно после мутации
      act(() => {
        releaseOnSettledRef.value?.();
      });

      expect(handleClose).toHaveBeenCalled();
    });
  });

  // ==============================
  //  Компоненты внутри модалки
  // ==============================

  it('отображает TitleChain с правильным количеством титулов', () => {
    render(<EtrnModal cardId="card-123" visible={true} onClose={jest.fn()} onSigned={jest.fn()} />);

    expect(screen.getByText('Цепочка титулов ЭТрН')).toBeInTheDocument();
  });

  it('отображает ParticipantBlock с данными участников', () => {
    render(<EtrnModal cardId="card-123" visible={true} onClose={jest.fn()} onSigned={jest.fn()} />);

    expect(screen.getByText('Участники')).toBeInTheDocument();
  });

  it('отображает PepBlock с данными PEP', () => {
    render(<EtrnModal cardId="card-123" visible={true} onClose={jest.fn()} onSigned={jest.fn()} />);

    expect(screen.getByText('Основание ПЭП')).toBeInTheDocument();
  });

  it('отображает CargoAndRoute с данными груза', () => {
    render(<EtrnModal cardId="card-123" visible={true} onClose={jest.fn()} onSigned={jest.fn()} />);

    expect(screen.getByText('Груз и маршрут')).toBeInTheDocument();
  });

  it('отображает "Ожидание данных от Корус" при пустом titleChain', () => {
    mockUseEtrnCard.mockReturnValue({
      data: { ...defaultCardData, titleChain: [] },
      isLoading: false,
      error: null,
    });

    render(<EtrnModal cardId="card-123" visible={true} onClose={jest.fn()} onSigned={jest.fn()} />);

    expect(screen.getByText('Ожидание данных от Корус')).toBeInTheDocument();
  });

  // ==============================
  //  Подписание Т3 (SignT3Modal)
  // ==============================

  it('кнопка «Подписать УКЭП» активна при currentTitle=T2 и активной блокировке', async () => {
    let onSuccessAcquire: (() => void) | undefined;
    const acquireMutate = jest.fn((_vars: unknown, options?: { onSuccess?: () => void; onError?: (e: unknown) => void }) => {
      onSuccessAcquire = options?.onSuccess;
      options?.onSuccess?.();
    });
    mockUseAcquireLock.mockReturnValue([acquireMutate, { status: 'success', error: null, isLoading: false }]);

    render(<EtrnModal cardId="card-123" visible={true} onClose={jest.fn()} onSigned={jest.fn()} />);

    // ждём пока useEffect выполнит acquireLock и установит lockStatus в 'active'
    await waitFor(() => {
      expect(acquireMutate).toHaveBeenCalled();
    });

    // промотать до активного состояния
    act(() => {
      onSuccessAcquire?.();
    });

    await waitFor(() => {
      expect(screen.getByText('signUkep')).toBeInTheDocument();
    });
    const signButton = screen.getByText('signUkep').closest('button');
    expect(signButton).not.toBeDisabled();
  });

  it('кнопка «Подписать УКЭП» не отображается при currentTitle=T1', async () => {
    const acquireMutate = jest.fn((_vars: unknown, options?: { onSuccess?: () => void }) => options?.onSuccess?.());
    mockUseAcquireLock.mockReturnValue([acquireMutate, { status: 'success', error: null, isLoading: false }]);

    mockUseEtrnCard.mockReturnValue({
      data: { ...defaultCardData, currentTitle: 'T1' },
      isLoading: false,
      error: null,
    });

    render(<EtrnModal cardId="card-123" visible={true} onClose={jest.fn()} onSigned={jest.fn()} />);

    // ждём пока useEffect выполнит acquireLock
    await waitFor(() => {
      expect(screen.getByText('Закрыть')).toBeInTheDocument();
    });
    expect(screen.queryByText('signUkep')).not.toBeInTheDocument();
  });

  it('кнопка «Подписать УКЭП» не отображается, если блокировка в состоянии conflict', async () => {
    const acquireMutate = jest.fn((_vars: unknown, options?: { onError?: (e: unknown) => void }) => options?.onError?.({ response: { status: 409 } }));
    mockUseAcquireLock.mockReturnValue([acquireMutate, { status: 'error', error: null, isLoading: false }]);

    render(<EtrnModal cardId="card-123" visible={true} onClose={jest.fn()} onSigned={jest.fn()} />);

    // ждём пока useEffect выполнит acquireLock с ошибкой
    await waitFor(() => {
      expect(screen.getByText('lockConflictBanner')).toBeInTheDocument();
    });
    expect(screen.queryByText('signUkep')).not.toBeInTheDocument();
  });

  it('клик по «Подписать УКЭП» открывает SignT3Modal', async () => {
    const acquireMutate = jest.fn((_vars: unknown, options?: { onSuccess?: () => void }) => options?.onSuccess?.());
    mockUseAcquireLock.mockReturnValue([acquireMutate, { status: 'success', error: null, isLoading: false }]);

    render(<EtrnModal cardId="card-123" visible={true} onClose={jest.fn()} onSigned={jest.fn()} />);

    // ждём пока useEffect выполнит acquireLock и появится кнопка
    await waitFor(() => {
      expect(screen.getByText('signUkep')).toBeInTheDocument();
    });

    // До клика SignT3Modal не виден
    expect(screen.queryByTestId('sign-t3-modal')).not.toBeInTheDocument();

    // Клик по кнопке
    fireEvent.click(screen.getByText('signUkep').closest('button')!);

    // После клика SignT3Modal отрисован
    await waitFor(() => {
      expect(screen.getByTestId('sign-t3-modal')).toBeInTheDocument();
    });

    // Последний вызов mockSignT3Modal должен быть с visible=true
    const lastCallProps = mockSignT3Modal.mock.calls[mockSignT3Modal.mock.calls.length - 1][0];
    expect(lastCallProps.visible).toBe(true);
  });

  it('передаёт в SignT3Modal cardId и колбэки onClose/onSigned', async () => {
    const acquireMutate = jest.fn((_vars: unknown, options?: { onSuccess?: () => void }) => options?.onSuccess?.());
    mockUseAcquireLock.mockReturnValue([acquireMutate, { status: 'success', error: null, isLoading: false }]);

    const onClose = jest.fn();
    const onSigned = jest.fn();

    render(<EtrnModal cardId="card-123" visible={true} onClose={onClose} onSigned={onSigned} />);

    await waitFor(() => {
      expect(screen.getByText('signUkep')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('signUkep').closest('button')!);

    await waitFor(() => {
      expect(screen.getByTestId('sign-t3-modal')).toBeInTheDocument();
    });

    const lastCallProps = mockSignT3Modal.mock.calls[mockSignT3Modal.mock.calls.length - 1][0];
    expect(lastCallProps.cardId).toBe('card-123');
    expect(typeof lastCallProps.onClose).toBe('function');
    expect(typeof lastCallProps.onSigned).toBe('function');
  });

  it('передаёт в SignT3Modal реальный content из useGetEtrnTitle после успешного fetch', async () => {
    const fetchTitle = jest.fn().mockResolvedValue({
      fileName: 'first_title_20260821.xml',
      content: 'PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPEZpcnN0VGl0bGU+PC9GaXJzdFRpdGxlPg==',
      creationTime: '2026-08-21T10:30:00',
    });
    mockUseGetEtrnTitle.mockReturnValue({
      data: null,
      isLoading: false,
      isError: false,
      error: null,
      refetch: fetchTitle,
    });

    render(<EtrnModal cardId="card-123" visible={true} onClose={jest.fn()} onSigned={jest.fn()} />);

    await waitFor(() => {
      expect(screen.getByText('signUkep')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('signUkep').closest('button')!);

    await waitFor(() => {
      expect(fetchTitle).toHaveBeenCalled();
    });

    await waitFor(() => {
      expect(screen.getByTestId('sign-t3-modal')).toBeInTheDocument();
    });

    const lastCallProps = mockSignT3Modal.mock.calls[mockSignT3Modal.mock.calls.length - 1][0];
    expect(lastCallProps.title).toEqual({
      fileName: 'first_title_20260821.xml',
      content: 'PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPEZpcnN0VGl0bGU+PC9GaXJzdFRpdGxlPg==',
      creationTime: '2026-08-21T10:30:00',
    });
  });

  it('при ошибке fetch title SignT3Modal НЕ открывается', async () => {
    const axiosError = {
      message: 'Network Error',
      request: { response: JSON.stringify({ message: 'Network Error' }) },
    } as AxiosError;
    // В react-query v2 refetch() возвращает Promise<TResult | undefined>:
    // при ошибке запрос резолвится с undefined, а не реджектится.
    // Само уведомление пользователя делает onError в useGetEtrnTitle.
    const fetchTitle = jest.fn().mockResolvedValue(undefined);
    mockUseGetEtrnTitle.mockReturnValue({
      data: null,
      isLoading: false,
      isError: true,
      error: axiosError,
      refetch: fetchTitle,
    });

    render(<EtrnModal cardId="card-123" visible={true} onClose={jest.fn()} onSigned={jest.fn()} />);

    await waitFor(() => {
      expect(screen.getByText('signUkep')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('signUkep').closest('button')!);

    await waitFor(() => {
      expect(fetchTitle).toHaveBeenCalled();
    });

    // Дать React время завершить async openSignModal
    await new Promise(resolve => setTimeout(resolve, 0));

    // SignT3Modal не должен быть отрисован при сетевой ошибке
    expect(screen.queryByTestId('sign-t3-modal')).not.toBeInTheDocument();

    // Показ toast при сетевой ошибке — ответственность хука useGetEtrnTitle
    // (его onError вызывает logger.toMessage). Компонент тихо завершает работу.
    expect(mockLogger.toMessage).not.toHaveBeenCalled();
  });

  it('при пустом content SignT3Modal НЕ открывается и показывается toast', async () => {
    const fetchTitle = jest.fn().mockResolvedValue({
      fileName: 'first_title_20260821.xml',
      content: '',
      creationTime: '2026-08-21T10:30:00',
    });
    mockUseGetEtrnTitle.mockReturnValue({
      data: null,
      isLoading: false,
      isError: false,
      error: null,
      refetch: fetchTitle,
    });

    render(<EtrnModal cardId="card-123" visible={true} onClose={jest.fn()} onSigned={jest.fn()} />);

    await waitFor(() => {
      expect(screen.getByText('signUkep')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('signUkep').closest('button')!);

    await waitFor(() => {
      expect(fetchTitle).toHaveBeenCalled();
    });

    // Дать React время завершить async openSignModal
    await new Promise(resolve => setTimeout(resolve, 0));

    // SignT3Modal не должен быть отрисован — пустой content опасно подписывать КриптоПро
    expect(screen.queryByTestId('sign-t3-modal')).not.toBeInTheDocument();

    // logger.toMessage должен быть вызван с уровнем 'error' и сообщением про пустой content
    expect(mockLogger.toMessage).toHaveBeenCalledWith(
      'error',
      expect.stringContaining('содержимое титула')
    );
  });
});
