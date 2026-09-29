import React from 'react';
import { render, screen } from '@testing-library/react';
import TitleChain from './TitleChain';

// ========================
//    Моки
// ========================

jest.mock('i18n', () => ({
  useTranslation: () => ({
    t: {
      Etrn: {
        card: {
          titleChainTitle: 'Цепочка титулов ЭТрН',
          titleChainWaiting: 'Ожидание данных от Корус',
          titleLabels: {
            T1: 'Титул 1 - Отправитель',
            T2: 'Титул 2 - Перевозчик',
            T3: 'Титул 3 - Получатель',
            T4: 'Титул 4 - Перевозчик',
          },
          statuses: {
            EXPECTED: 'Ожидается',
            AVAILABLE: 'Доступен',
            WAIT_CONDITIONS: 'Ожидание условий',
            READY_TO_SIGN: 'Готов к подписанию',
            SIGNING: 'Подписание',
            SIGNED_LOCALLY: 'Подписан',
            SENT_TO_OPERATOR: 'Отправлен перевозчику',
            ACCEPTED_BY_OPERATOR: 'Принят перевозчиком',
            ERROR: 'Ошибка',
            REJECTED: 'Отклонён',
            OUTCOME_UNKNOWN: 'Неизвестный исход',
            SIGNED: 'Подписан',
            NOT_STARTED: 'Не начат',
            NOT_SIGNED: 'Не подписан',
          },
        },
      },
    },
  }),
}));

jest.mock('./styles.module.scss', () => ({
  titleChain: 'titleChain',
  title: 'title',
  waitingText: 'waitingText',
  chainWrapper: 'chainWrapper',
  titleCard: 'titleCard',
  titleLabel: 'titleLabel',
  bg_success: 'bg_success',
  bg_default: 'bg_default',
  signerNameTextDark: 'signerNameTextDark',
  signerNameTextLight: 'signerNameTextLight',
  statusLabelTextLight: 'statusLabelTextLight',
  textDark: 'textDark',
  statusBlock: 'statusBlock',
  statusLabel: 'statusLabel',
  textLight: 'textLight',
  signerName: 'signerName',
  connector: 'connector',
  connector_success: 'connector_success',
  connector_default: 'connector_default',
}));

// ========================
//    Данные
// ========================

const signedTitle = {
  title: 'T1',
  signedAt: '2026-07-21T10:00:00',
  signedBy: 'Иванов И.И.',
};

const unsignedTitle = {
  title: 'T2',
  signedAt: null,
  signedBy: null,
};

// ========================
//  Тесты
// ========================

describe('TitleChain', () => {
  it('рендерит заголовок при пустом списке', () => {
    render(<TitleChain titles={[]} />);
    expect(screen.getByText('Цепочка титулов ЭТрН')).toBeInTheDocument();
  });

  it('рендерит текст ожидания при пустом списке', () => {
    render(<TitleChain titles={[]} />);
    expect(screen.getByText('Ожидание данных от Корус')).toBeInTheDocument();
  });

  it('рендерит DOM при пустом списке', () => {
    const { container } = render(<TitleChain titles={[]} />);
    expect(container.querySelector('.titleChain')).toBeInTheDocument();
    expect(container.querySelector('.title')).toBeInTheDocument();
    expect(container.querySelector('.waitingText')).toBeInTheDocument();
  });

  it('рендерит заголовок при наличии титулов', () => {
    render(<TitleChain titles={[signedTitle, unsignedTitle]} />);
    expect(screen.getByText('Цепочка титулов ЭТрН')).toBeInTheDocument();
  });

  it('рендерит подписанный титул с правильными статусом и контрагентом', () => {
    render(<TitleChain titles={[signedTitle]} />);
    expect(screen.getByText('Подписан')).toBeInTheDocument();
    expect(screen.getByText('Иванов И.И.')).toBeInTheDocument();
  });

  it('рендерит неподписанный титул с правильными статусом и контрагентом', () => {
    render(<TitleChain titles={[unsignedTitle]} />);
    expect(screen.getByText('Не подписан')).toBeInTheDocument();
    expect(screen.getByText('Не подписан')).toBeInTheDocument();
  });

  it('рендерит label титула T1', () => {
    render(<TitleChain titles={[signedTitle]} />);
    expect(screen.getByText('Титул 1 - Отправитель')).toBeInTheDocument();
  });

  it('рендерит 2 titleCard для 2 титулов', () => {
    const { container } = render(<TitleChain titles={[signedTitle, unsignedTitle]} />);
    expect(container.querySelectorAll('.titleCard')).toHaveLength(2);
  });

  it('рендерит connectors между титулами (N-1 штук)', () => {
    const { container } = render(<TitleChain titles={[signedTitle, unsignedTitle]} />);
    const connectors = container.querySelectorAll('.connector');
    expect(connectors).toHaveLength(1);
  });

  it('не рендерит connector после последнего титула', () => {
    const { container } = render(<TitleChain titles={[signedTitle]} />);
    const connectors = container.querySelectorAll('.connector');
    expect(connectors).toHaveLength(0);
  });

  it('подписанные титулы имеют bg_success класс', () => {
    const { container } = render(<TitleChain titles={[signedTitle]} />);
    const titleLabels = container.querySelectorAll('.titleLabel');
    // Первый titleLabel с bg_success
    const successLabel = Array.from(titleLabels).find((el) => el.classList.contains('bg_success'));
    expect(successLabel).toBeInTheDocument();
  });

  it('неподписанные титулы имеют bg_default класс', () => {
    const { container } = render(<TitleChain titles={[unsignedTitle]} />);
    const titleLabels = container.querySelectorAll('.titleLabel');
    // titleLabel без bg_success — значит bg_default
    const defaultLabel = Array.from(titleLabels).find((el) =>
      !el.classList.contains('bg_success')
    );
    expect(defaultLabel).toBeInTheDocument();
  });

  it('connectors между двумя подписанными имеют connector_success', () => {
    const signedT1 = { ...signedTitle, title: 'T1' };
    const signedT2 = { ...signedTitle, title: 'T2' };
    const { container } = render(<TitleChain titles={[signedT1, signedT2]} />);
    const connectors = container.querySelectorAll('.connector');
    expect(connectors[0]).toHaveClass('connector_success');
  });

  it('connectors с неподписанным следующим имеют connector_default', () => {
    const signedT1 = { ...signedTitle, title: 'T1' };
    const unsignedT2 = { ...unsignedTitle, title: 'T2' };
    const { container } = render(<TitleChain titles={[signedT1, unsignedT2]} />);
    const connectors = container.querySelectorAll('.connector');
    expect(connectors[0]).toHaveClass('connector_default');
  });

  it('рендерит статус EXPECTED для титула без label в i18n', () => {
    const unknownTitle = { title: 'UNKNOWN', signedAt: null, signedBy: null };
    render(<TitleChain titles={[unknownTitle]} />);
    expect(screen.getByText('Не начат')).toBeInTheDocument();
  });

  it('рендерит все 4 поля (label, status, signer, connector) для подписанного титула', () => {
    const { container } = render(<TitleChain titles={[signedTitle]} />);
    const cards = container.querySelectorAll('.titleCard');
    expect(cards).toHaveLength(1);
    const card = cards[0] as Element;
    expect(card.querySelector('.titleLabel')).toBeInTheDocument();
    expect(card.querySelector('.statusBlock')).toBeInTheDocument();
    expect(card.querySelector('.statusLabel')).toBeInTheDocument();
    expect(card.querySelector('.signerName')).toBeInTheDocument();
  });
});
