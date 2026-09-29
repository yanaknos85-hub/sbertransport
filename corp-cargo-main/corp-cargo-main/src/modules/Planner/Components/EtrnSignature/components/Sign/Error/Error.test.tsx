import React from 'react';
import { render, screen } from '@testing-library/react';
import Error from './Error';
import { Messages } from 'modules/Planner/Components/EtrnSignature/constants/CryptoPro';

// ========================
//    Моки
// ========================

jest.mock('i18n', () => ({
  useTranslation: () => ({
    t: {
      Etrn: {
        signModal: {
          error: {
            invalidWorkplace: 'Не удалось инициализировать рабочее место',
            getSystemInfoError: 'Не удалось получить информацию о системе',
            noCspProvider: 'КриптоПро CSP не установлен',
            noCadesPlugin: 'Плагин КриптоПро для браузера не установлен',
            getCertificatesError: 'Не удалось получить список сертификатов',
            noCertificates: 'Сертификаты не найдены',
            signingError: 'Ошибка при формировании подписи',
          },
        },
      },
    },
  }),
}));

jest.mock('@ant-design/icons', () => ({
  CloseCircleOutlined: () => <span data-testid="close-circle">X</span>,
}));

jest.mock('./styles.module.scss', () => ({
  container: 'container',
  title: 'title',
}));

// ========================
//  Тесты
// ========================

describe('Error', () => {
  describe('isWorkplaceFailed = true (NoCspProvider / NoCadesPlugin)', () => {
    it('рендерит заголовок "Не удалось инициализировать рабочее место"', () => {
      render(<Error messages={[Messages.NoCspProvider]} />);
      expect(screen.getByText('Не удалось инициализировать рабочее место')).toBeInTheDocument();
    });

    it('рендерит <ul> со списком ошибок', () => {
      const { container } = render(<Error messages={[Messages.NoCspProvider, Messages.NoCadesPlugin]} />);
      const ul = container.querySelector('ul');
      expect(ul).toBeInTheDocument();
    });

    it('рендерит <li> для каждого сообщения', () => {
      render(<Error messages={[Messages.NoCspProvider, Messages.NoCadesPlugin]} />);
      expect(screen.getByText('КриптоПро CSP не установлен')).toBeInTheDocument();
      expect(screen.getByText('Плагин КриптоПро для браузера не установлен')).toBeInTheDocument();
    });

    it('рендерит 2 <li> для двух сообщений', () => {
      const { container } = render(<Error messages={[Messages.NoCspProvider, Messages.NoCadesPlugin]} />);
      const items = container.querySelectorAll('li');
      expect(items).toHaveLength(2);
    });
  });

  describe('isWorkplaceFailed = false (другие ошибки)', () => {
    it('рендерит заголовок с текстом первого сообщения (SigningError)', () => {
      render(<Error messages={[Messages.SigningError]} />);
      expect(screen.getByText('Ошибка при формировании подписи')).toBeInTheDocument();
    });

    it('рендерит заголовок с текстом первого сообщения (NoCertificates)', () => {
      render(<Error messages={[Messages.NoCertificates]} />);
      expect(screen.getByText('Сертификаты не найдены')).toBeInTheDocument();
    });

    it('рендерит заголовок с текстом первого сообщения (GetSystemInfoError)', () => {
      render(<Error messages={[Messages.GetSystemInfoError]} />);
      expect(screen.getByText('Не удалось получить информацию о системе')).toBeInTheDocument();
    });

    it('рендерит заголовок с текстом первого сообщения (GetCertificatesError)', () => {
      render(<Error messages={[Messages.GetCertificatesError]} />);
      expect(screen.getByText('Не удалось получить список сертификатов')).toBeInTheDocument();
    });

    it('НЕ рендерит <ul> со списком', () => {
      const { container } = render(<Error messages={[Messages.SigningError]} />);
      const ul = container.querySelector('ul');
      expect(ul).toBeNull();
    });

    it('НЕ рендерит <li>', () => {
      const { container } = render(<Error messages={[Messages.SigningError]} />);
      const items = container.querySelectorAll('li');
      expect(items).toHaveLength(0);
    });
  });

  describe('структура', () => {
    it('рендерит CloseCircleOutlined', () => {
      render(<Error messages={[Messages.SigningError]} />);
      expect(screen.getByTestId('close-circle')).toBeInTheDocument();
    });

    it('рендерит корневой контейнер', () => {
      const { container } = render(<Error messages={[Messages.SigningError]} />);
      const root = container.firstChild as HTMLElement | null;
      expect(root).not.toBeNull();
    });

    it('рендерит блок заголовка', () => {
      const { container } = render(<Error messages={[Messages.SigningError]} />);
      const titleBlock = container.querySelector('.title');
      expect(titleBlock).toBeInTheDocument();
    });

    it('рендерит текст заголовка внутри блока title', () => {
      const { container } = render(<Error messages={[Messages.SigningError]} />);
      const titleBlock = container.querySelector('.title');
      expect(titleBlock?.textContent).toContain('Ошибка при формировании подписи');
    });
  });
});
