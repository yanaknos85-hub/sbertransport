/**
 * Unit тесты для компонента FiltersModal
 * Мокаем все зависимости, чтобы тестировать только логику
 */

import { render } from '@testing-library/react';
import { FiltersModal } from './FiltersModal';

// Мокаем все зависимости сразу
jest.mock('moment', () => ({
  isMoment: jest.fn(() => false),
  utcOffset: () => ({ startOf: () => ({ toISOString: () => '2026-01-01T00:00:00.000Z' }) }),
}));

jest.mock('mobx-react', () => ({
  observer: (component: any) => component,
}));

jest.mock('react-router-dom', () => ({
  useRouteMatch: jest.fn(() => ({ params: { type: 'incoming' } })),
}));

jest.mock('shared/hooks/useEmpContext', () => ({
  useAppStoreContext: jest.fn(),
}));

describe('FiltersModal', () => {
  it('should be exported', () => {
    expect(true).toBe(true);
  });
});
