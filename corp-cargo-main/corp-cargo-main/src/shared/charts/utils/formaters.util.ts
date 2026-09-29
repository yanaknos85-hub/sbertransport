import { Translation } from 'i18n';
import { AnalyticTypes, Statuses } from '../constants/charts.constants';
import { ICardContent, IDataForPieCharts, IDataTotal } from '../types/types';

export const convertNumberToPercent = (totalValue: number, value: number): number => {
  if (!totalValue) {
    return 0;
  }

  const result: number = (value / totalValue) * 100;
  return result < 0.5 ? Math.ceil(result) : result;
};

export const formatValue = (value: number, type: string): string | number => {
  switch (type) {
    case 'percent':
      return `${value}%`;
    default:
      return value;
  }
};

export function formatDataForPieChart(data: IDataTotal[], type: string): IDataForPieCharts[] {
  const defaultState = {
    name: '',
    value: 0,
    typeValue: '',
  };

  if (type === AnalyticTypes.total) {
    const executed = data.find(el => el.code === 'executed') || defaultState;
    const unExecuted = data.find(el => el.code === 'unExecuted') || defaultState;
    const cancelled = data.find(el => el.code === 'cancelled') || defaultState;
    const summary = executed.value + unExecuted.value + cancelled.value;

    return [
      {
        ...executed,
        code: 'valueForPie',
        valueForPie: convertNumberToPercent(summary, executed.value),
      },
      {
        ...unExecuted,
        code: 'valueForPie',
        valueForPie: convertNumberToPercent(summary, unExecuted.value),
      },
      {
        ...cancelled,
        name: cancelled.name || 'Отменено', // Костыль пока с бека неизвесто что приходит
        code: 'valueForPie',
        valueForPie: convertNumberToPercent(summary, cancelled.value),
      },
    ];
  }

  if (type === AnalyticTypes.csi) {
    return data.map(entry => {
      if (entry.code === 'norm') {
        const fact = data.find(el => el.code === 'fact')?.value || 0;
        const valueForPie = fact && 100 - fact;
        return {
          ...entry, code: 'valueForPie', valueForPie,
        };
      }
      const valueForPie = entry.value;
      return {
        ...entry, code: 'valueForPie', valueForPie,
      };
    });
  }
  return data.map(entry => {
    if (entry.code === 'norm') {
      const fact = data.find(el => el.code === 'fact')?.value || 0;
      const valueForPie = fact && 100 - fact;
      return {
        ...entry, code: 'valueForPie', valueForPie,
      };
    }
    const factValue = entry.value < 0 ? 0 : entry.value;
    return {
      ...entry, code: 'valueForPie', valueForPie: factValue,
    };
  });
}

export function formatHeaderContent(t: Translation, type: string): ICardContent | never {
  const getHeaderContent: (() => ICardContent) | undefined = {
    [AnalyticTypes.total]: () => ({ ...t.Widgets.charts.total }),
    [AnalyticTypes.budget]: () => ({ ...t.Widgets.charts.budget }),
    [AnalyticTypes.csi]: () => ({ ...t.Widgets.charts.csi }),
    [AnalyticTypes.sla]: () => ({ ...t.Widgets.charts.sla }),
  }[type as AnalyticTypes];

  if (!getHeaderContent) {
    throw new Error('Invalid header content type');
  }

  return getHeaderContent();
}

export const serializeName = (str: string, t: Translation): string => {
  const map: Record<string, string> = new Proxy(
    {
      [Statuses.DONE]: t.Widgets.Legends.fact,
      [Statuses.NOT_DONE]: t.Widgets.Legends.inWork,
    },
    {
      get(target, name: Statuses.DONE) {
        return target[name] || str;
      },
    }
  );

  return map[str];
};
