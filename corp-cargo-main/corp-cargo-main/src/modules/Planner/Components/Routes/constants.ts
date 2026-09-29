import { colors } from 'shared/styles/styles';
import { RouteStatusEnum } from '../../types';


export const PlanningTitle = {
  [RouteStatusEnum.CARGO_PLANNING]: 'Планируется',
  [RouteStatusEnum.CARGO_PLANNING_FINISHED]: 'Запланировано',
};

export const PLANNING_INDICATORS = {
  [RouteStatusEnum.CARGO_PLANNING]: {
    name: 'Планируется',
    right: '16px',
    color: colors.blue10,
    backgroundColor: colors.blue1,
  },
  [RouteStatusEnum.CARGO_PLANNING_FINISHED]: {
    name: 'Запланировано',
    right: '16px',
    color: colors.green10,
    backgroundColor: colors.green1,
  },
};

export const CREATOR_INDICATORS = {
  'Автоматически (шедулер)': {
    name: 'Автоматически (шедулер)',
    right: '136px',
    color: colors.gray8,
    border: colors.gray4,
    backgroundColor: 'inherit',
  },
  'Ручной': {
    name: 'Ручной',
    right: '136px',
    color: colors.gray8,
    border: colors.gray4,
    backgroundColor: 'inherit',
  },
  'Интеграция': {
    name: 'Интеграция',
    right: '136px',
    color: colors.gray8,
    border: colors.gray4,
    backgroundColor: 'inherit',
  },
  'Автопланирование SRM': {
    name: 'Автопланирование SRM',
    right: '136px',
    color: colors.gray8,
    border: colors.gray4,
    backgroundColor: 'inherit',
  },
};
