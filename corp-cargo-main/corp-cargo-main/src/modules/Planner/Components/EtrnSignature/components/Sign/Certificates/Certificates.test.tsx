import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import Certificates from './Certificates';
import type { CertificateExtended } from 'modules/Planner/Components/EtrnSignature/cryptoPro.interface';

// ========================
//       Моки
// ========================

jest.mock('i18n', () => ({
  useTranslation: () => ({
    t: {
      Etrn: {
        signModal: {
          certificates: {
            validTo: 'Действителен до:',
          },
        },
      },
    },
  }),
}));

jest.mock('@ant-design/icons', () => ({
  FileProtectOutlined: () => <span data-testid="file-protect-icon" />,
}));

jest.mock('classnames', () => ({
  __esModule: true,
  default: (...args: unknown[]) => args.filter(Boolean).join(' '),
}));

jest.mock('constants/constants.app', () => ({
  DATE_FORMAT: {
    DATE_WITH_TIME_DOTS: 'DD.MM.YYYY HH:mm',
    DATE_WITH_TIME_DOTS_ZONE: 'DD.MM.YYYY HH:mm ([GMT]Z)',
  },
}));

jest.mock('./styles.module.scss', () => ({
  container: 'container',
  card: 'card',
  card_selected: 'card_selected',
  card_disabled: 'card_disabled',
  card__content: 'card__content',
  block: 'block',
  block__label: 'block__label',
}));

// ========================
//    Тестовые данные
// ========================

const makeCert = (overrides: Partial<CertificateExtended> = {}): CertificateExtended => ({
  name: 'Test Certificate',
  issuerName: 'Issuer',
  subjectName: 'Subject',
  thumbprint: 'thumb-1',
  validFrom: '2026-01-01T00:00:00Z',
  validTo: '2026-12-31T23:59:00Z',
  active: true,
  ...overrides,
} as CertificateExtended);

// ========================
//       Тесты
// ========================

describe('Certificates', () => {
  describe('рендеринг', () => {
    it('должен рендерить контейнер', () => {
      const { container } = render(
        <Certificates selected={null} list={null} onSelect={jest.fn()} />,
      );
      expect(container.firstChild).not.toBeNull();
    });

    it('не должен рендерить карточки при list = null', () => {
      render(<Certificates selected={null} list={null} onSelect={jest.fn()} />);
      expect(screen.queryByRole('button')).toBeNull();
    });

    it('не должен рендерить карточки при пустом списке', () => {
      render(<Certificates selected={null} list={[]} onSelect={jest.fn()} />);
      expect(screen.queryByRole('button')).toBeNull();
    });

    it('должен рендерить по карточке на каждый сертификат', () => {
      const list = [
        makeCert({ thumbprint: 'thumb-1' }),
        makeCert({ thumbprint: 'thumb-2' }),
        makeCert({ thumbprint: 'thumb-3' }),
      ];
      render(<Certificates selected={null} list={list} onSelect={jest.fn()} />);
      expect(screen.getAllByRole('button')).toHaveLength(3);
    });

    it('должен отображать имя сертификата', () => {
      const list = [makeCert({ name: 'My Certificate' })];
      render(<Certificates selected={null} list={list} onSelect={jest.fn()} />);
      expect(screen.getByText('My Certificate')).toBeInTheDocument();
    });

    it('должен отображать локализованный лейбл "Действителен до:"', () => {
      const list = [makeCert()];
      render(<Certificates selected={null} list={list} onSelect={jest.fn()} />);
      expect(screen.getByText('Действителен до:')).toBeInTheDocument();
    });

    it('должен рендерить иконку FileProtectOutlined для каждого сертификата', () => {
      const list = [
        makeCert({ thumbprint: 'thumb-1' }),
        makeCert({ thumbprint: 'thumb-2' }),
      ];
      render(<Certificates selected={null} list={list} onSelect={jest.fn()} />);
      expect(screen.getAllByTestId('file-protect-icon')).toHaveLength(2);
    });
  });

  describe('форматирование даты', () => {
    it('должен форматировать validTo через moment с DATE_WITH_TIME_DOTS', () => {
      const list = [makeCert({ validTo: '2026-12-31T23:59:00Z' })];
      render(<Certificates selected={null} list={list} onSelect={jest.fn()} />);
      // moment выводит дату в локали машины (UTC+3 в jest-окружении): 01.01.2027 02:59.
      // Главное — что moment был вызван и строка содержит дату в формате DD.MM.YYYY HH:mm.
      const validToText = screen.getByText(/\d{2}\.\d{2}\.\d{4} \d{2}:\d{2}/);
      expect(validToText).toBeInTheDocument();
    });
  });

  describe('взаимодействие', () => {
    it('должен вызывать onSelect с thumbprint при клике на активный сертификат', () => {
      const onSelect = jest.fn();
      const list = [makeCert({ thumbprint: 'thumb-abc', active: true })];
      render(<Certificates selected={null} list={list} onSelect={onSelect} />);

      fireEvent.click(screen.getByRole('button'));

      expect(onSelect).toHaveBeenCalledTimes(1);
      expect(onSelect).toHaveBeenCalledWith('thumb-abc');
    });

    it('НЕ должен вызывать onSelect при клике на неактивный сертификат', () => {
      const onSelect = jest.fn();
      const list = [makeCert({ thumbprint: 'thumb-disabled', active: false })];
      render(<Certificates selected={null} list={list} onSelect={onSelect} />);

      fireEvent.click(screen.getByRole('button'));

      expect(onSelect).not.toHaveBeenCalled();
    });

    it('должен корректно работать с несколькими сертификатами', () => {
      const onSelect = jest.fn();
      const list = [
        makeCert({ thumbprint: 'thumb-1', active: true }),
        makeCert({ thumbprint: 'thumb-2', active: true }),
        makeCert({ thumbprint: 'thumb-3', active: false }),
      ];
      render(<Certificates selected={null} list={list} onSelect={onSelect} />);

      const buttons = screen.getAllByRole('button');
      fireEvent.click(buttons[0]);
      fireEvent.click(buttons[1]);

      expect(onSelect).toHaveBeenCalledTimes(2);
      expect(onSelect).toHaveBeenNthCalledWith(1, 'thumb-1');
      expect(onSelect).toHaveBeenNthCalledWith(2, 'thumb-2');
    });
  });

  describe('selected prop (визуальное состояние)', () => {
    it('должен передавать role="button" для всех карточек', () => {
      const list = [
        makeCert({ thumbprint: 'thumb-1' }),
        makeCert({ thumbprint: 'thumb-2' }),
      ];
      render(<Certificates selected={null} list={list} onSelect={jest.fn()} />);
      expect(screen.getAllByRole('button')).toHaveLength(2);
    });
  });

  describe('aria-disabled', () => {
    it('должен ставить aria-disabled=false для активного сертификата', () => {
      const list = [makeCert({ active: true })];
      render(<Certificates selected={null} list={list} onSelect={jest.fn()} />);
      const button = screen.getByRole('button');
      expect(button.getAttribute('aria-disabled')).toBe('false');
    });

    it('должен ставить aria-disabled=true для неактивного сертификата', () => {
      const list = [makeCert({ active: false })];
      render(<Certificates selected={null} list={list} onSelect={jest.fn()} />);
      const button = screen.getByRole('button');
      expect(button.getAttribute('aria-disabled')).toBe('true');
    });
  });
});
